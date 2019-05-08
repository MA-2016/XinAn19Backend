package org.luncert;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.alibaba.fastjson.JSON;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.luncert.IftopMessage.Flow;
import org.luncert.IftopMessage.FlowValue;
import org.luncert.IftopMessage.GroupValue;
import org.luncert.IftopMessage.Record;

@RunWith(JUnit4.class)
public class TestIftopMessageResolver {

    private String inputHeader = "interface: wlp3s0\n"
            + "IP address is: 113.54.225.57\n"
            + "MAC address is: 3c:95:09:51:61:2d\n"
            + "Listening on wlp3s0\n";

    private String inputItem = "   # Host name (port/service if enabled)            last 2s   last 10s   last 40s cumulative\n"
            + "--------------------------------------------------------------------------------------------\n"
            + "   1 10.207.123.243                           =>       480b       480b       1.8Kb       120B\n"
            + "     tsa01s07-in-f14.1e100.net                <=         0b         0b         0b         0B\n"
            + "   2 10.207.123.243                           =>       480b       480b       480b       120B\n"
            + "     tsa01s07-in-f14.1e100.net                <=         0b         0b         0b         0B\n"
            + "--------------------------------------------------------------------------------------------\n"
            + "Total send rate:                                       480b       480b       480b\n"
            + "Total receive rate:                                      0b         0b         0b\n"
            + "Total send and receive rate:                           480b       480b       480b\n"
            + "--------------------------------------------------------------------------------------------\n"
            + "Peak rate (sent/received/total):                       480b         0b       480b\n"
            + "Cumulative (sent/received/total):                      120B         0B       120B\n"
            + "============================================================================================\n";

    @Test
    public void testMatch() {
        Pattern p = Pattern.compile(IftopMessage.REGEX_FLOWS);
        Matcher m = p.matcher(inputHeader + inputItem);
        List<Flow> flows = new LinkedList<>();
        while (m.find()) {
            Record out = new Record(m.group("localAddr"),
                FlowValue.valueOf(m.group("localLast2s")),
                FlowValue.valueOf(m.group("localLast10s")),
                FlowValue.valueOf(m.group("localLast40s")),
                FlowValue.valueOf(m.group("localCumulative"))
            );
            Record in = new Record(m.group("remoteAddr"),
                FlowValue.valueOf(m.group("remoteLast2s")),
                FlowValue.valueOf(m.group("remoteLast10s")),
                FlowValue.valueOf(m.group("remoteLast40s")),
                FlowValue.valueOf(m.group("remoteCumulative"))
            );
            flows.add(new Flow(out, in));
        }
        Assert.assertEquals(flows.size(), 2);

        p = Pattern.compile(IftopMessage.REGEX_OTHERS);
        m = p.matcher(inputHeader + inputItem);

        Assert.assertTrue(m.find());
        Assert.assertEquals(m.group("tsLast2s"), "480b");
        Assert.assertEquals(m.group("tsLast10s"), "480b");
        Assert.assertEquals(m.group("tsLast40s"), "480b");
        Assert.assertFalse(m.find());
    }

    @Test
    public void testIftopMessage() {
        IftopMessage msg = IftopMessage.valueOf(inputHeader + inputItem);

        // Test JSON
        String jsonStr = JSON.toJSONString(msg);
        msg = JSON.parseObject(jsonStr, IftopMessage.class);

        GroupValue totalSend = msg.getTotalSend();
        Assert.assertTrue(totalSend.getLast2s().value() == 480d);
        Assert.assertTrue(totalSend.getLast10s().value() == 480d);
        Assert.assertTrue(totalSend.getLast40s().value() == 480d);

        GroupValue totalReceive = msg.getTotalReceive();
        Assert.assertTrue(totalReceive.getLast2s().value() == 0d);
        Assert.assertTrue(totalReceive.getLast10s().value() == 0d);
        Assert.assertTrue(totalReceive.getLast40s().value() == 0d);

        GroupValue totalSR = msg.getTotalSend();
        Assert.assertTrue(totalSR.getLast2s().value() == 480d);
        Assert.assertTrue(totalSR.getLast10s().value() == 480d);
        Assert.assertTrue(totalSR.getLast40s().value() == 480d);

        GroupValue cumulative = msg.getCumulative();
        Assert.assertTrue(cumulative.getLast2s().value() == 120d);
        Assert.assertTrue(cumulative.getLast10s().value() == 0d);
        Assert.assertTrue(cumulative.getLast40s().value() == 120d);

        GroupValue peakRate = msg.getPeakRate();
        Assert.assertTrue(peakRate.getLast2s().value() == 480d);
        Assert.assertTrue(peakRate.getLast10s().value() == 0d);
        Assert.assertTrue(peakRate.getLast40s().value() == 480d);
    }

    @Test
    public void testResolver() throws IOException, InterruptedException {
        RedirectStream rs = new RedirectStream();
        
        OutputStreamWriter writer = new OutputStreamWriter(rs.writePoint());
        IftopMessageResolver resolver = new IftopMessageResolver(rs.readPoint(), (msg) -> {
        });

        resolver.start();
        writer.write(inputHeader.toCharArray());
        for (int i = 0; i < 5; i++) {
            writer.write(inputItem.toCharArray());
            writer.flush();
            Thread.sleep(100);
        }
        resolver.stop();
        rs.close();

        // 确定写入RedirectStream的数据已经全部被消费了
        Assert.assertEquals(rs.readPoint().read(), -1);
    }

}