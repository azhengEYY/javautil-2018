package org.javautil.dblogging;

//import static org.junit.Assert.assertTrue;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

//import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class H2LoggerDatasourceTest {
    // TODO why is this in src/main not src/test

    private Logger logger = LoggerFactory.getLogger(getClass());

    private DataSource getPopulatedH2() throws SQLException {
        logger.info("creating logger h2");
        final HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:h2:/tmp/dblogger");
        config.setUsername("sr");
        config.setPassword("tutorial");
        config.setAutoCommit(true);

        DataSource dataSource = new HikariDataSource(config);
        Connection connection = dataSource.getConnection();
        Statement s = connection.createStatement();
        logger.info("dropping all objects");
        s.execute("drop all objects");
        s.execute("create table job_log (job_log_id number(9))");
        connection.commit();
        System.out.println("getDatabaseInstrumentation connection: " + connection);

        connection.close();
        return dataSource;

    }

    public long testDataSource(DataSource ds) throws SQLException {
        final String count = "select count(*) cnt from job_log";
        Connection conn1 = ds.getConnection();
        Statement statement1 = conn1.createStatement();
        ResultSet rset1 = statement1.executeQuery(count);
        rset1.next();
        long cnt1 = rset1.getLong("cnt");
        rset1.close();
        conn1.close();
        return cnt1;
    }

    // @Test
    public void test() throws SQLException {
        DataSource h2loggerDataSource = getPopulatedH2();
        ;
        long count1 = testDataSource(h2loggerDataSource);
        logger.info("count1 {}", count1);
        //
        long count2 = testDataSource(h2loggerDataSource);
        logger.info("count2 {}", count2);

        Connection conn2 = h2loggerDataSource.getConnection();
        Statement h2stmt = conn2.createStatement();
        // blows up here with Table job_stat not found
        h2stmt.executeUpdate("insert into job_log (job_log_id) values (-2)");
        conn2.commit();

        long count3 = testDataSource(h2loggerDataSource);
        logger.info("count1 {} newCount {}", count1, count3);
        logger.info("connection " + conn2);
        assert (count3 > count1);
        File f = new File("/tmp/dblogger.mv.db");
        assert (f.exists());
        String connInfo = conn2.toString();
        assert (connInfo.endsWith("url=jdbc:h2:/tmp/dblogger user=SR"));
        conn2.close();
    }
}
