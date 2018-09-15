package org.javautil.dblogging.installer;

import java.sql.Connection;

import javax.sql.DataSource;

import org.javautil.dblogging.DbloggerPropertiesDataSource;
import org.javautil.sql.SqlSplitterException;
import org.junit.Test;

public class DbloggerOracleInstallTest {
    
    
    @Test
    public void testDrop() throws SqlSplitterException, Exception {
        DataSource xeDatasource = new DbloggerPropertiesDataSource("dblogger.xe.properties").getDataSource();
        Connection xeConnection = xeDatasource.getConnection();
  
        
        DbloggerOracleInstall oralog = new DbloggerOracleInstall(xeConnection,true,false);
        oralog.drop();
        oralog.process();

       
    }
    

}
