package fun.util.pipeline;

import java.util.concurrent.atomic.AtomicInteger;

public class TestThread {


    static AtomicInteger count = new AtomicInteger();

    public static void main(String[] args) {
        final DBQueue<String> queue = new DBQueue<String>(10);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String s = null;
                    int i = 0;
                    while (((s = queue.take()) != null)) {
                        count.getAndIncrement();
                        System.out.println(s + "-----" + queue.size() + "------" + i++);
                    }
                    queue.setEnd(true);
                    System.out.println(queue.size());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();


        try {
            for (int j = 0; j < 10; j++) {
                final int k = j;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(2);
							for (int i = 0; i < 1000; i++) {
                                queue.add("--------" + i);
                                throw new RuntimeException("11");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            // count.incrementAndGet();
                        } finally {
                            queue.finish();
                        }

                    }
                }).start();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        while (!queue.isFinish() || !queue.isEnd()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println(count);
    }

    protected static class DBQueue<E> extends MultiThreadPipeline<E> {

        public DBQueue(int threadCount) {
            super(threadCount);
        }

        private boolean end;

        public boolean isEnd() {
            return end;
        }

        public void setEnd(boolean end) {
            this.end = end;
        }

    }
}
