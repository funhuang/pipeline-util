package fun.util.pipeline;

import java.util.concurrent.CountDownLatch;

/**
 * 多个消费端管道
 * 
 * @author fun
 *
 * @param <T>
 */
public class MultiConsumePipeline<T> extends Pipeline<T> {

    private CountDownLatch countDown;

    public MultiConsumePipeline(Integer count) {
        if (count < 1) {
            throw new RuntimeException("消费者数量不能为0");
        }
        countDown = new CountDownLatch(count);
    }

    /**
     * 完成一个消费者，消费端调用
     */
    public void consumed() {
        countDown.countDown();
    }

    /**
	 * 等待消费结束
	 * 
	 * @throws InterruptedException
	 */
    public void await() throws InterruptedException {
        countDown.await();
    }

    @Override
    public void finish() {
        super.finish();
        signalNotEmpty();
    }

    @Override
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
            count.decrementAndGet();
        } finally {
            notEmpty.signal();
            takeLock.unlock();
        }
        return x;
    }
}
