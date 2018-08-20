package org.javautil.sql;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class ConnectionUtil {
	/**
	 * Used in stress testing
	 * TODO move to ConnectionHelper?
	 *
	 * @param conn
	 * @param sql
	 * @return
	 * @throws SQLException
	 */
	public static int exhaustQuery(Connection conn, String sql) throws SQLException {
		final Statement s = conn.createStatement();
		final ResultSet rset = s.executeQuery(sql);
		int rowcount = 0;
		while (rset.next()) {
			rowcount++;
		}
		s.close();
		rset.close();
		return rowcount;
	}

}
