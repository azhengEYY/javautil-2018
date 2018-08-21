package org.javautil.dblogging;

import static org.junit.Assert.assertNotNull;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.javautil.sql.SqlSplitterException;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class DbloggerH2Test {
    private static final Logger logger = LoggerFactory.getLogger(DbloggerH2Test.class);
    // TODO are these required or duplicate

    // @Test
    public void testInstantiate() throws SqlSplitterException, Exception {
        DataSource ds = new H2LoggerDataSource().getPopulatedH2FromDbLoggerProperties();
        // Connection connection = ds.getConnection();
        AbstractDblogger dblogger = new DbloggerH2(ds.getConnection());

        assertNotNull(dblogger);
        long jobNbr = dblogger.beginJob("DbLoggerH2Test", getClass().getName(), null, null, null, null);
        // System.out.println(dblogger.getUtProcessStatus(jobNbr));
        dblogger.insertStep("step one", null, null);
        dblogger.finishStep();
        dblogger.insertStep("step two", null, null);
        dblogger.finishStep();

        dblogger.endJob();
        // System.out.println(dblogger.getUtProcessStatus(jobNbr));

    }

    public DataSource getDataSource() throws SQLException {

        final HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:h2:/tmp/DbloggerH2Test;AUTO_SERVER=TRUE");
        // config.setJdbcUrl("jdbc:h2:/tmp/dblogger");
        config.setUsername("sr");
        config.setPassword("tutorial");
        config.setAutoCommit(false);

        DataSource ds = new HikariDataSource(config);
        Connection connection = ds.getConnection();
        Statement s = connection.createStatement();

        s.execute("drop table wtf if exists");
        s.execute("create table wtf(id number(9))");
        s.close();
        connection.close();
        return ds;
    }

    @Test
    public void getDataSourceTest() throws SQLException {
        DataSource ds = getDataSource();
        Connection conn = ds.getConnection();
        Statement s = conn.createStatement();
        ResultSet rset = s.executeQuery("select count(*) from wtf");
        rset.next();
        conn.close();
    }

    @Test
    public void test3() throws SQLException {
        DataSource ds = getDataSource();
        Connection connection = ds.getConnection();
        Statement s = connection.createStatement();

        s.execute("drop table wtf2 if exists");
        s.execute("create table wtf2(id number(9))");
        s.close();
        connection.commit();
    }

    // @Test
    public void test2() throws SqlSplitterException, Exception {
        DataSource ds = getDataSource();
        Connection connection = ds.getConnection();
        // System.out.println("getDatabaseInstrumentation connection: " + connection);

        connection.commit();
        CreateDbloggerDatabaseObjects installer = new H2Install(connection).setDrop(true).setNoFail(true)
                .setShowSql(true);
        installer.process();
        connection.commit();
        // System.out.println("installed objects and commited");
        // System.out.println("connection " + connection);
        AbstractDblogger dblogger = new DbloggerH2(connection);

        assertNotNull(dblogger);
        long jobNbr = dblogger.beginJob("DbLoggerH2Test", getClass().getName(), null, null, null, null);
        // System.out.println(dblogger.getUtProcessStatus(jobNbr));
        dblogger.insertStep("step one", null, null);
        dblogger.finishStep();
        dblogger.insertStep("step two", null, null);
        dblogger.finishStep();

        dblogger.endJob();
        // System.out.println(dblogger.getUtProcessStatus(jobNbr));
        connection.commit();
        connection.close();

    }

}
