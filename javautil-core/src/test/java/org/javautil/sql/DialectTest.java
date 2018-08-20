package org.javautil.sql;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import org.junit.Test;

public class DialectTest {

	// TODO test agains a variety of data sources
	//@Test
	public void oracleTest() throws SQLException, IOException {
		final ApplicationPropertiesDataSource apds = new ApplicationPropertiesDataSource();
		final Connection oracleConnection = apds.getDataSource().getConnection();
		assertEquals(Dialect.ORACLE, Dialect.getDialect(oracleConnection));
		oracleConnection.close();
	}

	@Test
	public void h2Test() throws SQLException, ClassNotFoundException {
		final Connection conn = H2InMemory.getConnection();
		assertEquals(Dialect.H2,Dialect.getDialect(conn));
		conn.close();
	}
}
