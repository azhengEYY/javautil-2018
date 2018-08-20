package org.javautil.oracle.trace;

import java.io.IOException;

import org.javautil.oracle.trace.record.Record;
import org.junit.Test;

public class TraceFileReaderTest {
    // TODO add tests
    @Test
    public void test1() throws IOException {
        String inputFileName = "src/test/resources/oratrace/dev12c_ora_8973_job_1.trc";
        TraceFileReader tfr = new TraceFileReader(inputFileName);
        Record record;
        while ((record = tfr.getNext()) != null) {
            // System.out.println(record);

        }
        tfr.close();
    }
}
