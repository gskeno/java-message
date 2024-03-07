package org.gskeno.kafka.example.test;

public class DaemonTest {

    public static void main(String[] args) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(3000);
                    System.out.println("前端线程");
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        // 只有守护线程时，进程可退出，所以不会输出
        thread.setDaemon(true);
        thread.start();
    }
}
