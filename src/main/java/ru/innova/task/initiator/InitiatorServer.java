package ru.innova.task.initiator;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import ru.innova.task.common.AbstractWorker;
import ru.innova.task.common.InteractionHelper;
import ru.innova.task.common.ProtocolConstants;
import ru.innova.task.common.Receiver;
import ru.innova.task.common.RingQueue;
import ru.innova.task.common.Saver;
import ru.innova.task.common.Sender;
import ru.innova.task.network.ClientNetworkReceiver;
import ru.innova.task.network.ClientNetworkSender;
import ru.innova.task.network.NetworkReceiver;
import ru.innova.task.network.NetworkSender;

/**
 * Программа, реализающая Initiator Server в соответствии с задачей.
 * 
 * @author sergey
 */
public class InitiatorServer extends Thread {
    private static final String OUTPUT_FILE_NAME = "initiator_receieve.txt";
    private static final int MAX_SENDERS = 3;
    private static final int MAX_RECEIVERS = 3;
    private final Logger logger;
    private final Config config;
    private final List<Sender> senders;
    private final List<Receiver> receivers;
    private final BlockingQueue<Integer> toProxy;
    private final BlockingQueue<Integer> fromProxy;
    private final DataGenerator generator;
    private final ExecutorService threadPool;
    private AbstractWorker saver;
    private Producer producer;
    private Future<Integer> saverFuture;
    
    InitiatorServer(Config config) {
        this.config = config;
        this.receivers = new ArrayList<>(config.receiverThreads);
        this.senders = new ArrayList<>(config.senderThreads);
        this.toProxy = new SynchronousQueue<>();
        this.fromProxy = new RingQueue();
        this.generator = new DataGenerator();
        this.threadPool = Executors.newCachedThreadPool(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread result = new Thread(r);
                result.setDaemon(true);
                return result;
            }
        });
        this.logger = Logger.getLogger("initiator.Server");
    }
    
    @Override
    public void run() {
        try {
            prepare();
            doWork();
        } catch (InterruptedException e) {
            // Nothing to do. Exit.
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    private void doWork() throws InterruptedException, ExecutionException {
        InteractionHelper interactionHelper = new InteractionHelper();
        while (true) {
            String cmd = interactionHelper.prompt("Enter command: ", "help");
            if (cmd.equals("help")) {
                interactionHelper.showMessage("help - show this message\n"
                                        + "start - start sending numbers\n"
                                        + "stop - suspend sending numbers\n"
                                        + "exit - exit program");
            } else if (cmd.equals("start")) {
                producer.startWork();
            } else if (cmd.equals("stop")) {
                producer.stopWork();
            } else if (cmd.equals("exit")) {
                processExit();
                interactionHelper.showMessage("Bye!");
                break;
            }
        }
    }

    private void prepare() {
        CountDownLatch dummySignal = new CountDownLatch(0);
        for (int i = 0; i < config.receiverThreads; i++) {
            NetworkReceiver input = new ClientNetworkReceiver(config.host, config.port, ProtocolConstants.INITIATOR_SIGN);
            receivers.add(new Receiver(input, fromProxy, generator, "initiator.Receiver", dummySignal));
        }
        for (int i = 0; i < config.senderThreads; i++) {
            NetworkSender output = new ClientNetworkSender(config.host, config.port, ProtocolConstants.INITIATOR_SIGN);
            senders.add(new Sender(toProxy, output, generator, "initiator.Sender", dummySignal));
        }
        this.producer = new Producer(toProxy, generator);
        this.saver = new Saver(fromProxy, OUTPUT_FILE_NAME, generator, "initiator.Saver");
        for (Receiver receiver: receivers) {
            threadPool.submit(receiver);
        }
        for (Sender sender: senders) {
            threadPool.submit(sender);
        }
        this.saverFuture = threadPool.submit(saver);
        threadPool.submit(producer);
    }

    private void processExit() throws InterruptedException, ExecutionException {
        producer.stopWork();
        producer.exitWork();
        for (Sender sender: senders) {
            sender.exitWork();
        }
        for (Receiver receiver: receivers) {
            receiver.exitWork();
        }
        saver.exitWork();
        threadPool.shutdown();
        threadPool.awaitTermination(1000, TimeUnit.MILLISECONDS);
        saverFuture.get();
    }
    
    private static final class Config {
        String host;
        int port;
        int senderThreads;
        int receiverThreads;
        
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
        out.printf("Usage: java %s <proxy-server-host> <proxy-server-port> <sender-threads-count> <receiver-threads-count>\n", InitiatorServer.class.getName());
        return retval;
    }

    private static Config parseArguments(String[] args) {
        if (args.length != 4) {
            System.exit(usage(1));
        }
        Config config = new Config();
        int index = 0;
        config.host = args[index++];
        try {
            config.port = Integer.parseInt(args[index++]);
            config.senderThreads = Integer.parseInt(args[index++]);
            config.receiverThreads = Integer.parseInt(args[index++]);
        } catch (NumberFormatException e) {
            System.err.println(e.getMessage());
            System.exit(usage(2));
        }
        if (config.senderThreads <= 0 || config.senderThreads > MAX_SENDERS) {
            System.err.printf("Incorrect number of %s threads %d. Specify number greater than %d less or equal than %d\n", "sender", config.senderThreads, 0, MAX_SENDERS);
            System.exit(usage(3));
        }
        if (config.receiverThreads <= 0 || config.receiverThreads > MAX_RECEIVERS) {
            System.err.printf("Incorrect number of %s threads %d. Specify number greater than %d less or equal than %d\n", "receiver", config.receiverThreads, 0, MAX_RECEIVERS);
            System.exit(usage(3));
        }
        return config;
    }

    public static void main(String[] args) {
        try {
            Config config = parseArguments(args);
            InitiatorServer server = new InitiatorServer(config);
            server.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
