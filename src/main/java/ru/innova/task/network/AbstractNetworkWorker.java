package ru.innova.task.network;

import java.io.IOException;
import java.net.Socket;

/**
 * Реализация для объектов, которые отвечают за сетевое взаимодействие.
 * 
 * @author sergey
 */
abstract public class AbstractNetworkWorker implements NetworkWorker {
    /**
     * Тип подключенного клиента (proxy или echo).
     */
    protected final int clientType;
    
    /**
     * Сокет, который держит соединение
     */
    protected Socket socket;
    
    /**
     * Создает объект.
     * 
     * @param clientType тип подключенного клиента
     */
    public AbstractNetworkWorker(int clientType) {
        this.socket = null;
        this.clientType = clientType;
    }

    @Override
    public void close() throws IOException {
        if (socket != null) {
            try {
                socket.close();
            } finally {
                this.socket = null;
            }
        }
    }
    
    @Override
    public boolean isConnected() {
        return socket != null && socket.isConnected();
    }
}
