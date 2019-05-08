package org.luncert;

import java.security.InvalidParameterException;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
public class IftopMessage {

    @Data
    @AllArgsConstructor
    public static class Flow {
        private Record out, in;
    }

    @Data
    @AllArgsConstructor
    public static class Record {
        private String ip;
        private FlowValue last2s, last10s, last40s, cumulative;
    }

    @Data
    @AllArgsConstructor
    public static class FlowValue {
        private double value;
        private FlowUnit unit;

        public static FlowValue valueOf(String raw) {
            char c;
            int i = 0, limit = raw.length();
            for (; i < limit &&
                ((c = raw.charAt(i)) >= '0' && c <= '9' || c == '.'); i++){}
            
            if (i == 0) {
                throw new InvalidParameterException("string parameter must start with a number, eg: 104B, source: " + raw);
            } else if (i == limit) {
                throw new InvalidParameterException("string parameter must end with a unit literal, eg: 104B, source: " + raw);
            }

            return new FlowValue(Double.valueOf(raw.substring(0, i)),
                FlowUnit.valueOf(raw.substring(i)));
        }

        public double value() { return value; }
        public FlowUnit unit() { return unit; }
    }

    public static enum FlowUnit {
        TB, GB, MB, KB, B, Tb, Gb, Mb, Kb, b
    }

    @Data
    @AllArgsConstructor
    public static class GroupValue {
        private FlowValue last2s, last10s, last40s;
    }

    private List<Flow> flows;
    private GroupValue totalSend, totalReceive, totalSendReceive, peakRate, cumulative;

    static final String WHITESPACE = "(\n|.)*?";
    static final String REGEX_FLOWS = "(?<flowID>[\\d]+?)[ ]+(?<localAddr>[\\d\\w\\.-]+?)[ ]+=>[ ]+(?<localLast2s>\\d[\\d\\w\\.]+)[ ]+(?<localLast10s>\\d[\\d\\w\\.]+)[ ]+(?<localLast40s>\\d[\\d\\w\\.]+)[ ]+(?<localCumulative>\\d[\\d\\w\\.]+)" +
        WHITESPACE + "(?<remoteAddr>[\\d\\w\\.-]+?)[ ]+<=[ ]+(?<remoteLast2s>\\d[\\d\\w\\.]+)[ ]+(?<remoteLast10s>\\d[\\d\\w\\.]+)[ ]+(?<remoteLast40s>\\d[\\d\\w\\.]+)[ ]+(?<remoteCumulative>\\d[\\d\\w\\.]+)"; 
    static final String REGEX_OTHERS =
        "Total.*?(?<tsLast2s>\\d[\\d\\w\\.]+)[ ]+(?<tsLast10s>\\d[\\d\\w\\.]+)[ ]+(?<tsLast40s>\\d[\\d\\w\\.]+)" + WHITESPACE + // Total send rate
        "Total.*?(?<trLast2s>\\d[\\d\\w\\.]+)[ ]+(?<trLast10s>\\d[\\d\\w\\.]+)[ ]+(?<trLast40s>\\d[\\d\\w\\.]+)"+ WHITESPACE + // Total receive rate
        "Total.*?(?<tsrLast2s>\\d[\\d\\w\\.]+)[ ]+(?<tsrLast10s>\\d[\\d\\w\\.]+)[ ]+(?<tsrLast40s>\\d[\\d\\w\\.]+)" + WHITESPACE + // Total send and receive rate
        "Peak.*?(?<peakLast2s>\\d[\\d\\w\\.]+)[ ]+(?<peakLast10s>\\d[\\d\\w\\.]+)[ ]+(?<peakLast40s>\\d[\\d\\w\\.]+)" + WHITESPACE + // Peak rate
        "Cumulative.*?(?<cumuLast2s>\\d[\\d\\w\\.]+)[ ]+(?<cumuLast10s>\\d[\\d\\w\\.]+)[ ]+(?<cumuLast40s>\\d[\\d\\w\\.]+)" + WHITESPACE; // Cumulative

    public static IftopMessage valueOf(final String input) {
        IftopMessage msg = new IftopMessage();
        
        Pattern p = Pattern.compile(REGEX_FLOWS);
        Matcher m = p.matcher(input);
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
        msg.setFlows(flows);
        
        p = Pattern.compile(REGEX_OTHERS);
        m = p.matcher(input);

        // use if check instead of assert keyword
        // why don't assert keyword executing the assert statement?
        // it causes regex throw no match exception, because find method must be invoked before group method
        while (m.find()) {
            msg.setTotalSend(new GroupValue(
                FlowValue.valueOf(m.group("tsLast2s")),
                FlowValue.valueOf(m.group("tsLast10s")),
                FlowValue.valueOf(m.group("tsLast40s"))
            ));
            msg.setTotalReceive(new GroupValue(
                FlowValue.valueOf(m.group("trLast2s")),
                FlowValue.valueOf(m.group("trLast10s")),
                FlowValue.valueOf(m.group("trLast40s"))
            ));
            msg.setTotalSendReceive(new GroupValue(
                FlowValue.valueOf(m.group("tsrLast2s")),
                FlowValue.valueOf(m.group("tsrLast10s")),
                FlowValue.valueOf(m.group("tsrLast40s"))
            ));
            msg.setPeakRate(new GroupValue(
                FlowValue.valueOf(m.group("peakLast2s")),
                FlowValue.valueOf(m.group("peakLast10s")),
                FlowValue.valueOf(m.group("peakLast40s"))
            ));
            msg.setCumulative(new GroupValue(
                FlowValue.valueOf(m.group("cumuLast2s")),
                FlowValue.valueOf(m.group("cumuLast10s")),
                FlowValue.valueOf(m.group("cumuLast40s"))
            ));
        }

        return msg;
    }

}