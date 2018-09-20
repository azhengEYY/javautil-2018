package org.javautil.dblogging;

import java.io.Closeable;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.javautil.dblogging.installer.DbloggerOracleInstall;
import org.javautil.sql.Dialect;
import org.javautil.sql.ResultSetHelper;
import org.javautil.sql.SqlSplitterException;
import org.javautil.util.NameValue;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OracleInstallTest {

   // private static ApplicationPropertiesDataSource dsSource = new ApplicationPropertiesDataSource();
    
    static DataSource                              applicationDataSource;
    private static Logger                          logger   = LoggerFactory.getLogger(OracleInstallTest.class);
    static boolean                                 skipTests;
 

    @BeforeClass
    public static void beforeClass() throws Exception {
        applicationDataSource = new DbloggerPropertiesDataSource("dblogger.xe.properties").getDataSource();
        if (!Dialect.getDialect(applicationDataSource.getConnection()).equals(Dialect.ORACLE)) {
            skipTests = true;
        }
        DbloggerOracleInstall installer = new DbloggerOracleInstall(applicationDataSource.getConnection(), true, false);
        installer.process();
    }
    
    @AfterClass
    public static void afterClass() throws IOException {
       ((Closeable) applicationDataSource).close();
    }

    NameValue getUtProcessStatus(Connection connection2, long id) throws SQLException {
        final String sql = "select * from job_log where job_log_id = :id";
      //  final Connection connection2 = dataSource.getConnection();
        final PreparedStatement statusStatement = connection2.prepareStatement(sql);
        statusStatement.setLong(1, id);

        final ResultSet rset = statusStatement.executeQuery();
        rset.next();
        final NameValue retval = ResultSetHelper.getNameValue(rset, false);
      //  connection2.close();
        return retval;
    }

    //@Test
    public void installTest() throws Exception, SqlSplitterException {
        // TODO test all tables and objects, log write and oracle trace reda
        
        DbloggerOracleInstall installer = new DbloggerOracleInstall(applicationDataSource.getConnection(), true, false);
        installer.process();
    }

}
