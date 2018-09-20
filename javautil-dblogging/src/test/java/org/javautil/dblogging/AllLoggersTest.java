/**
 *
 */
package org.javautil.dblogging;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.Closeable;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.javautil.dblogging.installer.DbloggerOracleInstall;
import org.javautil.dblogging.installer.H2LoggerDataSource;
import org.javautil.dblogging.logger.Dblogger;
import org.javautil.dblogging.logger.SplitLoggerForOracle;
import org.javautil.dblogging.tracepersistence.DbloggerPersistence;
import org.javautil.dblogging.tracepersistence.DbloggerPersistenceImpl;
import org.javautil.sql.ApplicationPropertiesDataSource;
import org.javautil.sql.Binds;
import org.javautil.sql.ConnectionUtil;
import org.javautil.sql.SqlSplitterException;
import org.javautil.sql.SqlStatement;
import org.javautil.util.NameValue;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AllLoggersTest {

    private final static Logger logger      = LoggerFactory.getLogger(AllLoggersTest.class);
    final String                processName = "Logging Example";
    private static DataSource   applicationDataSource;
    private static DataSource   loggerDataSource;
    private boolean showInstallSql = false;

    @BeforeClass
    public static void beforeClass() throws SqlSplitterException, Exception {
        applicationDataSource = new ApplicationPropertiesDataSource().getDataSource(new AllLoggersTest(),
                "logger_and_application.properties");
        loggerDataSource = new DbloggerPropertiesDataSource("logger_and_application.properties").getDataSource();
    }
    
    @AfterClass
    public static void afterClass() throws IOException {
       ((Closeable) applicationDataSource).close();
       ((Closeable) loggerDataSource).close();
    }
    
   
    public long sampleUsage(Dblogger dblogger, Connection appConnection) throws SqlSplitterException, Exception {
        dblogger.prepareConnection();
        final String processName = "Process Name";
        // Start the job

        final long logJobId = dblogger.startJobLogging(processName,getClass().getName(), null, null,  4);
        dblogger.setModule("SplitLoggerTest", "simple example");
        dblogger.setAction("Some work");
        dblogger.insertStep("Full join", "Meaningless busy work", getClass().getName());
        ConnectionUtil.exhaustQuery(appConnection, "select * from user_tab_columns, user_tables where rownum < 100");

        dblogger.setAction("Another set of work");
        ConnectionUtil.exhaustQuery(appConnection, "select count(*) from all_tab_columns");
        // End the job
        dblogger.endJob();
        return logJobId;
    }

    @Test
    public void testSplitLogger() throws SqlSplitterException, Exception {
        Connection appConnection = applicationDataSource.getConnection();
        Connection xeConnection = loggerDataSource.getConnection();
        logger.info("xeInstall");
        DbloggerOracleInstall orainst = new DbloggerOracleInstall(xeConnection, true, showInstallSql);
        orainst.process();
        logger.info("DbloggerOracleInstall");
        orainst = new DbloggerOracleInstall(appConnection, true, showInstallSql);
        orainst.process();
        //
        logger.info("new persistenceLogger");
        DbloggerPersistence persistenceLogger = new DbloggerPersistenceImpl(loggerDataSource.getConnection());
        Dblogger dblogger = new SplitLoggerForOracle(appConnection, persistenceLogger);
        logger.info("sampleUsage");
        long id = sampleUsage(dblogger, appConnection);
        Connection conn = loggerDataSource.getConnection();
        testResults(conn, id);
        appConnection.close();
        xeConnection.close();
        conn.close();
    }

//    @Test
//    public void testDbloggerForOracle() throws SqlSplitterException, Exception {
//        Connection appConnection = appDataSource.getConnection();
//        logger.info("installing objects for appConnection");
//        DbloggerOracleInstall orainst = new DbloggerOracleInstall(appConnection, true, showInstallSql);
//        orainst.process();
//        logger.info("objects created");
//        //
//        Dblogger dblogger = new DbloggerForOracle(appConnection);
//        long id = sampleUsage(dblogger, appConnection);
//        Connection conn = appDataSource.getConnection();
//        testResults(conn, id);
//        conn.close();
//        appConnection.close();
//    }

    //@Test
    public void testH2logger() throws SqlSplitterException, Exception {
        DataSource h2DataSource = new H2LoggerDataSource().getPopulatedH2FromDbLoggerProperties(this,
                "h2.dblogger.properties");
        Connection appConnection = applicationDataSource.getConnection();
        Dblogger dblogger = new SplitLoggerForOracle(appConnection, h2DataSource.getConnection());
        long id = sampleUsage(dblogger, appConnection);
        Connection conn = h2DataSource.getConnection();
        testResults(conn, id);
        conn.close();
        appConnection.close();
    }

    public void testResults(Connection conn, long id) throws SQLException {
        SqlStatement ss = new SqlStatement(conn, "select * from job_log where job_log_id = :job_log_id");
        Binds binds = new Binds();
        binds.put("job_log_id", id);
        final NameValue status = ss.getNameValue(binds, false);
        ss.close();
        logger.debug(status.getSortedMultilineString());
        // assertEquals(processName, status.getString("PROCESS_NAME"));
   //     assertEquals("C", status.getString("STATUS_ID"));
        assertNotNull(status.getString("STATUS_TS"));
        String tracefileName = status.getString("TRACEFILE_NAME");
        int xeindex = tracefileName.indexOf("xe");
        assertEquals(-1, xeindex);

        // check out step
        ss = new SqlStatement(conn, "select * from job_step where job_log_id = :job_log_id");
        final NameValue stepStatusNv = ss.getNameValue(binds, false);
       // logger.info("step: {}", stepStatusNv.getSortedMultilineString());
    }

   

}
