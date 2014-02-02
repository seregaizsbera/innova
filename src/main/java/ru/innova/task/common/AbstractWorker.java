package ru.innova.task.common;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Класс, содержащий общие для обычных потоков данные. В основном, эти данные нужны для того,
 * чтобы отличать потоки друг от друга.
 * Класс реализует интерфейс {@link Callable}, чтобы его было удобно использовать в объектах {@link ExecutorService}.
 * 
 * @author sergey
 */
abstract public class AbstractWorker implements Callable<Integer> {
    /**
     * Номер потока. Рекомендуется у потоков одного типа задавать разные номера.
     * Номер используется для вывода в лог.
     */
    protected final int myNumber;
    /**
     * Логгер, в который можно писать сообщения. Имя логгера берется из поля {@link #name}.
     */
    protected final Logger logger;
    /**
     * Имя потока. Оно же имя логгера.
     */
    protected final String name;
    /**
     * Флаг, указывающий на то, что потоку необходимо завершиться.
     */
    protected volatile boolean exited;

    /**
     * Создает объект с указанными параметрами.
     * 
     * @param number номер потока
     * @param name имя потока
     */
    protected AbstractWorker(int number, String name) {
        this.myNumber = number;
        this.name = name;
        this.logger = Logger.getLogger(name);
    }

    /**
     * Отмечает, что поток должен завершиться, устанавливая поле {@link #exited} в <code>true</code>.
     * Объекты данного класса должны периодически
     * проверять поле {@link #exited}, и если оно равно <code>true</code>, то работу надо
     * как можно быстрее завершить.
     */
    public void exitWork() {
        logger.log(Level.FINE, "[{0}] exit request received", myNumber);
        this.exited = true;
    }
}
