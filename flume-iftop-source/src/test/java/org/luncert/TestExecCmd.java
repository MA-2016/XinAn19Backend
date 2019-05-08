package org.luncert;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.ExecuteStreamHandler;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class TestExecCmd {

    private static class DefaultExecuteStreamHandler implements ExecuteStreamHandler {

        @Override
        public void setProcessErrorStream(InputStream arg0) throws IOException {

        }

        @Override
        public void setProcessInputStream(OutputStream arg0) throws IOException {

        }

        @Override
        public void setProcessOutputStream(InputStream arg0) throws IOException {
            pOut = arg0;
        }

        InputStream pOut;
        volatile boolean running;

        @Override
        public void start() throws IOException {
            if (pOut != null) {
                running = true;
                Thread t = new Thread(() -> {
                    try {
                        int n = 0;
                        byte[] buf = new byte[128];
                        while (running || n >= 0) {
                            n = pOut.read(buf);
                            if (n > 0) {
                                System.out.write(buf, 0, n);
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    synchronized (this) {
                        notifyAll();
                    }
                });
                t.setDaemon(true);
                System.out.println("> start");
                t.start();
            }
        }

        @Override
        public synchronized void stop() throws IOException {
            if (running) {
                running = false;
                try {
                    wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("> stop");
            }
        }

    }

    @Test
    public void test() throws ExecuteException, IOException, InterruptedException {
        // super authority is needed
        String line = "iftop -i wlp3s0 -t";
        CommandLine cmdLine = CommandLine.parse(line);
        DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();
        DefaultExecutor executor = new DefaultExecutor();
        executor.setExitValue(0);
        executor.setStreamHandler(new DefaultExecuteStreamHandler());
        executor.execute(cmdLine, resultHandler);
        resultHandler.waitFor(20000);
    }

}