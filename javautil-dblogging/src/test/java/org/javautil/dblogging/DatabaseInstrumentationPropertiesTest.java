package org.javautil.dblogging;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.junit.Test;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class DatabaseInstrumentationPropertiesTest {

    public DataSource getDataSource() throws SQLException {

        final HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:h2:/tmp/dblogger;AUTO_SERVER=TRUE");
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
    public void test1() throws IOException, SQLException {

        DataSource ds = getDataSource();
        Connection conn = ds.getConnection();
        Statement s = conn.createStatement();
        s.execute("drop table scruffy if exists");
        s.execute("create table scruffy (name varchar(32))");
        ResultSet rset = s.executeQuery("select count(*) from wtf");
        rset.next();
        ;
        int rowcount = rset.getInt(1);
        assertEquals(0, rowcount);
        conn.commit();
    }
    //
    // @Test
    // public void test2() throws Exception, SqlSplitterException {
    // DatabaseInstrumentationProperties dip = new
    // DatabaseInstrumentationProperties(this);
    // DbloggerH2 dblogger = (DbloggerH2) dip.getDatabaseInstrumentation();
    // assertNotNull(dblogger);
    // int jobNbr = dblogger.beginJob("DbLoggerH2Test", getClass().getName(), null,
    // null, null);
    // System.out.println(dblogger.getUtProcessStatus(jobNbr));
    // dblogger.insertStep("step one");
    // dblogger.finishStep();
    // dblogger.insertStep("step two");
    // dblogger.finishStep();
    //
    // dblogger.endJob();
    // System.out.println(dblogger.getUtProcessStatus(jobNbr));
    //
    // }
}
