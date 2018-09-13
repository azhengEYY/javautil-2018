//package org.javautil.dblogging;
//
//import java.sql.Connection;
//
//import javax.sql.DataSource;
//
//import org.javautil.dblogging.installer.DbloggerOracleInstall;
//import org.javautil.sql.ApplicationPropertiesDataSource;
//import org.javautil.sql.Dialect;
//import org.javautil.sql.SqlSplitterException;
//import org.junit.BeforeClass;
//import org.junit.Test;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//public class RunOracleInstallTest {
//
//    private static ApplicationPropertiesDataSource dsSource = new ApplicationPropertiesDataSource();
//    static DataSource                              dataSource;
//    private static Logger                          logger   = LoggerFactory.getLogger(RunOracleInstallTest.class);
//    static boolean                                 skipTests;
//    Connection                                     connection;
//
//    @BeforeClass
//    public static void beforeClass() throws Exception {
//        dataSource = dsSource.getDataSource();
//        if (!Dialect.getDialect(dataSource.getConnection()).equals(Dialect.ORACLE)) {
//            skipTests = true;
//        }
//    }
//
//    @Test
//    public void installTest() throws Exception, SqlSplitterException {
//        DbloggerOracleInstall installer = new DbloggerOracleInstall(dataSource.getConnection(), true, false);
//        installer.process();
//    }
//
//}
