package fun.util.pipeline;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class TestLine {

    static int count = 0;

    public static void main(String[] args) {
		final Pipeline<String> line = new Pipeline<>();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String s = null;
					while (((s = line.take()) != null)) {
						System.out.println(s);
                    }
					System.out.println(line.size());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();


        try {
            Map<String, String> map = new HashMap<>();

            for (int i = 0; i < 1000000; i++) {
                map.put("" + i, "-----" + i);
				line.add("11111111" + i);
            }
			line.finish();
            for (Entry<String, String> entry : map.entrySet()) {
                entry.setValue(null);
            }
            System.out.println();
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("111111");
    }

}
