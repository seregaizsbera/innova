package ru.innova.task.network;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * Реализация для объектов, которые получают данные по сети.
 * 
 * @author sergey
 */
abstract public class AbstractNetworkReceiver extends AbstractNetworkWorker implements NetworkReceiver {
    protected DataInputStream input;
    
    /**
     * Создает объект
     * 
     * @param clientType тип клиента
     */
    public AbstractNetworkReceiver(int clientType) {
        super(clientType);
        this.input = null;
    }

    @Override
    public int receive() throws IOException {
        if (!isConnected()) {
            throw new IOException("Not connected");
        }
        return input.readInt();
    }
}
