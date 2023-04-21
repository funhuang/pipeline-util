package fun.util.pipeline;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import fun.util.pipeline.LoopLine.LoopFunction;

public class TestLoop {

    static AtomicInteger c = new AtomicInteger();

    static ExecutorService pool = Executors.newFixedThreadPool(100);

	static LoopLine queue = new LoopLine();

    static {
        for (int i = 0; i < 100; i++) {
            pool.submit(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        try {
                            queue.take().apply();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }
    }


    public static void main(String[] args) {
        try {
            final int size = 1000;

            while (true) {
                final LoopFunction f = queue.new LoopFunction(1000) {
                    final AtomicInteger total = new AtomicInteger(0);
                    Random r = new Random();

                    @Override
                    public void apply() throws InterruptedException {
                        if (total.incrementAndGet() > size) {
                            return;
                        }
                        confirm();
                        try {
                            int t = r.nextInt(100);
                            Thread.sleep(t);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        String s = "---111111-----" + total.get();
                        System.out.println("---" + s + "------" + c.getAndIncrement());

                        countDown();
                    }
                };
                queue.add(f);
                Thread.sleep(100);
                f.waitEnd();

                System.out.println("-------------------------------------------------------------------");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
