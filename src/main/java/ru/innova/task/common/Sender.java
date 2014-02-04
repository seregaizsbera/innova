package ru.innova.task.common;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import ru.innova.task.network.NetworkSender;

public class Sender extends AbstractWorker {
    private static final int WAIT_TIMEOUT = 500;
    private static final AtomicInteger counter = new AtomicInteger(0);
    private final BlockingQueue<Integer> input;
    private final NetworkSender output;
    private final DataController controller;
    private final CountDownLatch startSignal;
    
    public Sender(BlockingQueue<Integer> input, NetworkSender output, DataController controller, String name, CountDownLatch startSignal) {
        super(counter.incrementAndGet(), name);
        this.input = input;
        this.output = output;
        this.controller = controller;
        this.startSignal = startSignal;
        this.exited = false;
        logger.log(Level.FINE, "[{0}] Sender.Sender()", myNumber);
    }

    @Override
    public Integer call() {
        logger.log(Level.FINE, "[{0}] init", myNumber);
        try {
            logger.log(Level.FINE, "[{0}] barrier overcome", myNumber);
            try {
                output.connect();
            } catch (IOException e) {
                logger.log(Level.SEVERE, "[" + myNumber + "] " + e.getMessage(), e);
                return 0;
            }
            startSignal.await();
            while (true) {
                if (!output.isConnected()) {
                    logger.log(Level.FINE, "[{0}] connection closed", myNumber);
                    return 0;
                }
                logger.log(Level.FINE, "[{0}] main loop", myNumber);
                Integer number = input.poll(WAIT_TIMEOUT, TimeUnit.MILLISECONDS);
                logger.log(Level.FINE, "[{0}] got number {1}", new Object[] {myNumber, number});
                if (number == null) {
                    logger.log(Level.FINE, "[{0}] got null", new Object[] {myNumber});
                    if (exited  && controller.areAllSaved()) {
                        logger.log(Level.FINE, "[{0}] exitting", myNumber);
                        return 0;
                    }
                } else {
                    send(number);
                }
            }
        } catch (InterruptedException e) {
            // Nothing to do. Exit.
        } catch (Exception e) {
            logger.log(Level.SEVERE, "[" + myNumber + "] " + e.getMessage(), e);
        } finally {
            try {
                output.close();
            } catch (IOException e) {
                logger.log(Level.SEVERE, "[" + myNumber + "] " + e.getMessage(), e);
            }
        }
        return 0;
    }

    private void send(int value) throws IOException {
        logger.log(Level.FINE, "[{0}] send({1})", new Object[] {myNumber, value});
        output.send(value);
    }
}
