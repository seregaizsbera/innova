package ru.innova.task.network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import ru.innova.task.common.ProtocolConstants;

/**
 * Реализация для объектов, которые получают данные от сервера.
 * 
 * @author sergey
 */
public class ClientNetworkReceiver extends AbstractNetworkReceiver {
    private final String host;
    private final int port;
    
    /**
     * Создает новый объект.
     * 
     * @param host имя или адрес узла
     * @param port номер порта
     * @param clientType тип клиента
     */
    public ClientNetworkReceiver(String host, int port, int clientType) {
        super(clientType);
        this.host = host;
        this.port = port;
    }

    @Override
    public void connect() throws IOException {
        if (isConnected()) {
            throw new IOException("Already connected");
        }
        while (!isConnected()) {
            try {
                connectInternal();
            } catch (IOException e) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e1) {
                    Thread.currentThread().interrupt();
                    throw new IOException(e1);
                }
            }
        }
    }

    private void connectInternal() throws UnknownHostException, IOException {
        this.socket = new Socket(host, port);
        InputStream in = socket.getInputStream();
        this.input = new DataInputStream(in);
        OutputStream out = socket.getOutputStream();
        DataOutputStream output = new DataOutputStream(out);
        output.writeInt(clientType);
        output.writeInt(ProtocolConstants.RECEIVER_SIGN);
    }
}
