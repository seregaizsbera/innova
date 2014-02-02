package ru.innova.task.proxy;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;

import ru.innova.task.common.AbstractWorker;
import ru.innova.task.common.DummyDataController;
import ru.innova.task.common.ProtocolConstants;
import ru.innova.task.common.Receiver;
import ru.innova.task.common.Sender;
import ru.innova.task.network.ServerNetworkReceiver;
import ru.innova.task.network.ServerNetworkSender;

/**
 * Обработчик, получающий новые установленные соедиения.
 * Сервер создает экземпляр класса <code>ConnectionHandler</code> для каждого нового соединения.
 * Обработчик считывает тип клиента и тип подключения, на основании этих значений
 * создает обработчик нужного класса и передает управление ему. 
 * 
 * @author sergey
 */
public class ConnectionHandler implements Callable<Integer> {
    private final Socket socket;
    private final Logger logger;
    private final ProxyServer.Queues queues;
    private final CountDownLatch dummySignal;
    
    /**
     * Создает объект.
     * 
     * @param socket сокет, полученный из {@link ServerSocket#accept()}
     * @param queues набор очередей
     * @param dummySignal синхронизационный объект, который нужен обработчикам при старте
     */
    public ConnectionHandler(Socket socket, ProxyServer.Queues queues, CountDownLatch dummySignal) {
        this.socket = socket;
        this.queues = queues;
        this.dummySignal = dummySignal;
        this.logger = Logger.getLogger("proxy.Handler");
    }

    @Override
    public Integer call() throws Exception {
        try {
            InputStream in = socket.getInputStream();
            DataInputStream input = new DataInputStream(in);
            if (socket.isClosed()) {
                return 0;
            }
            int clientType = input.readInt();
            int connectorType = input.readInt();
            AbstractWorker worker = makeWorker(clientType, connectorType);
            return worker.call();
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        } finally {
            socket.close();
        }
        return 0;
    }

    private AbstractWorker makeWorker(int clientType, int connectorType) throws IOException {
        AbstractWorker worker;
        if (clientType == ProtocolConstants.INITIATOR_SIGN) {
            if (connectorType == ProtocolConstants.RECEIVER_SIGN) {
                worker = new Sender(queues.toInitiator, new ServerNetworkSender(socket, clientType), DummyDataController.getInstance(), "proxy.Sender", dummySignal);
            } else if (connectorType == ProtocolConstants.SENDER_SIGN) {
                worker = new Receiver(new ServerNetworkReceiver(socket, clientType), queues.toEcho, DummyDataController.getInstance(), "proxy.Receiver", dummySignal);
            } else {
                throw new IllegalStateException(String.format("Incorrect connector type received (%d)", connectorType));
            }
        } else if (clientType == ProtocolConstants.ECHO_SIGN) {
            if (connectorType == ProtocolConstants.RECEIVER_SIGN) {
                worker = new Sender(queues.toEcho, new ServerNetworkSender(socket, clientType), DummyDataController.getInstance(), "proxy.Sender", dummySignal);
            } else if (connectorType == ProtocolConstants.SENDER_SIGN) {
                worker = new Receiver(new ServerNetworkReceiver(socket, clientType), queues.toInitiator, DummyDataController.getInstance(), "proxy.Receiver", dummySignal);
            } else {
                throw new IllegalStateException(String.format("Incorrect connector type received (%d)", connectorType));
            }
        } else {
            throw new IllegalStateException(String.format("Incorrect server type received (%d)", clientType));
        }
        return worker;
    }
}
