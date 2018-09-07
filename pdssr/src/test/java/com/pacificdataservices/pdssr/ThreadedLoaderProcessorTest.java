package com.pacificdataservices.pdssr;



import java.sql.Connection;


import javax.sql.DataSource;

import org.javautil.conditionidentification.CreateConditionIdentificationDatabaseObjects;
import org.javautil.dblogging.DbloggerPropertiesDataSource;
import org.javautil.dblogging.installer.DbloggerOracleInstall;
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
        Connection applicationConnection = dataSource.getConnection();
        DataSource xeDatasource = new DbloggerPropertiesDataSource("dblogger.xe.properties").getDataSource();
        Connection xeConnection = xeDatasource.getConnection();
        
        DbloggerOracleInstall oralog = new DbloggerOracleInstall(xeConnection,true,true);
        oralog.drop();
        oralog.process();
        
        CreateConditionIdentificationDatabaseObjects condiobj = new CreateConditionIdentificationDatabaseObjects(applicationConnection);
        condiobj.setShowSql(true);
        condiobj.drop();
        condiobj.process();
    
        Timer timer = new Timer();
        final ThreadedLoaderProcessor loadProcessor = new ThreadedLoaderProcessor(dataSource, xeDatasource,8);
        loadProcessor.run();
        logger.info("elapsed " + timer.getElapsedMillis());
        // TODO check the number of tables in etl_File
//        // 1 is 349,266
        // 8 is 102,056
    }
}
