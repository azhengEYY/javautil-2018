package com.pacificdataservices.pdssr;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.javautil.dblogging.H2Install;
import org.javautil.sql.SqlRunner;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class LoggerDbInstallTest {
    public Logger logger = LoggerFactory.getLogger(getClass());

    void h2Install(Connection connection) throws Exception {
        H2Install installer = new H2Install(connection).setNoFail(false).setDrop(true).setShowSql(true);
        installer.process();
    }

    void localInstall(Connection connection) throws Exception {
        Statement s = connection.createStatement();
        s.execute("drop all objects");
        s.execute("create table ut_process_status (ut_process_status_id number(9))");
        connection.commit();
    }

    void verifyPartialInstall(Connection conn1) throws SQLException {
        Statement statement1 = conn1.createStatement();
        final String count = "select count(*) cnt from ut_process_status";
        ResultSet rset1 = statement1.executeQuery(count);
        rset1.next();
        long cnt1 = rset1.getLong("cnt");
        logger.info("cnt1 {}", cnt1);
    }

    void verifyFullInstall(Connection conn) throws SQLException {
        verifyPartialInstall(conn);
        //
        Statement statement1 = conn.createStatement();
        final String count = "select count(*) cnt from ut_process_step";
        ResultSet rset1 = statement1.executeQuery(count);
        rset1.next();
        long cnt1 = rset1.getLong("cnt");
        logger.info("cnt1 {}", cnt1);

    }

    private HikariDataSource getH2DataSource() throws Exception {
        logger.info("creating logger h2");
        final HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:h2:/tmp/dbloggerh2;AUTO_SERVER=TRUE");
        config.setUsername("sr");
        config.setPassword("tutorial");
        config.setAutoCommit(true);

        HikariDataSource dataSource = new HikariDataSource(config);
        // validate
        Connection connection = dataSource.getConnection();
        Statement s = connection.createStatement();
        s.close();
        connection.close();
        return dataSource;

    }

    // @Test
    public void testH2Install() throws Exception {
        HikariDataSource ds = getH2DataSource();
        Connection conn = ds.getConnection();
        h2Install(conn);
        verifyFullInstall(conn);

        File f = new File("/tmp/dbloggerh2.mv.db");
        assertTrue(f.exists());
        String connInfo = conn.toString();
        assertTrue(connInfo.endsWith("url=jdbc:h2:/tmp/dbloggerh2 user=SR"));
        ds.close();
        assertTrue(f.exists());
        ds = getH2DataSource();
        conn = ds.getConnection();
        h2Install(conn);
        verifyFullInstall(conn);
        conn.close();
        ds.close();
        assertTrue(f.exists());
        // h2connection.close();

    }

    @Test
    public void testSqlRunnerInstall() throws Exception {
        HikariDataSource ds = getH2DataSource();
        Connection conn = ds.getConnection();
        Statement s = conn.createStatement();
        s.execute("drop all objects");
        final String createTablesResource = "ddl/h2/dblogger_install_tables.sr.sql";
        new SqlRunner(this, createTablesResource).setConnection(conn).setPrintSql(true).setContinueOnError(false)
                .process();
        logger.info("SqlRunner complete");
        conn.commit();
        // h2Install(conn);
        // verifyFullInstall(conn);
        //
        // File f = new File("/tmp/dbloggerh2.mv.db");
        // assertTrue(f.exists());
        // String connInfo = conn.toString();
        // assertTrue(connInfo.endsWith("url=jdbc:h2:/tmp/dbloggerh2 user=SR"));
        // ds.close();
        // assertTrue(f.exists());
        // ds = getH2DataSource();
        // conn = ds.getConnection();
        // h2Install(conn);
        // verifyFullInstall(conn);
        // conn.close();
        // ds.close();
        // assertTrue(f.exists());
        // // h2connection.close();

    }

    // @Test
    public void testLocalInstallOneConnection() throws Exception {
        HikariDataSource ds = getH2DataSource();
        Connection conn = ds.getConnection();
        localInstall(conn);
        verifyPartialInstall(conn);

        File f = new File("/tmp/dbloggerh2.mv.db");
        assertTrue(f.exists());
        String connInfo = conn.toString();
        assertTrue(connInfo.endsWith("url=jdbc:h2:/tmp/dbloggerh2 user=SR"));
        ds.close();
        // h2connection.close();

    }

    // @Test
    public void testLocalInstallMultiConnection() throws Exception {
        HikariDataSource ds = getH2DataSource();
        Connection conn = ds.getConnection();
        localInstall(conn);
        Connection conn2 = ds.getConnection();
        verifyPartialInstall(conn2);

        File f = new File("/tmp/dbloggerh2.mv.db");
        assertTrue(f.exists());
        String connInfo = conn.toString();
        assertTrue(connInfo.endsWith("url=jdbc:h2:/tmp/dbloggerh2 user=SR"));
        ds.close();
        // h2connection.close();

    }
}
