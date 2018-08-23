package com.pacificdataservices.pdssr;

import java.io.File;
import java.sql.Connection;
import java.util.Arrays;

import javax.sql.DataSource;

import org.javautil.dblogging.Dblogger;
import org.javautil.dblogging.DbloggerForOracle;
import org.javautil.dblogging.DbloggerPropertiesDataSource;
import org.javautil.dblogging.SplitLoggerForOracle;
import org.javautil.dblogging.installer.OracleInstall;
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


    @Override
    @Test
    public void test() throws SqlSplitterException, Exception {

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


    }
}
