package ru.innova.task.network;

import java.io.Closeable;
import java.io.IOException;

/**
 * Интерфейс, описывающий объекты, которые отвечают за сетевое взаимодействие.
 * 
 * @author sergey
 */
public interface NetworkWorker extends Closeable {

    /**
     * Установить соединение
     * 
     * @throws IOException в случае ошибки
     */
    void connect() throws IOException;
    
    /**
     * Узнать, установлено ли соединение.
     * 
     * @return <code>true</code>, если соединение установлено
     */
    boolean isConnected();

}
