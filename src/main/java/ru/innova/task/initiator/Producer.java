package ru.innova.task.initiator;

import java.io.PrintWriter;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;

import ru.innova.task.common.AbstractWorker;
import ru.innova.task.common.ProtocolConstants;

/**
 * Объект-исполнитель, отвечающий за генерацию чисел, подлежащих
 * отправке в систему серверов.
 * 
 * В данном классе происходит подготовка чисел для отправки и их сохранение в текстовый файл.
 * 
 * @author sergey
 */
public class Producer extends AbstractWorker {
    private static final String OUTPUT_ENCODING = "UTF-8";
    private static final String OUTPUT_FILE_NAME = "initiator_send.txt";
    private final BlockingQueue<Integer> output;
    private final DataGenerator generator;
    private final ReentrantLock mode;
    private final Condition doSomething;
    private volatile boolean started;

    /**
     * Создает объект.
     * 
     * @param output очередь, куда надо помещать новые данные 
     * @param generator объект для генерации данных и сверки с входящими потоками
     */
    protected Producer(BlockingQueue<Integer> output, DataGenerator generator) {
        super(1, "initiator.Producer");
        this.output = output;
        this.generator = generator;
        this.mode = new ReentrantLock();
        this.doSomething = mode.newCondition();
        this.started = false;
        this.exited = false;
        logger.log(Level.FINE, "[{0}] Producer.Producer()", myNumber);
    }

    @Override
    public Integer call() {
        logger.log(Level.FINE, "[{0}] run()", myNumber);
        try (PrintWriter log = new PrintWriter(OUTPUT_FILE_NAME, OUTPUT_ENCODING)) {
            logger.log(Level.FINE, "[{0}] barrier overcome", myNumber);
            while (true) {
                logger.log(Level.FINE, "[{0}] main loop", myNumber);
                mode.lock();
                try {
                    while (!exited && !started) {
                        logger.log(Level.FINE, "[{0}] waiting for start", myNumber);
                        doSomething.await();
                    }
                } finally {
                    mode.unlock();
                }
                if (exited) {
                    logger.log(Level.FINE, "[{0}] exitting", myNumber);
                    return 0;
                }
                int value = generator.produceNext();
                output.put(value);
                log.println(value);
                log.flush();
                logger.log(Level.FINE, "[{0}] Value {1} produced", new Object[] {myNumber, value});
            }
        } catch (InterruptedException e) {
            // Nothing to do. Exit.
        } catch (Exception e) {
            logger.log(Level.SEVERE, "[" + myNumber + "] " + e.getMessage(), e);
        }
        return 0;
    }

    /**
     * Приостановить генерацию значений
     * 
     * @throws InterruptedException при прерывании потока во время ожидания
     */
    public void stopWork() throws InterruptedException {
        if (started) {
            output.put(ProtocolConstants.STOP_SIGN);
        }
        setStarted(false);
    }

    /**
     * Начать или возобновить генерацию значений
     * 
     * @throws InterruptedException при прерывании потока во время ожидания
     */
    public void startWork() throws InterruptedException {
        if (!started) {
            output.put(ProtocolConstants.START_SIGN);
        }
        setStarted(true);
    }

    private void setStarted(boolean newStarted) {
        mode.lock();
        try {
            this.started = newStarted;
            doSomething.signal();
        } finally {
            mode.unlock();
        }
    }

    @Override
    public void exitWork() {
        mode.lock();
        try {
            try {
                output.put(ProtocolConstants.EXIT_SIGN);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            super.exitWork();
            doSomething.signal();
        } finally {
            mode.unlock();
        }
    }
}
