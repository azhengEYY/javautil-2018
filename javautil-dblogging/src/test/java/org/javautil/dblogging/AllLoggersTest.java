/**
 *
 */
package org.javautil.dblogging;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.javautil.dblogging.installer.H2LoggerDataSource;
import org.javautil.dblogging.installer.OracleInstall;
import org.javautil.sql.ApplicationPropertiesDataSource;
import org.javautil.sql.Binds;
import org.javautil.sql.ConnectionUtil;
import org.javautil.sql.SqlSplitterException;
import org.javautil.sql.SqlStatement;
import org.javautil.util.NameValue;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AllLoggersTest
{

    private final static Logger logger      = LoggerFactory.getLogger(AllLoggersTest.class);
    final String         processName = "Logging Example";
    private static DataSource appDataSource;
    private static DataSource xeDataSource;
    
    @BeforeClass
    public static void beforeClass() throws SqlSplitterException, Exception {
        appDataSource = new ApplicationPropertiesDataSource().getDataSource(new AllLoggersTest(),"oracle.application.properties");
        xeDataSource = new DbloggerPropertiesDataSource("dblogger.xe.properties").getDataSource();
    }


    public long sampleUsage(Dblogger dblogger, Connection appConnection) throws SqlSplitterException, Exception {
        

        dblogger.prepareConnection();
        final String processName = "Process Name";
        // Start the job
        final long id = dblogger.beginJob(processName, getClass().getCanonicalName(), "ExampleLogging", null,
                Thread.currentThread().getName(), null);
        dblogger.setModule("SplitLoggerTest", "simple example");
        dblogger.setAction("Some work");
        dblogger.insertStep("Full join", "Meaningless busy work", getClass().getName());
        ConnectionUtil.exhaustQuery(appConnection, "select * from user_tab_columns, user_tables");

        dblogger.setAction("Another set of work");
        ConnectionUtil.exhaustQuery(appConnection, "select count(*) from all_tab_columns");
        // End the job
        dblogger.endJob();

        return id;
        
    }
    
    //@Test
    public void testSplitLogger() throws SqlSplitterException, Exception {
        Connection appConnection = appDataSource.getConnection();
        Connection xeConnection = xeDataSource.getConnection();
        OracleInstall orainst = new OracleInstall(xeConnection, true, false);
        orainst.process();
        orainst = new OracleInstall(appConnection, true, false);
        orainst.process();
        //
        Dblogger persistenceLogger = new DbloggerForOracle(xeDataSource.getConnection());
        Dblogger dblogger = new SplitLoggerForOracle(appConnection, persistenceLogger);
        long id = sampleUsage(dblogger, appConnection);
        Connection conn = xeDataSource.getConnection();
        testResults(conn,id);
        appConnection.close();
        xeConnection.close();
        conn.close();
    }
    
   // @Test
    public void testDbloggerForOracle() throws SqlSplitterException, Exception {
        Connection appConnection = appDataSource.getConnection();
       
        OracleInstall orainst = new OracleInstall(appConnection, true, false);
        orainst.process();
        //
        Dblogger dblogger = new DbloggerForOracle(appConnection);
        long id = sampleUsage(dblogger, appConnection);
        Connection conn = appDataSource.getConnection();
        testResults(conn,id);
        conn.close();
        appConnection.close();
    }
    
    @Test
    public void testH2logger() throws SqlSplitterException, Exception {
        DataSource h2DataSource = new H2LoggerDataSource().getPopulatedH2FromDbLoggerProperties(this, "h2.dblogger.properties"); 
        Connection appConnection = appDataSource.getConnection();
        Dblogger dblogger = new SplitLoggerForOracle(appConnection,h2DataSource.getConnection());
        long id = sampleUsage(dblogger, appConnection);
        Connection conn = h2DataSource.getConnection();
        testResults(conn,id);
        conn.close();
        appConnection.close();
    }
    
    public void testResults(Connection conn, long id) throws SQLException {
    
    SqlStatement ss = new SqlStatement(conn,"select * from job_log where job_log_id = :job_log_id");
    Binds binds = new Binds();
    binds.put("job_log_id",id);
    final NameValue status = ss.getNameValue(binds,false);
    ss.close();
    logger.debug(status.getSortedMultilineString());
 //   assertEquals(processName, status.getString("PROCESS_NAME"));
    assertEquals("C", status.getString("STATUS_ID"));
    assertNotNull(status.getString("STATUS_TS"));
    String tracefileName = status.getString("TRACEFILE_NAME");
    int xeindex = tracefileName.indexOf("xe");
    assertEquals(-1, xeindex);
    
    // check out step
    ss = new SqlStatement(conn,"select * from job_step where job_log_id = :job_log_id");
    final NameValue stepStatusNv = ss.getNameValue(binds,false);
    logger.info("step: {}" , stepStatusNv.getSortedMultilineString());
    }

//   // @Test
//    public void testOpenFile() throws SQLException, SqlSplitterException, IOException {
////        if (skipTests) {
////            logger.info("skipping tests not oracle");
////            return;
////        }
//        final Connection connection = dataSource.getConnection();
//        final DbloggerForOracle dblogger = new DbloggerForOracle(connection);
//        final String logFileName = dblogger.openFile(null);
//        logger.info("logFileName: " + logFileName);
//    }

//    NameValue getLastUtProcessStatus(Connection connection) throws SQLException {
//        final String sql = "select * from job_log "
//                + "where job_log_id = (select max(job_log_id) from job_log)";
//        final SqlStatement ss = new SqlStatement(connection, sql);
//        final NameValue retval = ss.getNameValue();
//        ss.close();
//        return retval;
//    }

}
