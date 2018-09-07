package com.pacificdataservices.pdssr;



import java.sql.Connection;


import javax.sql.DataSource;

import org.javautil.dblogging.DbloggerPropertiesDataSource;
import org.javautil.dblogging.installer.OracleInstall;
import org.javautil.misc.Timer;
import org.javautil.sql.ApplicationPropertiesDataSource;
import org.javautil.sql.SqlSplitterException;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThreadedLoaderProcessorTest extends CreateSchemaTest {
    Logger logger = LoggerFactory.getLogger(getClass());
  
   
    @Test
    public void test() throws SqlSplitterException, Exception {

        DataSource dataSource = new ApplicationPropertiesDataSource().getDataSource();
        DataSource xeDatasource = new DbloggerPropertiesDataSource("dblogger.xe.properties").getDataSource();
        Connection xeConnection = xeDatasource.getConnection();
        
        OracleInstall orainst = new OracleInstall(xeConnection, false, false);
        orainst.process();
        Timer timer = new Timer();
        final ThreadedLoaderProcessor loadProcessor = new ThreadedLoaderProcessor(dataSource, xeDatasource,8);
        loadProcessor.run();
        logger.info("elapsed " + timer.getElapsedMillis());
        // 1 is 349,266
        // 8 is 102,056
    }
}
