package com.pacificdataservices.pdssr;

import java.sql.Connection;

import javax.sql.DataSource;

import org.javautil.misc.Timer;
import org.javautil.sql.ApplicationPropertiesDataSource;
import org.javautil.sql.Dialect;
import org.javautil.sql.SqlSplitterException;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreateSchemaTest {
    static DataSource     datasource;
    private static Logger logger = LoggerFactory.getLogger(CreateSchemaTest.class);
    static Connection     conn;

    @BeforeClass
    public static void beforeClass() throws Exception, SqlSplitterException {
        datasource = new ApplicationPropertiesDataSource().getDataSource();
        conn = datasource.getConnection();
        if (Dialect.getDialect(conn).equals(Dialect.H2)) {
            logger.info("conn" + conn);
        }
        logger.info("about to create schema");
        new CreateSchema(conn, true, true).process();
        conn.commit();

        // conn.close();
    }

    @Test
    public void test() throws Exception, SqlSplitterException {
        final DataSource datasource = new ApplicationPropertiesDataSource().getDataSource();
        final Connection conn = datasource.getConnection();
        final Timer t = new Timer("createSchema");
        final CreateSchema cs = new CreateSchema(conn, true, false);
        cs.process();
        conn.commit();
        t.logElapsed();
        logger.info("schema created");

    }

}
