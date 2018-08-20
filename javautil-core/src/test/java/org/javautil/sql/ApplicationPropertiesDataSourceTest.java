package org.javautil.sql;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApplicationPropertiesDataSourceTest {

	static boolean befored = false;

	protected static DataSource dataSource;
	static Logger logger = LoggerFactory.getLogger(ApplicationPropertiesDataSourceTest.class);

	@BeforeClass
	public static  void beforeClass() throws IOException, SQLException {

		final ApplicationPropertiesDataSource apds = new ApplicationPropertiesDataSource();
		apds.getApplicationProperties();
		dataSource = apds.getDataSource();
		final Connection conn = dataSource.getConnection();
		assertNotNull(conn);
		if (Dialect.getDialect(conn).equals(Dialect.H2)) {
			Statement nuke = conn.createStatement();
			nuke.execute("DROP ALL OBJECTS DELETE FILES");
			nuke.close();

		}
		logger.info("conn" + conn);
		conn.close();
		befored = true;

	}

//	@Test
//	public void test() throws SQLException, IOException {
//		final ApplicationPropertiesDataSource apds = new ApplicationPropertiesDataSource();
//		apds.getApplicationProperties();
//		final DataSource dataSource = apds.getDataSource();
//		final Connection conn = dataSource.getConnection();
//		assertNotNull(conn);
//		// can't close or we lose the h2 connection //
//		// conn.close();
//	}
}
