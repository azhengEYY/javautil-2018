/**
 *
 */
package org.javautil.dblogging;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.javautil.sql.ApplicationPropertiesDataSource;
import org.javautil.sql.ConnectionUtil;
import org.javautil.sql.Dialect;
import org.javautil.sql.ResultSetHelper;
import org.javautil.util.NameValue;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExampleLoggingTest {

    private static DataSource              dataSource;
    private static Connection              conn;
    private static Dblogger dblogger;
    private static boolean                 skipTests;

    private final Logger                   logger      = LoggerFactory.getLogger(getClass());
    final String                           processName = "Logging Example";

    @BeforeClass
    public static void beforeClass() throws SQLException, IOException {
        dataSource = new ApplicationPropertiesDataSource().getDataSource(); // TODO convert to properties
        conn = dataSource.getConnection();
        switch (Dialect.getDialect(conn)) {
        case ORACLE:
            break;
        default:
            skipTests = true;

        }
        dblogger = new DbloggerForOracle(conn);
    }

    @Test
    public void testOpenCursors() throws SQLException {
        if (skipTests) {
            return;
        }
        final Connection conn = dataSource.getConnection();
        final Dblogger dblogger = new DbloggerForOracle(conn);
        dblogger.prepareConnection();

        dblogger.beginJob(processName, getClass().getCanonicalName(), "ExampleLogging", null,
                Thread.currentThread().getName(), null);

        dblogger.setModule("dog", "killed");
        dblogger.setModule("hello", null); // TODO call dbms_session and verify
        for (int i = 0; i < 10; i++) {
            dblogger.setAction("action #" + i);
        }
    }

    @Test
    public void testIt() throws SQLException, IOException {
        if (skipTests) {
            return;
        }
        // todo switch log files
        // todo record log file
        // todo test tracing
        String traceFileName;
        final int id = sampleUsage();

        traceFileName = dblogger.getTraceFileName();
        assertNotNull(traceFileName);
        // logger.info("traceFileName: " + traceFileName);
        testBeginJob(processName, id);
        testEndJob(id);
        final File outputFile = new File("/tmp/wtf.trc");

        dblogger.getMyTraceFile(outputFile);

    }

    int sampleUsage() throws SQLException {
        dblogger.prepareConnection();

        final int id = dblogger.beginJob(processName, getClass().getCanonicalName(), "ExampleLogging", null,
                Thread.currentThread().getName(), null);
        action1();
        action2();
        dblogger.endJob();
        return id;
    }

    /**
     * A call to dblogger.setAction will insert a record
     *
     * @throws SQLException
     */
    void action1() throws SQLException {
        dblogger.setAction("Some work");

        final String sql = "select * from all_tab_columns where rownum < 1000";
        ConnectionUtil.exhaustQuery(conn, sql);
    }

    void action2() throws SQLException {
        dblogger.setAction("Another set of work");
        final String sql = "select count(*) from all_tab_columns";
        ConnectionUtil.exhaustQuery(conn, sql);
    }

    NameValue getUtProcessStatus(int id) throws SQLException {
        final String sql = "select * from ut_process_status where ut_process_status_id = :id";
        final Connection connection2 = dataSource.getConnection();
        final PreparedStatement statusStatement = connection2.prepareStatement(sql);
        statusStatement.setInt(1, id);

        final ResultSet rset = statusStatement.executeQuery();
        rset.next();
        final NameValue retval = ResultSetHelper.getNameValue(rset, false);
        connection2.close();
        return retval;

    }

    void testBeginJob(String processName, int id) throws SQLException {
        final NameValue row = getUtProcessStatus(id);
        assertEquals(processName, row.get("PROCESS_NAME"));

    }

    void testEndJob(int id) throws SQLException {
        final NameValue row = getUtProcessStatus(id);
        assertNotNull(row);
        // logger.info(row.toString());
    }
}
