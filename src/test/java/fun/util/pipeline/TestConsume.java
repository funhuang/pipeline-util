package fun.util.pipeline;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TestConsume {

    static ExecutorService pool = Executors.newFixedThreadPool(10);

    public static void main(String[] args) {
        try {
            MultiConsumePipeline<String> queue = new MultiConsumePipeline<>(10);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        for (int i = 0; i < 10000; i++) {
                            queue.add(i + "iiiiiiiii");
                        }
                        queue.finish();
                    } catch (Exception e) {
                    }
                }
            }).start();

            for (int i = 0; i < 10; i++) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String name = Thread.currentThread().getName();
                        try {
                            while (true) {
                                String s = queue.take();
                                if (s == null) {
                                    break;
                                }
                                // System.out.println(name + "---------" + s);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        System.out.println(name);
                        queue.consumed();
                    }
                }).start();
            }
            queue.await();
            System.out.println("------------------------------------");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
