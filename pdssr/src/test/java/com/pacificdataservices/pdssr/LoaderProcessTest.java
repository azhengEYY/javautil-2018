package com.pacificdataservices.pdssr;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;

import javax.sql.DataSource;

import org.javautil.dblogging.Dblogger;
import org.javautil.dblogging.DbloggerForOracle;
import org.javautil.dblogging.DbloggerPropertiesDataSource;
import org.javautil.dblogging.H2LoggerDataSource;
import org.javautil.dblogging.OracleInstall;
import org.javautil.dblogging.SplitLoggerForOracle;
import org.javautil.sql.ApplicationPropertiesDataSource;
import org.javautil.sql.SqlSplitterException;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoaderProcessTest extends CreateSchemaTest {
    Logger logger = LoggerFactory.getLogger(getClass());

    File[] getLoadFiles(LoadProcessor loadProcessor) {
        return Arrays.copyOfRange(loadProcessor.getFiles(), 0, 3);
    }

    public long testDataSource(DataSource ds) throws SQLException {
        final String count = "select count(*) cnt from ut_process_status";
        Connection conn1 = ds.getConnection();
        Statement statement1 = conn1.createStatement();
        ResultSet rset1 = statement1.executeQuery(count);
        rset1.next();
        long cnt1 = rset1.getLong("cnt");
        rset1.close();
        conn1.close();
        return cnt1;
    }

    @Override
    @Test
    public void test() throws SqlSplitterException, Exception {
      //  DataSource h2loggerDataSource = new H2LoggerDataSource().getPopulatedH2FromDbLoggerProperties();
     //   long oldCount = testDataSource(h2loggerDataSource);
     //   logger.info("oldCount {}", oldCount);

        DataSource dataSource = new ApplicationPropertiesDataSource().getDataSource();
        Connection connection = dataSource.getConnection();
        //
        DataSource xeDatasource = new DbloggerPropertiesDataSource("dblogger.xe.properties").getDataSource();
        Connection xeConnection = xeDatasource.getConnection();
        
        OracleInstall orainst = new OracleInstall(xeConnection, false, false);
        orainst.process();
        Dblogger persistenceLogger = new DbloggerForOracle(xeDatasource.getConnection());
        
        // begin sample job
        final SplitLoggerForOracle dblogger = new SplitLoggerForOracle(connection, persistenceLogger);
        
        final LoadProcessor loadProcessor = new LoadProcessor(conn, dblogger);
        loadProcessor.processFiles(getLoadFiles(loadProcessor));
//        Connection h2connection = h2loggerDataSource.getConnection();
//        long newCount = testDataSource(h2loggerDataSource);
//        logger.info("oldCount {} newCount {}", oldCount, newCount);
//        logger.info("connection " + h2connection);
//        assertTrue(newCount > oldCount);
//        File f = new File("/tmp/dbloggerh2.mv.db");
//        assertTrue(f.exists());
//        String connInfo = h2connection.toString();
//        assertTrue(connInfo.endsWith("url=jdbc:h2:/tmp/dbloggerh2 user=SR"));
//        h2connection.close();

    }
}
