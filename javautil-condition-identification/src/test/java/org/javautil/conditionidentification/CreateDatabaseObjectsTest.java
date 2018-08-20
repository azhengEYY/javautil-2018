package org.javautil.conditionidentification;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import org.javautil.sql.ApplicationPropertiesDataSourceTest;
import org.javautil.sql.SqlSplitterException;
import org.javautil.sql.SqlStatement;
import org.junit.Before;

public class CreateDatabaseObjectsTest extends ApplicationPropertiesDataSourceTest {

	boolean initted = false;
	Connection connection;

	@Before
	public void before() throws SQLException, IOException {
	
		if (!initted) {
			connection = dataSource.getConnection();
			CreateDatabaseObjects cdo;
			try {
				cdo = new CreateDatabaseObjects(connection);
			} catch (SqlSplitterException e) {
				connection.close();
				throw new RuntimeException(e);
			}
			cdo.setShowSql(true);
			cdo.process();
			initted = true;
			testTables();
		}
	
	}

	void testTables() throws SQLException {
		SqlStatement ss = new SqlStatement(connection, "select count(*) row_count from ut_condition_row_msg");
		ss.getNameValue();
	}
//
//	@Test
//	public void test2() throws SQLException, IOException, SqlSplitterException {
//		SqlStatement ss = new SqlStatement(connection, "select count(*) row_count from ut_condition_row_msg");
//		NameValue rowCount = ss.getNameValue();
//		// Connection connection = dataSource.getConnection();
//		// CreateDatabaseObjects cdo = new CreateDatabaseObjects(connection);
//		// cdo.setShowSql(true);
//		// cdo.process();
//		// // test tables exist but just running to completion is good enough for now
//		// connection.close();
//		// // checked yaml and sr load just by getting here.
//	}
}
