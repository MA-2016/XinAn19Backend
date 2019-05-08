package org.luncert;

import org.apache.flume.Context;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

// TODO: it seems imposible to test IftopSource with Junit framework
@RunWith(JUnit4.class)
public class TestIftopSource {

    @Test
    public void test() {
        IftopSource source = new IftopSource();

        // Mock context
        Context ctx = new Context();
        ctx.put("iftopInterface", "wlp3s0");

        source.configure(ctx);
        // source.setChannelProcessor(new ChannelProcessor());
    }

}