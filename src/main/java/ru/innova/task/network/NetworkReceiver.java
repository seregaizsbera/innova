package ru.innova.task.network;

import java.io.IOException;

/**
 * Интерфейс, описывающий объекты, которые получают данные по сети.
 * 
 * @author sergey
 */
public interface NetworkReceiver extends NetworkWorker {

    /**
     * Получить из сетевого соединения очередное целое число.
     * 
     * @return очередное целое число, полученное по сети
     * @throws IOException в случае ошибки
     */
    int receive() throws IOException;

}
