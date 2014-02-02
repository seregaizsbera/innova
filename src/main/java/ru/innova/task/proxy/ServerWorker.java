package ru.innova.task.proxy;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;

import ru.innova.task.common.AbstractWorker;

/**
 * Поток, слушающий серверное соединение.
 * 
 * @author sergey
 */
public class ServerWorker extends AbstractWorker {
    private final int port;
    private final ExecutorService threadPool;
    private final CountDownLatch startSignal;
    private final CountDownLatch exitSignal;
    private final ProxyServer.Queues queues;
    private ServerSocket serverSocket;

    /**
     * Создает объект
     * 
     * @param port номер порта
     * @param startSignal объект для получения сигнала о старте
     * @param exitSignal объект для получения сигнала о выходе
     * @param threadPool пул потоков для обработчиков подключаемых соединений
     */
    public ServerWorker(int port, CountDownLatch startSignal, CountDownLatch exitSignal, ExecutorService threadPool) {
        super(1, "proxy.Server");
        this.port = port;
        this.threadPool = threadPool;
        this.startSignal = startSignal;
        this.exitSignal = exitSignal;
        this.queues = new ProxyServer.Queues();
        this.serverSocket = null;
    }

    @Override
    public Integer call() {
        try {
            startSignal.await();
            this.serverSocket = new ServerSocket(port);
            while (true) {
                if (exited) {
                    break;
                }
                final Socket socket = serverSocket.accept();
                threadPool.submit(new ConnectionHandler(socket, queues, startSignal));
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            exitSignal.countDown();
        } catch (InterruptedException e) {
            exitSignal.countDown();
            Thread.currentThread().interrupt();
        }
        return 0;
    }
    
    @Override
    public void exitWork() {
        super.exitWork();
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                logger.log(Level.SEVERE, e.getMessage(), e);
            }
        }
    }
}
