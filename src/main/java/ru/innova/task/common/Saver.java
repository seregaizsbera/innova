package ru.innova.task.common;

import java.io.PrintWriter;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import ru.innova.task.common.AbstractWorker;
import ru.innova.task.common.DataController;

/**
 * Класс-исполнитель, получающий данные из очереди и помещающий их в файл.
 * 
 * @author sergey
 */
public class Saver extends AbstractWorker {
    private static final String OUTPUT_ENCODING = "UTF-8";
    private final BlockingQueue<Integer> input;
    private final DataController controller;
    private final String outputFileName;

    /**
     * Создает новый экземпляр класса.
     * 
     * @param input входная очередь
     * @param outputFileName имя выходного файла
     * @param controller объект для сверки данных, передаваемых в различных направлениях
     * @param name имя потока
     */
    public Saver(BlockingQueue<Integer> input, String outputFileName, DataController controller, String name) {
        super(1, name);
        this.input = input;
        this.controller = controller;
        this.exited = false;
        this.outputFileName = outputFileName;
        logger.log(Level.FINE, "[{0}] Saver.Saver()", myNumber);
    }

    @Override
    public Integer call() {
        logger.log(Level.FINE, "[{0}] init", myNumber);
        try (PrintWriter output = new PrintWriter(outputFileName, OUTPUT_ENCODING)) {
            logger.log(Level.FINE, "[{0}] barrier overcome", myNumber);
            while (true) {
                logger.log(Level.FINE, "[{0}] main loop", myNumber);
                Integer number = input.poll(500, TimeUnit.MILLISECONDS);
                logger.log(Level.FINE, "[{0}] got number {1}", new Object[] {myNumber, number});
                if (number == null) {
                    logger.log(Level.FINE, "[{0}] got null", myNumber);
                    if (exited && controller.areAllSaved()) {
                        logger.log(Level.FINE, "[{0}] exiting", myNumber);
                        return 0;
                    }
                } else {
                    output.println(number);
                    output.flush();
                    logger.log(Level.FINE, "[{0}] saved number {1}", new Object[] {myNumber, number});
                    controller.saved(number);
                }
            }
        } catch (InterruptedException e) {
            // Nothing to do. Exit.
        } catch (Exception e) {
            logger.log(Level.SEVERE, "[" + myNumber + "] " + e.getMessage(), e);
        }
        return 0;
    }
}
