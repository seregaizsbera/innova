package ru.innova.task.network;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Реализация для объектов, которые отправляют данные по сети.
 * 
 * @author sergey
 */
abstract public class AbstractNetworkSender extends AbstractNetworkWorker implements NetworkSender {
    protected DataOutputStream output;
    
    /**
     * Создает объект.
     * 
     * @param clientType тип подключенного клиента.
     */
    public AbstractNetworkSender(int clientType) {
        super(clientType);
        this.output = null;
    }

    @Override
    public void send(int value) throws IOException {
        if (!isConnected()) {
            throw new IOException("Not connected");
        }
        output.writeInt(value);
    }
}
