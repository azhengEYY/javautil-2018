package org.javautil.dblogging;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import javax.sql.DataSource;

import org.javautil.io.ResourceHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class H2LoggerDataSource {
    private Logger logger = LoggerFactory.getLogger(getClass());

    private String getRequired(Properties properties, String name) {
        String retval = properties.getProperty(name);
        if (retval == null) {
            throw new IllegalArgumentException("no such property: '" + name + "'");
        }
        return retval.trim();
    }

    public DataSource getPopulatedH2FromDbLoggerProperties() throws IOException, SQLException {
        return getPopulatedH2FromDbLoggerProperties(this);
    }

    private DataSource getPopulatedH2FromDbLoggerProperties(Object invoker) throws IOException, SQLException {

        InputStream dbloggerPropertiesStream = ResourceHelper.getResourceAsInputStream(invoker, "dblogger.properties");
        Properties properties = new Properties();
        properties.load(dbloggerPropertiesStream);
        System.out.println(properties.toString());
        return getPopulatedH2(properties);
    }

    private DataSource getPopulatedH2(Properties properties) throws SQLException {
        final HikariConfig config = new HikariConfig();

        config.setJdbcUrl(getRequired(properties, "dblogger.datasource.url"));
        config.setUsername(getRequired(properties, "dblogger.datasource.username"));
        config.setPassword(getRequired(properties, "dblogger.datasource.password"));
        config.setAutoCommit(true);

        DataSource dataSource = new HikariDataSource(config);
        Connection connection = dataSource.getConnection();
        System.out.println("before H2Install");
        H2Install installer = new H2Install(connection).setNoFail(true).setDrop(false).setShowSql(false);
        try {
            installer.process();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        connection.commit();
        connection.close();
        return dataSource;

    }

    // TODO normalize don't need a new one,
    private DataSource getPopulatedH2() throws SQLException {
        logger.info("creating logger h2");
        final HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:h2:/tmp/dbloggerh2p;AUTO_SERVER=TRUE");
        config.setUsername("sr");
        config.setPassword("tutorial");
        config.setAutoCommit(true);

        DataSource dataSource = new HikariDataSource(config);
        Connection connection = dataSource.getConnection();
        Statement s = connection.createStatement();

        connection.commit();
        connection.close();
        // DataSource dataSource = getDataSourceDblogger2();
        connection = dataSource.getConnection();
        System.out.println("getDatabaseInstrumentation connection: " + connection);
        s = connection.createStatement();
        connection.commit();
        H2Install installer = new H2Install(connection).setNoFail(false).setDrop(true).setShowSql(true);
        try {
            installer.process();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        connection.close();
        return dataSource;

    }
}