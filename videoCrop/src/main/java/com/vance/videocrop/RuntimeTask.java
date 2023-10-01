package com.vance.videocrop;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.CountDownLatch;

public class RuntimeTask extends Thread {

    CountDownLatch latch;
    String cmd;

    public RuntimeTask(CountDownLatch latch, String cmd) {
        this.latch = latch;
        this.cmd = cmd;
    }

    @Override
    public void run() {
        Process process = null;
        try {
            process = Runtime.getRuntime().exec(cmd);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
            process.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            process.destroy();
        }
        latch.countDown();
    }
}
