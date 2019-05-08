package org.luncert;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;

import com.alibaba.fastjson.JSON;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteStreamHandler;
import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.EventDeliveryException;
import org.apache.flume.PollableSource;
import org.apache.flume.conf.Configurable;
import org.apache.flume.event.EventBuilder;
import org.apache.flume.source.AbstractSource;
import org.eclipse.jetty.util.BlockingArrayQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Reference:
 * http://flume.apache.org/releases/content/1.9.0/FlumeDeveloperGuide.html#client-sdk
 * https://www.jianshu.com/p/fb9b2ff03475
 */
public class IftopSource extends AbstractSource implements Configurable, PollableSource {

    private static final Logger logger = LoggerFactory.getLogger(IftopMessage.class);

    private String interfaceName;

    @Override
    public void configure(Context ctx) {
        interfaceName = ctx.getString("iftopInterface");
        Objects.requireNonNull(interfaceName,
                "Parameter iftopInterface must be not null, please specify a valid value in the flume "
                        + "configuration, like: agent.source.s1.iftopInterface = enp2s0");

    }

    private BlockingArrayQueue<Event> eventQueue = new BlockingArrayQueue<>();
    private DefaultExecuteResultHandler resultHandler;

    @Override
    public synchronized void start() {
        CommandLine cmdLine = CommandLine.parse("iftop -t -i " + interfaceName);
        DefaultExecutor executor = new DefaultExecutor();
        resultHandler = new DefaultExecuteResultHandler();

        try {
            executor.setStreamHandler(new DefaultExecuteStreamHandler());
            executor.execute(cmdLine, resultHandler);
            super.start();
            logger.info("FlumeIftopSource: start successfully");
        } catch (IOException e) {
            logger.error("FlumeIftopSource: start failed", e);
        }

    }

    private class DefaultExecuteStreamHandler implements ExecuteStreamHandler {

        @Override
        public void setProcessInputStream(OutputStream arg0) throws IOException {
            // not implemented
        }

        @Override
        public void setProcessErrorStream(InputStream i) throws IOException {
            // not implemented
        }

        InputStream pOut;

        @Override
        public void setProcessOutputStream(InputStream i) throws IOException {
            pOut = i;
            Objects.requireNonNull(pOut, "Unexpected error, parameter InputStream is null value");
        }

        IftopMessageResolver resolver;

        @Override
        public void start() throws IOException {
            resolver = new IftopMessageResolver(pOut, (iftopMsg) -> {
                eventQueue.add(EventBuilder.withBody(
                    JSON.toJSONString(iftopMsg).getBytes()));
            });
            resolver.start();
            logger.info("FlumeIftopSource: IftopMessageResolver start successfully");
        }

        @Override
        public void stop() throws IOException {
            try {
                resolver.stop();
                logger.info("FlumeIftopSource: IftopMessageResolver stop successfully");
            } catch (InterruptedException e) {
                logger.error("FlumeIftopSource: IftopMessageResolver stop failed", e);
            }
        }

    }

    @Override
    public synchronized void stop() {
        try {
            resultHandler.waitFor(5000);
            super.stop();
            Exception ex = resultHandler.getException();
            if (ex == null) {
                logger.info("FlumeIftopSource: stop successfully with exit value = "
                    + resultHandler.getExitValue());
            } else {
                logger.error("FlumeIftopSource: stop failed", ex);
            }
        } catch (InterruptedException e) {
            logger.error("FlumeIftopSource: stop failed", e);
        }
    }

    @Override
    public Status process() throws EventDeliveryException {
        Status status = null;

        try {
            // Receive new data
            Event e = eventQueue.take();
    
            // Store the Event into this Source's associated Channel(s)
            getChannelProcessor().processEvent(e);
    
            status = Status.READY;
        } catch (Throwable t) {
            // If error occurs that the action of putting event to channel failed,
            // this event will be discarded

            // Log exception, handle individual exceptions as needed
    
            status = Status.BACKOFF;
    
            // re-throw all Errors
            if (t instanceof Error) {
            throw (Error)t;
            }
        }
        return status;
    }

    /**
     * Not used
     */
    @Override
    public long getBackOffSleepIncrement() {
        return 0;
    }

    /**
     * Not used
     */
    @Override
    public long getMaxBackOffSleepInterval() {
        return 0;
    }
    
}