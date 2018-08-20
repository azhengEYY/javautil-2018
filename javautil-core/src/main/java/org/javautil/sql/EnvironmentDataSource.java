package org.javautil.sql;

import java.util.Properties;

import javax.sql.DataSource;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class EnvironmentDataSource {

	public static final String DATABASE_URL = "DATABASE_URL";
	public static final String DATABASE_USERNAME = "DATABASE_USERNAME";
	public static final String DATABASE_PASSWORD = "DATABASE_PASSWORD";

	public EnvironmentDataSource() {
		super();
	}

	static String getNotNull(Properties properties, String propertyName) {
		final String retval = properties.getProperty(propertyName);
		if (retval == null) {
			throw new IllegalArgumentException("No such property: " + propertyName);
		}
		return retval;
	}

	static String getEnv(String name) {
		final String retval = System.getenv(name);
		if (retval == null) {
			throw new IllegalArgumentException("Unable to get environment variable " + name);
		}
		return retval;
	}

	public static Properties getEnvironmentProperties() {
		final Properties properties = new Properties();
		properties.put(DATABASE_URL, getEnv(DATABASE_URL));
		properties.put(DATABASE_USERNAME, getEnv(DATABASE_USERNAME));
		properties.put(DATABASE_PASSWORD, getEnv(DATABASE_PASSWORD));
		return properties;
	}

	public static DataSource getDataSource() {
		return getDataSource(getEnvironmentProperties());
	}

	public static DataSource getDataSource(Properties properties) {
		final HikariConfig config = new HikariConfig();
		config.setJdbcUrl(getNotNull(properties, DATABASE_URL));
		config.setUsername(getNotNull(properties, DATABASE_USERNAME));
		config.setPassword(getNotNull(properties, DATABASE_PASSWORD));
		config.setAutoCommit(false);
		return new HikariDataSource(config);
	}
}
