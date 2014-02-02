package ru.innova.task.network;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Реализация для объектов, которые получают данные от клиента.
 * 
 * @author sergey
 */
public class ServerNetworkReceiver extends AbstractNetworkReceiver {
    
    /**
     * Создает новый объект.
     * 
     * @param socket сокет, полученный методом {@link ServerSocket#accept()}.
     * @param clientType тип подключенного клиента.
     * @throws IOException в случае ошибки связи
     */
    public ServerNetworkReceiver(Socket socket, int clientType) throws IOException {
        super(clientType);
        this.socket = socket;
        this.input = new DataInputStream(socket.getInputStream());
    }

    @Override
    public void connect() throws IOException {
        // always connected
    }
}
