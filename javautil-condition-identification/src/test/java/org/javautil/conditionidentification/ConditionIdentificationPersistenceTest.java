package org.javautil.conditionidentification;

import java.io.IOException;
import java.sql.SQLException;

import org.javautil.sql.SqlSplitterException;
import org.junit.Test;

public class ConditionIdentificationPersistenceTest extends CreateDatabaseObjectsTest {

	@Test
	public void test1() throws SQLException, IOException, SqlSplitterException {
//		Connection connection = dataSource.getConnection();
		ConditionIdentificationPersistence condi = new ConditionIdentificationPersistence(connection,1);
		condi.purgeRun(1);
		
		connection.close();
		// checked yaml and sr load just by getting here.
	}
}
