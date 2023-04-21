package fun.util.pipeline;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;

public class TestLineConsume {

    static ExecutorService pool = Executors.newFixedThreadPool(10);

    public static void main(String[] args) {
        try {
            while (true) {
				MultiConsumePipeline<String> line = new MultiConsumePipeline<>(10);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            for (int i = 0; i < 1000; i++) {
								line.add(i + "iiiiiiiii");
                            }
							line.finish();
                        } catch (Exception e) {
                        }
                    }
                }).start();

                ReentrantLock lock = new ReentrantLock();
                for (int i = 0; i < 10; i++) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            String name = Thread.currentThread().getName();
                            Random r = new Random();
                            int index = 0;
                            try {
                                out: while (true) {
                                    lock.lock();
                                    while (true) {
										String s = line.take();
                                        if (s == null) {
                                            break out;
                                        }
                                        index++;
                                        int t = r.nextInt(100);
                                        if (index == 100) {
                                            lock.unlock();
                                            Thread.sleep(t);
                                            System.out.println(Thread.currentThread().getName());
                                            index = 0;
                                            continue out;
                                        }
                                        // System.out.println(name + "---------"
                                        // +
                                        // s);
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            lock.unlock();
                            System.out.println(name);
							line.consumed();
                        }
                    }).start();
                }
				line.await();
                System.out.println("------------------------------------");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
