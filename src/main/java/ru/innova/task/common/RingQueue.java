package ru.innova.task.common;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;


/**
 * <p>
 * Блокирующая очередь, которая выдает элементы по порядку.
 * В очереди хранятся целые числа. Помещать числа можно вразнобой, но выходить
 * из очереди они будут последовательно.
 * </p>
 * 
 * <p>
 * Очередь рассчитывает на то, что рано или поздно придут все числа начиная с 1-го.
 * По умолчанию 1-е ожидаемое число равно 0. Если придет число меньше первого
 * или одно и то же число придет 2 раза подряд, то метод put() выбросит исключение
 * {@linkplain IndexOutOfBoundsException}.
 * </p>
 * 
 * <p>Метод put() неблокирующий. Если в очереди нет места для сохранения нового элемента,
 * то внутренний массив будет увеличен в 2 раза.
 * </p>
 * 
 * Для простоты реализованы только те методы, которые нужны для решения задания.
 * <ul>
 *  <li>{@link #put(Integer)}</li>
 *  <li>{@link #take()}</li>
 *  <li>{@link #poll()}</li>
 *  <li>{@link #size()}</li>
 *  <li>{@link #isEmpty()}</li>
 * </ul>
 * 
 * Остальные методы выбрасывают исключение {@linkplain UnsupportedOperationException}.
 * 
 * @author sergey
 */
public class RingQueue implements BlockingQueue<Integer> {
    private static final int ABSENT = 0;
    private static final int PRESENT = 1;
    private final ReentrantLock lock;
    private final Condition canTake;
    private volatile AtomicIntegerArray buf;
    private volatile int bufSize;
    private volatile int min;
    private volatile int start;
    private volatile int count;
    
    /**
     * Конструктор по умолчанию. Создает очередь с начальным значением 0.
     */
    public RingQueue() {
        this(16, 0);
    }
    
    /**
     * Создает очередь с внутренним массивом указанного размера.
     * 
     * @param initialSize начальный размер буфера
     */
    public RingQueue(int initialSize) {
        this(initialSize, 0);
    }
    
    /**
     * Создает очередь с указанными параметрами.
     * 
     * @param initialSize начальный размер буфера
     * @param firstValue первое ожидаемое значение
     */
    public RingQueue(int initialSize, int firstValue) {
        this.bufSize = initialSize;
        this.buf = new AtomicIntegerArray(bufSize);
        for (int i = 0; i < bufSize; i++) {
            buf.set(i, ABSENT);
        }
        this.min = firstValue;
        this.start = 0;
        this.count = 0;
        this.lock = new ReentrantLock();
        this.canTake = lock.newCondition();
    }
    
    @Override
    public boolean isEmpty() {
        return count > 0;
    }
    
    @Override
    public int size() {
        return count;
    }
    
    /**
     * Положить новый элемент в очередь.
     * В отличие от метода {@linkplain BlockingQueue#put(Object)}, данная реализация
     * не является блокирующей. Если в очереди не хватает места для хранения новых данных,
     * то в ней увеличивается внутренний буфер.
     */
    @Override
    public void put(Integer value) {
        lock.lock();
        try {
            if (value == null) {
                throw new NullPointerException("Parameter value is null");
            }
            if (value < min) {
                throw new IndexOutOfBoundsException(String.format("Minimum number expected is %d, but value specified was %d", min, value));
            }
            if (value - min >= bufSize) {
                resize(value);
            }
            int position = (value - min + start) % bufSize;
            if (buf.compareAndSet(position, ABSENT, PRESENT)) {
                count++;
            } else {
                throw new IndexOutOfBoundsException(String.format("Number %d received second time", value));
            }
            if (value == min) {
                canTake.signal();
            }
        } finally {
            lock.unlock();
        }
    }
    
    @Override
    public Integer take() throws InterruptedException {
        lock.lock();
        try {
            while (!buf.compareAndSet(start, PRESENT, ABSENT)) {
                canTake.await();
            }
            start++;
            start %= bufSize;
            count--;
            return min++;
        } finally {
            lock.unlock();
        }
    }
    
    @Override
    public Integer poll(long timeout, TimeUnit timeUnit) throws InterruptedException {
        lock.lock();
        try {
            long nanos = timeUnit.toNanos(timeout);
            while (!buf.compareAndSet(start, PRESENT, ABSENT)) {
                if (nanos <= 0) {
                    return null;
                }
                nanos = canTake.awaitNanos(nanos);
            }
            start++;
            start %= bufSize;
            count--;
            return min++;
        } finally {
            lock.unlock();
        }
    }

    private void resize(int newMax) {
        int newCapacity = bufSize * 2;
        while (newCapacity <= newMax - min) {
            newCapacity *= 2;
        }
        AtomicIntegerArray newBuf = new AtomicIntegerArray(newCapacity);
        for (int i = start; i < bufSize; i++) {
            newBuf.set(i - start, buf.get(i));
        }
        for (int i = 0; i < start; i++) {
            newBuf.set(i + bufSize - start, buf.get(i));
        }
        for (int i = bufSize; i < newCapacity; i++) {
            newBuf.set(i, ABSENT);
        }
        this.buf = newBuf;
        this.bufSize = newCapacity;
        this.start = 0;
    }

    @Override
    public Integer remove() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Integer poll() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Integer element() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Integer peek() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<Integer> iterator() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object[] toArray() {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(Collection<? extends Integer> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean add(Integer e) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean offer(Integer e) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean offer(Integer e, long timeout, TimeUnit unit) throws InterruptedException {
        throw new UnsupportedOperationException();
    }

    @Override
    public int remainingCapacity() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean contains(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int drainTo(Collection<? super Integer> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int drainTo(Collection<? super Integer> c, int maxElements) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public String toString() {
        return buf.toString();
    }
}
