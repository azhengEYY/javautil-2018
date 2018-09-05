package org.javautil.sql;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public class H2FileDatabase {

	public static Connection getConnection(File file, String username, String password) throws SQLException, ClassNotFoundException {
		Class.forName("org.h2.Driver");
		String fileName = file.getAbsolutePath();
		final Connection conn = DriverManager.getConnection("jdbc:h2:" + fileName, username, password);
		System.out.println("conn class " + conn.getClass().getCanonicalName());
		return conn;
	}
	
	


}
