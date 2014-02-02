package ru.innova.task.initiator;

import ru.innova.task.common.AtomicDataController;

/**
 * Данный класс добавляет к сверке данных, реализованной в {@link AtomicDataController},
 * генерацию значений.
 * 
 * @author sergey
 */
public class DataGenerator extends AtomicDataController {

    /**
     * Получить очередное значение
     * 
     * @return новое значение
     */
    public int produceNext() {
        return lastProduced.incrementAndGet();
    }
}
