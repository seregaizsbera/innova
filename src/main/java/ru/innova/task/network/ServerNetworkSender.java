package ru.innova.task.network;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Реализация для объектов, которые отправляют данные от сервера клиенту.
 * 
 * @author sergey
 */
public class ServerNetworkSender extends AbstractNetworkSender {

    /**
     * Создает новый объект.
     * 
     * @param socket сокет, полученный методом {@link ServerSocket#accept()}.
     * @param clientType тип подключенного клиента.
     * @throws IOException в случае ошибки связи
     */
    public ServerNetworkSender(Socket socket, int clientType) throws IOException {
        super(clientType);
        this.socket = socket;
        this.output = new DataOutputStream(socket.getOutputStream());
    }

    @Override
    public void connect() throws IOException {
        // always connected
    }
}
