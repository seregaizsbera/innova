package ru.innova.task.proxy;

import java.io.PrintStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import ru.innova.task.common.InteractionHelper;

/**
 * Программа, реализающая Proxy Server в соответствии с задачей.
 * 
 * @author sergey
 */
public class ProxyServer extends Thread {
    private static final int OUTPUT_QUEUE_SIZE = 100000;
    private final Config config;
    private final Logger logger;
    private final ExecutorService threadPool;
    private final InteractionHelper interactionHelper;
    
    private ProxyServer(Config config) {
        this.config = config;
        this.logger = Logger.getLogger("proxy.Main");
        this.threadPool = Executors.newCachedThreadPool(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread result = new Thread(r);
                result.setDaemon(true);
                return result;
            }
        });
        this.interactionHelper = new InteractionHelper();
    }
    
    public static final class Queues {
        public final BlockingQueue<Integer> toInitiator;
        public final BlockingQueue<Integer> toEcho;
        
        public Queues() {
            this.toInitiator = new LinkedBlockingQueue<>(OUTPUT_QUEUE_SIZE);
            this.toEcho = new LinkedBlockingQueue<>(OUTPUT_QUEUE_SIZE);
        }
    }
    
    @Override
    public void run() {
        try {
            CountDownLatch startSignal = new CountDownLatch(1);
            CountDownLatch exitSignal = new CountDownLatch(1);
            Future<Integer> serverWorker = threadPool.submit(new ServerWorker(config.port, startSignal, exitSignal, threadPool));
            Future<Integer> controlThread = threadPool.submit(new ControlThread(startSignal, exitSignal));
            while (true) {
                if (exitSignal.await(500, TimeUnit.MILLISECONDS)) {
                    processExit();
                    return;
                }
                if (serverWorker.isDone()) {
                    interactionHelper.showError("Main server thread completed. Bye!");
                    processExit();
                    return;
                }
                if (controlThread.isDone()) {
                    interactionHelper.showError("Control thread completed. Bye!");
                    processExit();
                    return;
                }
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            processExit();
        }
    }

    private void processExit() {
        threadPool.shutdown();
        try {
            threadPool.awaitTermination(1000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            interrupt();
        }
        threadPool.shutdownNow();
    }

    private static final class Config {
        int port;
        
        Config() {
            // empty constructor
        }
    }
    

    private static int usage(int retval) {
        PrintStream out;
        if (retval == 0) {
            out = System.out;
        } else {
            out = System.err;
        }
        out.printf("Usage: java %s <port>\n", ProxyServer.class.getName());
        return retval;
    }
    
    private static Config parseArguments(String[] args) {
        if (args.length != 1) {
            System.exit(usage(1));
        }
        Config config = new Config();
        int index = 0;
        try {
            config.port = Integer.parseInt(args[index++]);
        } catch (NumberFormatException e) {
            System.err.println(e.getMessage());
            System.exit(usage(2));
        }
        return config;
    }

    public static void main(String[] args) {
        try {
            Config config = parseArguments(args);
            ProxyServer server = new ProxyServer(config);
            server.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
