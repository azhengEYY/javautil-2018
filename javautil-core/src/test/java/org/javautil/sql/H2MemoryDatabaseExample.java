package org.javautil.sql;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.BeforeClass;
import org.junit.Test;


public class H2MemoryDatabaseExample {

	private static final String DB_DRIVER = "org.h2.Driver";
	/**
	 * to keep the content of an in-memory database as long as the virtual machine is alive,
	 * use jdbc:h2:mem:test;DB_CLOSE_DELAY=-1.
	 */
	private static final String DB_CONNECTION_TEST = "jdbc:h2:mem:ext;DB_CLOSE_DELAY=-1";
	private static final String DB_CONNECTION = "jdbc:h2:mem:";
	private static final String DB_USER = "";
	private static final String DB_PASSWORD = "";

	private static final String createTableSql = "create table x (y number(9))";
	//private final Logger logger = LoggerFactory.getLogger(getClass());

	@BeforeClass
	public static void beforeClass() throws ClassNotFoundException
	{
		Class.forName(DB_DRIVER);
	}


	@Test
	public void memTest() throws SQLException {
		createTable(DB_CONNECTION);
	}

	@Test(expected = SQLException.class)
	/** With DB_CLOSE_DELAY set to -1 this should fail */
	public void memExtTest() throws SQLException {
		createTable(DB_CONNECTION_TEST);
	}

	@SuppressWarnings("resource")
	public void createTable(final String url) throws SQLException {
		Connection connection = DriverManager.getConnection(url, DB_USER, DB_PASSWORD);
		Statement s = connection.createStatement();
		s.execute(createTableSql);
		connection.close();

		connection = DriverManager.getConnection(url, DB_USER, DB_PASSWORD);
		s = connection.createStatement();
		s.execute(createTableSql);
		s.close();
		connection.close();
	}

	@Test
	public void testH2ConnectionHelper() throws SQLException, IOException {
		final Connection connection = DriverManager.getConnection("jdbc:h2:/tmp/x.dbf", DB_USER, DB_PASSWORD);

		H2ConnectionHelper.getDatabaseFile(connection);
		connection.close();
	}


}
