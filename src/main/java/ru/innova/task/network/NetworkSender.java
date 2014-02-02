package ru.innova.task.network;

import java.io.IOException;

/**
 * Интерфейс, описывающий объекты, которые отправляют данные по сети.
 * 
 * @author sergey
 */
public interface NetworkSender extends NetworkWorker {
    
    /**
     * Отправить целое число по сети.
     * 
     * @param value целое число, которое надо отправить по сети
     * @throws IOException в случае ошибки
     */
    void send(int value) throws IOException;
    
}
