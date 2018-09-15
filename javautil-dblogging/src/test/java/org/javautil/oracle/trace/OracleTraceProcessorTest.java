package org.javautil.oracle.trace;

import java.io.IOException;

import org.junit.Test;

public class OracleTraceProcessorTest {
    
    // TODO !!! handle errors

    @Test
    public void test() throws IOException {
        OracleTraceProcessor otp = new OracleTraceProcessor("src/test/resources/oratrace/dev18b_ora_813.trc");
        otp.process();
    }
    
    @Test
    public void test2() throws IOException {
        OracleTraceProcessor otp = new OracleTraceProcessor("src/test/resources/oratrace/dev18b_ora_6884_job_1.trc");
        otp.process();
    }
}
