package org.javautil.sql;

import java.util.Properties;

import javax.sql.DataSource;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class DataSourceFactory {


	// check ENV DATASOURCE_PROPERTIES
	// check ENV DATASOURCE_URL etc
	// check application.properties
	// TODO
	public static DataSource getDataSource(String url, String username, String password) {
		final HikariConfig config = new HikariConfig();
		config.setJdbcUrl(url);
		config.setUsername(username);
		config.setPassword(password);
		config.setAutoCommit(false);

		return new HikariDataSource(config);
	}

	public static DataSource getInMemoryDataSource() {

		return getDataSource("jdbc:h2:mem:test", "sa", "tutorials");
	}

	public static DataSource getInMemoryDataSourceSingleton() {
		return getDataSource("jdbc:h2:mem", "sa", "tutorials");
	}

	public static DataSource getDataSourceSpringProperties(Properties properties) {
		return ApplicationPropertiesDataSource.getDataSource(properties);
	}

	public static DataSource getDataSourceFromEnvironment() {
		return EnvironmentDataSource.getDataSource();
	}
	
	public static DataSource getH2Permanent(String path, String username, String password) {
		return getDataSource("jdbc:h2:" + path, username, password);
	}
}

