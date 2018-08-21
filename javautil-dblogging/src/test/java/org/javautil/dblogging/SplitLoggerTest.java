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

import org.javautil.sql.Binds;
import org.javautil.sql.ConnectionUtil;
import org.javautil.sql.SqlSplitterException;
import org.javautil.sql.SqlStatement;
import org.javautil.util.NameValue;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/// TODO code coverage https://asktom.oracle.com/pls/asktom/f?p=100:11:0::::P11_QUESTION_ID:9531985800346169203
public class SplitLoggerTest extends OracleInstallTest {

    private final Logger logger      = LoggerFactory.getLogger(getClass());
    final String         processName = "Logging Example";

    @Test // O add steps and make as an example
    public void sampleUsage() throws SqlSplitterException, Exception {
        if (skipTests) {
            logger.warn("skipping tests not oracle");
            return;
        }
        DataSource xeDatasource = new DbloggerPropertiesDataSource("dblogger.xe.properties").getDataSource();
        Connection xeConnection = xeDatasource.getConnection();
        
        final Connection connection = dataSource.getConnection();
        OracleInstall orainst = new OracleInstall(xeConnection, false, false);
        orainst.process();
        Dblogger persistenceLogger = new DbloggerForOracle(xeDatasource.getConnection());
        
        // begin sample job
        final SplitLoggerForOracle dblogger = new SplitLoggerForOracle(connection, persistenceLogger);
        dblogger.prepareConnection();
        final String processName = "Process Name";
        //
        final int id = dblogger.beginJob(processName, getClass().getCanonicalName(), "ExampleLogging", null,
                Thread.currentThread().getName(), null);

        dblogger.setAction("Some work");
        ConnectionUtil.exhaustQuery(connection, "select * from user_tab_columns, user_tables");

        dblogger.setAction("Another set of work");
        ConnectionUtil.exhaustQuery(connection, "select count(*) from all_tab_columns");
        dblogger.endJob();
        // test it

        // check ut_status_process_fields
        
        SqlStatement ss = new SqlStatement(xeDatasource.getConnection(),"select * from ut_process_status where ut_process_status_id = (select max(ut_process_status_id )from ut_process_status)");
        Binds binds = new Binds();
        final NameValue status = ss.getNameValue(binds,false);
        logger.debug(status.getSortedMultilineString());
        assertEquals(processName, status.getString("PROCESS_NAME"));
        assertEquals("C", status.getString("STATUS_ID"));
        assertNotNull(status.getString("STATUS_TS"));
     //   assertNotNull(status.getString("TOTAL_ELAPSED"));//
        status.getString("TRACEFILE_NAME");
    }

   // @Test
    public void testOpenFile() throws SQLException, SqlSplitterException, IOException {
        if (skipTests) {
            logger.info("skipping tests not oracle");
            return;
        }
        final Connection connection = dataSource.getConnection();
        final DbloggerForOracle dblogger = new DbloggerForOracle(connection);
        final String logFileName = dblogger.openFile(null);
        logger.info("logFileName: " + logFileName);
    }

    NameValue getLastUtProcessStatus(Connection connection) throws SQLException {
        final String sql = "select * from ut_process_status "
                + "where ut_process_status_id = (select max(ut_process_status_id) from ut_process_status)";
        final SqlStatement ss = new SqlStatement(connection, sql);
        final NameValue retval = ss.getNameValue();
        ss.close();
        return retval;
    }

}
