/**
 *
 */
package org.javautil.dblogging;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.javautil.oracle.OracleSessionInfo;
import org.javautil.sql.ApplicationPropertiesDataSource;
import org.javautil.sql.Binds;
import org.javautil.sql.ConnectionUtil;
import org.javautil.sql.SqlSplitterException;
import org.javautil.sql.SqlStatement;
import org.javautil.util.NameValue;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/// TODO code coverage https://asktom.oracle.com/pls/asktom/f?p=100:11:0::::P11_QUESTION_ID:9531985800346169203
public class SplitLoggerTest //extends OracleInstallTest 
{

    private final Logger logger      = LoggerFactory.getLogger(getClass());
    final String         processName = "Logging Example";

    @Test // O add steps and make as an example
    public void sampleUsage() throws SqlSplitterException, Exception {

        
        DataSource appDataSource = new ApplicationPropertiesDataSource().getDataSource(this,"oracle.application.properties");
        final Connection appConnection = appDataSource.getConnection();
        
        DataSource xeDatasource = new DbloggerPropertiesDataSource("dblogger.xe.properties").getDataSource();
        Connection xeConnection = xeDatasource.getConnection();
        
 
        logger.info("appConnection: {}", OracleSessionInfo.getConnectionInfo(appConnection) );
        //
        OracleInstall orainst = new OracleInstall(xeConnection, true, false);
        orainst.process();
        orainst = new OracleInstall(appConnection, true, false);
        orainst.process();
        //
        Dblogger persistenceLogger = new DbloggerForOracle(xeDatasource.getConnection());
        
        // begin sample job
        final SplitLoggerForOracle dblogger = new SplitLoggerForOracle(appConnection, persistenceLogger);
        dblogger.prepareConnection();
        final String processName = "Process Name";
        //
        final long id = dblogger.beginJob(processName, getClass().getCanonicalName(), "ExampleLogging", null,
                Thread.currentThread().getName(), null);
        logger.info("*****after beginJob tracefileName: {}",getTraceFileName(xeConnection,id));
        
        dblogger.setAction("Some work");
        ConnectionUtil.exhaustQuery(appConnection, "select * from user_tab_columns, user_tables");

        dblogger.setAction("Another set of work");
        ConnectionUtil.exhaustQuery(appConnection, "select count(*) from all_tab_columns");
        logger.info("*****before endJob tracefileName: {}",getTraceFileName(xeConnection,id));
        dblogger.endJob();
        logger.info("****after endJob tracefileName: {}", getTraceFileName(xeConnection,id));
        // test it

        // check ut_status_process_fields
        
        assertTraceNotXe(xeConnection,id);
        
//        SqlStatement ss = new SqlStatement(xeDatasource.getConnection(),"select * from ut_process_status where ut_process_status_id = :ut_process_status_id");
//        Binds binds = new Binds();
//        binds.put("ut_process_status_id",id);
//        final NameValue status = ss.getNameValue(binds,false);
//        logger.debug(status.getSortedMultilineString());
//        assertEquals(processName, status.getString("PROCESS_NAME"));
//        assertEquals("C", status.getString("STATUS_ID"));
//        assertNotNull(status.getString("STATUS_TS"));
//        String tracefileName = status.getString("TRACEFILE_NAME");
//        int xeindex = tracefileName.indexOf("xe");
//        assertEquals(-1, xeindex);
//        
//     //   assertNotNull(status.getString("TOTAL_ELAPSED"));//
//        status.getString("TRACEFILE_NAME");
    }
    
    
    public String getTraceFileName(Connection conn, long id) throws SQLException {
        SqlStatement ss = new SqlStatement(conn,"select * from ut_process_status where ut_process_status_id = :ut_process_status_id");
        Binds binds = new Binds();
        binds.put("ut_process_status_id",id);
        final NameValue status = ss.getNameValue(binds,false);
 
        String tracefileName = status.getString("TRACEFILE_NAME");
       return tracefileName;
    }
    public void assertTraceNotXe(Connection conn, long id) throws SQLException {
        SqlStatement ss = new SqlStatement(conn,"select * from ut_process_status where ut_process_status_id = :ut_process_status_id");
        Binds binds = new Binds();
        binds.put("ut_process_status_id",id);
        final NameValue status = ss.getNameValue(binds,false);
        logger.debug(status.getSortedMultilineString());
     //   assertEquals(processName, status.getString("PROCESS_NAME"));
        assertEquals("C", status.getString("STATUS_ID"));
        assertNotNull(status.getString("STATUS_TS"));
        String tracefileName = status.getString("TRACEFILE_NAME");
        int xeindex = tracefileName.indexOf("xe");
        assertEquals(-1, xeindex);
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

    NameValue getLastUtProcessStatus(Connection connection) throws SQLException {
        final String sql = "select * from ut_process_status "
                + "where ut_process_status_id = (select max(ut_process_status_id) from ut_process_status)";
        final SqlStatement ss = new SqlStatement(connection, sql);
        final NameValue retval = ss.getNameValue();
        ss.close();
        return retval;
    }

}
