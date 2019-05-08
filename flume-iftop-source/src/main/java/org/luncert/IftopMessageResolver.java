package org.luncert;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Objects;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Resolve IftopMessage from byte stream
 */
public class IftopMessageResolver {

    private static final Logger logger = LoggerFactory.getLogger(IftopMessageResolver.class);

    private InputStream inputStream;
    private Consumer<IftopMessage> consumer;
    private volatile boolean running;

    public IftopMessageResolver(InputStream inputStream, Consumer<IftopMessage> consumer) {
        Objects.requireNonNull(inputStream);
        this.inputStream = inputStream;
        this.consumer = consumer;
    }

    public void start() {
        running = true;
        Thread t = new Thread(() -> {
            try {
                int n = 0;
                byte[] buf = new byte[512];
                while (running || n >= 0) {
                    n = inputStream.read(buf);
                    if (n > 0) {
                        process(buf, n);
                    }
                }
            } catch (Exception e) {
                logger.error("FlumeIftopSource: got error while reading data", e);
            }
            running = false;
            synchronized (this) {
                notifyAll();
            }
        });
        t.start();
    }

    // 统计连续的'='数量，达到3时表示读到message末尾了，可以提取IftopMessage了
    private int divisionCount = 0;
    // 在刚提取过IftopMessage后有一段时间读的都是'='，这段时间会反复出现divisionCount=3的情况，所以这段时间禁止提取IftopMessage
    private boolean receivingMessage;
    private ByteArrayOutputStream buffer = new ByteArrayOutputStream();

    private void process(byte[] buf, int len) throws Exception {
        for (int i = 0; i < len; i++) {
            byte b = buf[i];
            if (b == '=') {
                if (receivingMessage) {
                    divisionCount++;
                    buffer.write(b);
                    if (divisionCount == 3) {
                        divisionCount = 0;
                        receivingMessage = false;
                        // substract data from buf to fill message
                        String raw = new String(buffer.toByteArray());
                        buffer.reset();
                        consumer.accept(IftopMessage.valueOf(raw));
                    }
                }
            } else {
                divisionCount = 0;
                receivingMessage = true;
                buffer.write(b);
            }
        }
    }

    public synchronized void stop() throws InterruptedException {
        if (running) {
            running = false;
            wait();
        }
    }

}