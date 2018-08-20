package org.javautil.dblogging;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.sql.DataSource;

import org.javautil.io.ResourceHelper;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class DbloggerPropertiesDataSource {

    private String resourceName;

    public DbloggerPropertiesDataSource(String resourceName) {
        super();
        this.resourceName = resourceName;
    }

    private static String getNotNull(Properties properties, String propertyName) {
        final String retval = properties.getProperty(propertyName);
        if (retval == null) {
            throw new IllegalArgumentException("No such property: " + propertyName);
        }
        return retval;
    }

    public static DataSource getDataSource(Properties properties) {
        final HikariConfig config = new HikariConfig();
        config.setJdbcUrl(getNotNull(properties, "dblogger.datasource.url"));
        config.setUsername(getNotNull(properties, "dblogger.datasource.username"));
        config.setPassword(getNotNull(properties, "dblogger.datasource.password"));
        config.setAutoCommit(false);
        return new HikariDataSource(config);
    }

    public Properties getApplicationProperties() throws IOException {
        InputStream input = null;

        final Properties properties = new Properties();
        try {
            input = ResourceHelper.getResourceAsInputStream(this, resourceName);
            properties.load(input);
        } catch (final IOException e) {
            if (input != null) {
                input.close();
            }
            throw new RuntimeException(e);
        }

        getNotNull(properties, "dblogger.datasource.driver-class-name");
        getNotNull(properties, "dblogger.datasource.url");
        getNotNull(properties, "dblogger.datasource.username");
        getNotNull(properties, "dblogger.datasource.password");
        input.close();
        return properties;
    }

    public DataSource getDataSource() throws IOException {
        return getDataSource(getApplicationProperties());
    }
}
