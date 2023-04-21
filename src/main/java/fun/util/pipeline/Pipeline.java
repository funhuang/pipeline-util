package fun.util.pipeline;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 管道，用于异步处理数据，结束后管道废弃
 * 
 * @author fun
 *
 * @param <T>
 */
public class Pipeline<T> implements Collection<T> {

    protected final Logger logger = LogManager.getLogger(getClass());

    protected final AtomicInteger count = new AtomicInteger();

    protected final ReentrantLock takeLock = new ReentrantLock();

    protected final Condition notEmpty = takeLock.newCondition();

    protected final ReentrantLock putLock = new ReentrantLock();

    protected final Condition notFull = putLock.newCondition();

    protected final AtomicBoolean block = new AtomicBoolean(false);

    protected final AtomicBoolean finish = new AtomicBoolean(false);

    protected transient Node head;

    protected transient Node last;

    protected class Node {
        T item;
        Node next;

        Node(T t) {
            item = t;
        }
    }

    public Pipeline() {
        last = head = new Node(null);
    }

    /**
	 * 管道结束，不会继续add，生产端调用
	 */
    public void finish() {
        finish.set(true);
        try {
            takeLock.lock();
            notEmpty.signal();
        } finally {
            takeLock.unlock();
        }
    }

    public boolean isFinish() {
        return finish.get();
    }

    /**
	 * 管道中断，无法take和add，消费端调用
	 */
    public void block() {
        block.set(true);
        finish.set(true);
        clear();
    }

    public boolean isBlocked() {
        return block.get();
    }

    @Override
    public boolean add(T e) {
        Node node = new Node(e);
        putLock.lock();
        try {
            if (!finish.get() && !block.get()) {
                last = last.next = node;
                count.getAndIncrement();
            }
        } finally {
            putLock.unlock();
        }
        signalNotEmpty();

        return true;
    }

    @Override
    public boolean addAll(Collection<? extends T> es) {
        putLock.lock();
        try {
            for (T e : es) {
                if (finish.get() || block.get()) {
                    break;
                }
                Node node = new Node(e);
                last = last.next = node;
                count.getAndIncrement();
            }
        } finally {
            putLock.unlock();
        }
        signalNotEmpty();

        return true;
    }

    protected void signalNotEmpty() {
        takeLock.lock();
        try {
            if (count.get() > 0) {
                notEmpty.signal();
            }
        } finally {
            takeLock.unlock();
        }
    }

    public T take() throws InterruptedException {
        if (block.get()) {
            return null;
        }
        T x;
        takeLock.lockInterruptibly();
        try {
            while (count.get() == 0 && !finish.get()) {
                notEmpty.await();
            }
            if (count.get() == 0 && finish.get()) {
                return null;
            }
            if (block.get()) {
                return null;
            }
            x = dequeue();
            int c = count.getAndDecrement();
            if (c > 1) {
                notEmpty.signal();
            }
        } finally {
            takeLock.unlock();
        }
        return x;
    }

    protected T dequeue() {
        Node h = head;
        Node first = h.next;
        h.next = h;
        head = first;
        T x = first.item;
        first.item = null;
        return x;
    }

    @Override
    public int size() {
        return count.get();
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public void clear() {
        fullyLock();
        try {
            for (Node p, h = head; (p = h.next) != null; h = p) {
                h.next = h;
                p.item = null;
            }
            head = last;
            count.set(0);
        } finally {
            fullyUnlock();
        }
    }

    private void fullyLock() {
        putLock.lock();
        takeLock.lock();
    }

    private void fullyUnlock() {
        takeLock.unlock();
        putLock.unlock();
    }


    @Override
	public final boolean removeAll(Collection<?> arg0) {
        clear();
        return false;
    }

    @Override
	public final boolean contains(Object arg0) {
        throw new RuntimeException("不支持的方法");
    }

    @Override
	public final Iterator<T> iterator() {
        throw new RuntimeException("不支持的方法");
    }

    @Override
	public final Object[] toArray() {
        throw new RuntimeException("不支持的方法");
    }

    @Override
	public final <E> E[] toArray(E[] arg0) {
        throw new RuntimeException("不支持的方法");
    }

    @Override
	public final boolean remove(Object arg0) {
        throw new RuntimeException("不支持的方法");
    }

    @Override
	public final boolean containsAll(Collection<?> arg0) {
        throw new RuntimeException("不支持的方法");
    }

    @Override
	public final boolean retainAll(Collection<?> arg0) {
        throw new RuntimeException("不支持的方法");
    }
}
