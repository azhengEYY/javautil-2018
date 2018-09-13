package org.javautil.oracle.trace;

import java.io.IOException;

import org.junit.Test;

public class OracleTraceProcessorTest {

    @Test
    public void test() throws IOException {
        OracleTraceProcessor otp = new OracleTraceProcessor("src/test/resources/oratrace/dev18b_ora_813.trc");
        otp.process();
    }
}
