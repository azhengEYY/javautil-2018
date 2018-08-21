package org.javautil.dblogging;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import javax.sql.DataSource;

import org.javautil.io.ResourceHelper;
import org.javautil.sql.Dialect;
import org.javautil.sql.SqlSplitterException;
import org.javautil.util.ListOfNameValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class DatabaseInstrumentationProperties {
    private Properties properties;
    Logger             logger = LoggerFactory.getLogger(getClass());

    // Todo could examine stack
    public DatabaseInstrumentationProperties(Object invoker) throws IOException {
        InputStream dbloggerPropertiesStream = ResourceHelper.getResourceAsInputStream(invoker, "dblogger.properties");
        properties = new Properties();
        properties.load(dbloggerPropertiesStream);
    }

    String getRequired(String name) {
        String retval = properties.getProperty(name);
        if (retval == null) {
            throw new IllegalArgumentException("no such property: '" + name + "'");
        }
        return retval.trim();
    }

    // public DataSource getDataSource() throws IOException, SQLException {
    // // TODO
    // // String driver = getRequired("dblogger.datasource.driver-class");
    // String url = getRequired("dblogger.datasource.url");
    // String username = getRequired("dblogger.datasource.username");
    // String password =getRequired("dblogger.datasource.password");
    // String message = String.format("url: '%s' username: '%s', password: '%s'",
    // url, username, password);
    // logger.info(message);
    // DataSource ds = DataSourceFactory.getDataSource(url,username,password);
    // Connection connection = ds.getConnection();
    // Statement s = connection.createStatement();
    // s.close();
    // return ds;
    // }

    public DataSource getDataSource() throws SQLException {

        final HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:h2:/tmp/dbloggerh2;AUTO_SERVER=TRUE");
        config.setUsername("sr");
        config.setPassword("tutorial");
        config.setAutoCommit(false);

        DataSource ds = new HikariDataSource(config);
        Connection connection = ds.getConnection();
        Statement s = connection.createStatement();

        s.execute("drop table wtf if exists");
        s.execute("create table wtf(id number(9))");
        s.close();
        connection.commit();
        connection.close();
        return ds;
    }

    public DataSource getPopulatedH2() throws Exception, SqlSplitterException {
        DataSource dataSource = getDataSource();
        Connection connection = dataSource.getConnection();
        System.out.println("getDatabaseInstrumentation connection: " + connection);
        Statement s = connection.createStatement();
        s.execute("drop table wtf if exists");
        s.execute("create table wtf(id number(9))");
        connection.commit();
        H2Install installer = new H2Install(connection);
        installer.setDrop(true).setNoFail(true).setShowSql(true);
        installer.process();
        connection.commit();
        connection.close();
        return dataSource;

    }

    public Dblogger getDatabaseInstrumentation() throws Exception, SqlSplitterException {
        DataSource dataSource = getDataSource();
        Connection connection = dataSource.getConnection();

        Dblogger dblogger = new DatabaseLoggerNoOperation();
        switch (Dialect.getDialect(connection)) {
        case ORACLE:
            dblogger = new DbloggerForOracle(connection);
            break;
        case H2:
            System.out.println("getDatabaseInstrumentation connection: " + connection);
            Statement s = connection.createStatement();
            s.execute("drop table wtf if exists");
            s.execute("create table wtf(id number(9))");
            connection.commit();
            CreateDbloggerDatabaseObjects installer = new H2Install(connection).setDrop(true).setNoFail(true)
                    .setShowSql(true);
            installer.process();
            connection.commit();
            System.out.println("installed objects and commited");
            System.out.println("connection " + connection);
            dblogger = new DbloggerH2(connection);
            break;

        default:
            return new DatabaseLoggerNoOperation();
        }
        AbstractDblogger dbloggerh2 = (AbstractDblogger) dblogger;
        long jobNbr = dblogger.beginJob("DbLoggerH2Test", getClass().getName(), null, null, null, null);
        ListOfNameValue jobEntry = dbloggerh2.getUtProcessStatus(jobNbr);
        dbloggerh2.insertStep("step one", null, null);
        dbloggerh2.finishStep();
        dbloggerh2.insertStep("step two", null, null);
        dbloggerh2.finishStep();
        // System.out.println(jobEntry);
        logger.info("jobEntry:\n{}", jobEntry);
        dblogger.endJob();
        return dblogger;
    }

}
