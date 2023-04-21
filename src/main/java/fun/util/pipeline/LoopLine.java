package fun.util.pipeline;

import java.util.concurrent.CountDownLatch;

import fun.util.pipeline.LoopLine.LoopFunction;

/**
 * 循环管道
 * 
 * @author fun
 *
 * @param <T>
 */
public class LoopLine extends Pipeline<LoopFunction> {

    public abstract class LoopFunction {

        private CountDownLatch countDown = null;

        public LoopFunction(int count) {
            countDown = new CountDownLatch(count);
        }

        protected void countDown() {
            countDown.countDown();
        }

        public void waitEnd() throws InterruptedException {
            countDown.await();
        }

        /**
		 * 需要调用这个接口以后管道中才能重新获取到
		 */
        public void confirm() {
            LoopLine.this.add(this);
        }

        public abstract void apply() throws InterruptedException;

    }

    @Override
    protected void signalNotEmpty() {
        takeLock.lock();
        try {
            notEmpty.signal();
        } finally {
            takeLock.unlock();
        }
    }

    @Override
    public boolean add(LoopFunction item) {
        Node node = new Node(item);
        putLock.lock();

        try {
            last = last.next = node;
            count.getAndIncrement();
        } finally {
            putLock.unlock();
        }

        signalNotEmpty();

        return true;
    }

    @Override
    public LoopFunction take() throws InterruptedException {
        takeLock.lockInterruptibly();

        LoopFunction item;
        try {
            while (count.get() == 0) {
                notEmpty.await();
            }

            item = dequeue();
            int c = count.getAndDecrement();
            if (c > 1) {
                notEmpty.signal();
            }
        } finally {
            takeLock.unlock();
        }

        return item;
    }

}
