package org.javautil.sql;

import static org.junit.Assert.assertNotNull;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.junit.Test;

public class DataSourceFactoryTest {

    @Test
    public void test() throws SQLException {
        DataSource dataSource = DataSourceFactory.getInMemoryDataSource();
        Connection conn = dataSource.getConnection();
        Statement statement = conn.createStatement();
        assertNotNull(statement);
        statement.close();
        conn.close();

    }

    // doesn't work TODO @Test
    public void testSingleton() throws SQLException {
        DataSource dataSource = DataSourceFactory.getInMemoryDataSourceSingleton();
        Connection conn = dataSource.getConnection();
        Statement statement = conn.createStatement();
        assertNotNull(statement);
        statement.close();
        conn.close();
    }
}
