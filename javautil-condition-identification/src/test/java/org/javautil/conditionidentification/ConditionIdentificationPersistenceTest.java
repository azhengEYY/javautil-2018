package org.javautil.conditionidentification;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.javautil.sql.ApplicationPropertiesDataSource;
import org.javautil.sql.ApplicationPropertiesDataSourceTest;
import org.javautil.sql.SqlSplitterException;
import org.junit.Test;

public class ConditionIdentificationPersistenceTest  {

	@Test
	public void testOracle() throws SQLException, IOException, SqlSplitterException {
	    DataSource dataSource  =    new ApplicationPropertiesDataSource().getDataSource(new ApplicationPropertiesDataSourceTest(),"oracle.application.properties");
		Connection connection = dataSource.getConnection();
		CreateConditionIdentificationDatabaseObjects cdo = new CreateConditionIdentificationDatabaseObjects(connection);
		cdo.setDrop(true);
		cdo.process();
		ConditionIdentificationPersistence condi = new ConditionIdentificationPersistence(connection,1);
		condi.purgeRun(1);
		
		connection.close();
		// checked yaml and sr load just by getting here.
	}
	
	// TOOD refactor copy paste
	@Test
    public void testh2() throws SQLException, IOException, SqlSplitterException {
        DataSource dataSource  =    new ApplicationPropertiesDataSource().getDataSource(new ApplicationPropertiesDataSourceTest(),"h2mem.application.properties");
        Connection connection = dataSource.getConnection();
        CreateConditionIdentificationDatabaseObjects cdo = new CreateConditionIdentificationDatabaseObjects(connection);
        cdo.setDrop(true);
        cdo.process();
        ConditionIdentificationPersistence condi = new ConditionIdentificationPersistence(connection,1);
        condi.purgeRun(1);
        
        connection.close();
        // checked yaml and sr load just by getting here.
    }
}
