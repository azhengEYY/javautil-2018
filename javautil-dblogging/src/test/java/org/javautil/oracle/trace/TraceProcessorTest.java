package org.javautil.oracle.trace;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.FileOutputStream;
import java.io.IOException;

import org.javautil.io.Tracer;
import org.javautil.oracle.trace.formatter.OracleTraceReport;
import org.javautil.oracle.trace.record.Parsing;
import org.junit.Test;

public class TraceProcessorTest {
    // SQL ID: 2xcqaktjqwp1h Plan Hash: 0
    //
    // insert into etl_file ( etl_file_id, RPT_ORG_ID, DATAFEED_ORG_ID)
    // values
    // ( :etl_file_id, :rpt_org_id, :org_id)
    //
    //
    // call count cpu elapsed disk query current rows
    // ------- ------ -------- ---------- ---------- ---------- ----------
    // ----------
    // Parse 1 0.00 0.00 0 0 0 0
    // Execute 1 0.00 0.00 0 9 97 1
    // Fetch 0 0.00 0.00 0 0 0 0
    // ------- ------ -------- ---------- ---------- ---------- ----------
    // ----------
    // total 2 0.00 0.00 0 9 97 1
    //
    // Misses in library cache during parse: 1
    // Misses in library cache during execute: 1
    // Optimizer mode: ALL_ROWS
    // Parsing user id: 672
    // Number of plan statistics captured: 1
    //
    // Rows (1st) Rows (avg) Rows (max) Row Source Operation
    // ---------- ---------- ----------
    // ---------------------------------------------------
    // 0 0 0 LOAD TABLE CONVENTIONAL ETL_FILE (cr=5 pr=0 pw=0 time=393 us starts=1)

    // @Test
    public void test1() throws IOException {
        String inputFileName = "src/test/resources/oratrace/job1.trc";
        OracleTraceProcessor tfr = new OracleTraceProcessor(inputFileName);
        tfr.process();
        CursorsStats cursorStats = tfr.getCursors();
        CursorInfo cursorStat = cursorStats.getCursorInfoByCursorId("2xcqaktjqwp1h");
        assertNotNull(cursorStat);
        Parsing parsing = cursorStat.getParsing();
        assertNotNull(parsing);
        // System.out.println(parsing.toString());
        String expected = "insert into etl_file ( etl_file_id, RPT_ORG_ID, DATAFEED_ORG_ID) values ( :etl_file_id, :rpt_org_id, :org_id)";
        assertEquals(expected, parsing.getSqlText().trim());
        OracleTraceReport formatter = new OracleTraceReport(System.out);
        formatter.process(false);
    }

    // @Test
    public void test2() throws IOException {

        String inputFileName = "src/test/resources/oratrace/dev12c_ora_10581_job_6.trc";

        FileOutputStream fos = new FileOutputStream(
                "target/test-classes/oratrace/dev12c_ora_10581_job_6.dblogging.prf");
        Tracer tracer = new Tracer("target/test-classes/oratrace/dev12c_ora_10581_job_6.dblogging.trace");
        OracleTraceProcessor tfr = new OracleTraceProcessor(inputFileName);
        tfr.setTracer(tracer);
        tfr.process();

//        CursorsStats cursorStats = tfr.getCursors();

        OracleTraceReport formatter = new OracleTraceReport(fos);

        formatter.format(tfr.getCursors(), false);
        fos.close();
        tracer.close();
    }

    @Test
    public void test3() throws IOException {
        String base = "dev18b_ora_813";
        String inputFileName = "src/test/resources/oratrace/" + base + ".trc";

        FileOutputStream fos = new FileOutputStream("target/test-classes/oratrace/" + base + ".juf");
        Tracer tracer = new Tracer("target/test-classes/oratrace/" + base + ".trace");
        OracleTraceProcessor tfr = new OracleTraceProcessor(inputFileName);
        tfr.setTracer(tracer);
        tfr.process();

    //    CursorsStats cursorStats = tfr.getCursors();

        OracleTraceReport formatter = new OracleTraceReport(fos);
      System.out.println(tfr.getCursors().toString());
//        for (CursorInfo ci : tfr.getCursors().getAllCursorInfo()) {
//            System.out.println(ci.toString());
//        }
        formatter.format(tfr.getCursors(), false);
        fos.close();
        tracer.close();
    }
}
