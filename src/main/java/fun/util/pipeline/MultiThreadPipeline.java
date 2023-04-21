package fun.util.pipeline;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 线性管道，用于多生产者异步处理数据，结束后管道废弃
 * 
 * @author fun
 *
 * @param <T>
 */
public class MultiThreadPipeline<T> extends Pipeline<T> {

    private final AtomicInteger finish = new AtomicInteger();

    public MultiThreadPipeline(int threadCount) {
        finish.set(threadCount);
    }

    /**
     * 生产者结束
     */
    @Override
    public void finish() {
        if (finish.decrementAndGet() == 0) {
            try {
                takeLock.lockInterruptibly();
                notEmpty.signalAll();
                takeLock.unlock();
            } catch (Exception e) {
                logger.error("执行错误", e);
            }
        }
    }

    @Override
    public boolean isFinish() {
        return finish.get() == 0;
    }

    /**
	 * 管道中断，无法take和add，消费端调用
	 */
    @Override
    public void block() {
        block.set(true);
        finish.set(0);
        clear();
    }

    @Override
    public T take() throws InterruptedException {
        if (block.get()) {
            return null;
        }
        T x;
        takeLock.lockInterruptibly();
        try {
            while (count.get() == 0 && finish.get() != 0) {
                notEmpty.await();
            }
            if (count.get() == 0 && finish.get() == 0) {
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

}
