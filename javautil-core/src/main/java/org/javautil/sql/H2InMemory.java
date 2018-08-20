package org.javautil.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public class H2InMemory {

	public static Connection getConnection() throws SQLException, ClassNotFoundException {
		Class.forName("org.h2.Driver");
		final Connection conn = DriverManager.getConnection("jdbc:h2:mem:", "", "");
		System.out.println("conn class " + conn.getClass().getCanonicalName());
		return conn;
	}


}
