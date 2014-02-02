package ru.innova.task.common;

import java.util.concurrent.atomic.AtomicInteger;


/**
 * Реализация интерфейса {@link DataController} при помощи атомарных целых чисел.
 * 
 * @author sergey
 */
public class AtomicDataController implements DataController {
    protected final AtomicInteger lastProduced;
    protected final AtomicInteger lastSaved;

    /**
     * Создает объект данного класса
     */
    public AtomicDataController() {
        this.lastProduced = new AtomicInteger(ProtocolConstants.NONE_VALUE);
        this.lastSaved = new AtomicInteger(ProtocolConstants.NONE_VALUE);
    }
    
    @Override
    public void saved(Integer value) {
        for (int current = lastSaved.get(); ; current = lastSaved.get()) {
            if (value <= current) {
                return;
            }
            if (lastSaved.compareAndSet(current, value)) {
                return;
            }
        }
    }
    
    @Override
    public void produced(Integer value) {
        for (int current = lastProduced.get(); ; current = lastProduced.get()) {
            if (value <= current) {
                return;
            }
            if (lastProduced.compareAndSet(current, value)) {
                return;
            }
        }
    }
    
    @Override
    public boolean areAllSaved() {
        return lastSaved.get() >= lastProduced.get();
    }
    
    /**
     * Начать отсчет сначала
     */
    public void reset() {
        lastProduced.set(ProtocolConstants.NONE_VALUE);
        lastSaved.set(ProtocolConstants.NONE_VALUE);
    }
}
