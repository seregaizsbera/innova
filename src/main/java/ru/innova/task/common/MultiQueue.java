package ru.innova.task.common;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;


/**
 * <p>
 * Блокирующая очередь, которая перекладывает элементы в несколько других
 * очередей.
 * </p>
 * 
 * <p>
 *  Данный класс предназначен для того, чтобы объект,
 *  который что-то помещает данные в очередь, положил их не в одну очередь, а
 *  в несколько. 
 * </p>
 * 
 * <p>Поскольку под данной очередью лежит несколько внутренних очередей, из нее
 *  нельзя ничего извлечь. Из методов, помещающих элементы, для простоты
 *  реализованы только те, которые нужны для решения задания.
 * </p>
 * <ul>
 *  <li>{@link #put(Integer)}</li>
 * </ul>
 * 
 * Остальные методы выбрасывают исключение {@linkplain UnsupportedOperationException}.
 * 
 * @author sergey
 */
public class MultiQueue implements BlockingQueue<Integer> {
    private final List<BlockingQueue<Integer>> queues;
    
    @SafeVarargs
    public MultiQueue(BlockingQueue<Integer>... queues) {
        this.queues = Arrays.asList(queues);
    }
    
    /**
     * Положить новый элемент в очередь.
     * В отличие от метода {@linkplain BlockingQueue#put(Object)}, данная реализация
     * не является блокирующей. Если в очереди не хватает места для хранения новых данных,
     * то в ней увеличивается внутренний буфер.
     */
    @Override
    public void put(Integer value) throws InterruptedException {
        for (BlockingQueue<Integer> queue: queues) {
            queue.put(value);
        }
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
    public boolean isEmpty() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int size() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Integer take() throws InterruptedException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Integer poll(long timeout, TimeUnit unit) throws InterruptedException {
        throw new UnsupportedOperationException();
    }
}
