package ru.innova.task.common;

/**
 * Заглушка для интерфейса {@link DataController}. Методы этого класса ничего не делают.
 * Данный класс применяется там, где требуется иметь объект класа {@link DataController},
 * но нет задачи контролировать соответствие входящих и исходящих данных.
 * 
 * @author sergey
 */
public class DummyDataController implements DataController {
    private static DummyDataController instance = null;
    
    @Override
    public void saved(Integer value) {
        // do nothing
    }
    
    @Override
    public void produced(Integer value) {
        // do nothing
    }
    
    @Override
    public boolean areAllSaved() {
        return true;
    }

    /**
     * Получить экземпляр singleton
     * 
     * @return экземпляр singleton
     */
    public static DummyDataController getInstance() {
        if (instance == null) {
            instance = new DummyDataController();
        }
        return instance;
    }
}
