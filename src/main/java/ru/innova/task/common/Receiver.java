package ru.innova.task.common;

import java.io.EOFException;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;

import ru.innova.task.network.NetworkReceiver;

/**
 * Класс-исполнитель, получающий данные из сети и помещающий их в очередь.
 * 
 * @author sergey
 */
public class Receiver extends AbstractWorker {
    private static volatile int counter = 0;
    private final BlockingQueue<Integer> output;
    private final DataController controller;
    private final NetworkReceiver input;
    private final CountDownLatch startSignal;

    /**
     * Создает новый экземпляр
     * 
     * @param input объект, получающий данные по сети
     * @param output очередь для сохранения полученных данных
     * @param controller объект, для сврерки данных, проходящих в различных направлениях
     * @param name имя потока
     * @param startSignal объект, через который поток дожидается момента старта
     */
    public Receiver(NetworkReceiver input, BlockingQueue<Integer> output, DataController controller, String name, CountDownLatch startSignal) {
        super(++counter, name);
        this.output = output;
        this.input = input;
        this.controller = controller;
        this.startSignal = startSignal;
        this.exited = false;
        logger.log(Level.FINE, "[{0}] Receiver.Receiver()", myNumber);
    }

    @Override
    public Integer call() {
        logger.log(Level.FINE, "[{0}] run()", myNumber);
        try {
            try {
                input.connect();
            } catch (IOException e) {
                logger.log(Level.SEVERE, "[" + myNumber + "] " + e.getMessage(), e);
                return 0;
            }
            logger.log(Level.FINE, "[{0}] barrier overcome", myNumber);
            startSignal.await();
            while (true) {
                if (!input.isConnected()) {
                    logger.log(Level.FINE, "[{0}] connection closed", myNumber);
                    return 0;
                }
                logger.log(Level.FINE, "[{0}] main loop", myNumber);
                Integer number = receive();
                logger.log(Level.FINE, "[{0}] got number {1}", new Object[] {myNumber, number});
                if (number == null) {
                    logger.log(Level.FINE, "[{0}] got null", myNumber);
                    if (exited && controller.areAllSaved()) {
                        logger.log(Level.FINE, "[{0}] exitting", myNumber);
                        return 0;
                    }
                } else {
                    if (number < 0) { // TODO move to proxy-specific class
                        if (number == ProtocolConstants.STOP_SIGN) {
                            System.out.println("The Initiator server has stopped.");
                        } else if (number == ProtocolConstants.START_SIGN) {
                            System.out.println("The Initiator server has started.");
                        } else if (number == ProtocolConstants.EXIT_SIGN) {
                            System.out.println("The Initiator server has exited.");
                        }
                    } else {
                        logger.log(Level.FINE, "[{0}] putting number {1}", new Object[] {myNumber, number});
                        output.put(number);
                        logger.log(Level.FINE, "[{0}] number {1} put", new Object[] {myNumber, number});
                    }
                }
            }
        } catch (InterruptedException e) {
            // Nothing to do. Exit.
        } catch (EOFException e) {
            // just exit
        } catch (Exception e) {
            logger.log(Level.SEVERE, "[" + myNumber + "] " + e.getMessage(), e);
        } finally {
            try {
                input.close();
            } catch (IOException e) {
                logger.log(Level.SEVERE, "[" + myNumber + "] " + e.getMessage(), e);
            }
        }
        return 0;
    }

    private Integer receive() throws IOException {
        return input.receive();
    }
}
 