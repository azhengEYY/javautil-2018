package org.javautil.sql;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.sql.DataSource;

import org.javautil.io.ResourceHelper;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class DbloggerPropertiesDataSource {

	public DbloggerPropertiesDataSource() {
		super();
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
		config.setJdbcUrl(getNotNull(properties, "spring.datasource.url"));
		config.setUsername(getNotNull(properties, "spring.datasource.username"));
		config.setPassword(getNotNull(properties, "spring.datasource.password"));
		config.setAutoCommit(false);
		return new HikariDataSource(config);
	}

	public Properties getApplicationProperties() throws IOException {
		InputStream input = null;

		final Properties properties = new Properties();
		try {
			input = ResourceHelper.getResourceAsInputStream(this, "application.properties");
			properties.load(input);
		} catch (final IOException e) {
			if (input != null) {
				input.close();
			}
			throw new RuntimeException(e);
		}

		getNotNull(properties, "spring.datasource.driver-class-name");
		getNotNull(properties, "spring.datasource.url");
		getNotNull(properties, "spring.datasource.username");
		getNotNull(properties, "spring.datasource.password");
		input.close();
		return properties;
	}

	public DataSource getDataSource() throws IOException {
		return getDataSource(getApplicationProperties());
	}
}
