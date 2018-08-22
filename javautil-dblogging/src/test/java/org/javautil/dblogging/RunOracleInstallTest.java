package org.javautil.dblogging;

import java.sql.Connection;

import javax.sql.DataSource;

import org.javautil.dblogging.installer.OracleInstall;
import org.javautil.sql.ApplicationPropertiesDataSource;
import org.javautil.sql.Dialect;
import org.javautil.sql.SqlSplitterException;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RunOracleInstallTest {

    private static ApplicationPropertiesDataSource dsSource = new ApplicationPropertiesDataSource();
    static DataSource                              dataSource;
    private static Logger                          logger   = LoggerFactory.getLogger(RunOracleInstallTest.class);
    static boolean                                 skipTests;
    Connection                                     connection;

    @BeforeClass
    public static void beforeClass() throws Exception {
        dataSource = dsSource.getDataSource();
        if (!Dialect.getDialect(dataSource.getConnection()).equals(Dialect.ORACLE)) {
            skipTests = true;
        }
    }

    @Test
    public void installTest() throws Exception, SqlSplitterException {
        OracleInstall installer = new OracleInstall(dataSource.getConnection(), true, false);
        installer.process();
    }

}
