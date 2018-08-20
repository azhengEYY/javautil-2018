package org.javautil.sql;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map.Entry;

public class ConnectionHelper {

	public static void executeProcedure(Connection connection, final String sql, Binds binds) throws SQLException {
		final CallableStatement callableStatement = connection.prepareCall(sql);
		for (final Entry<String, Object> entry : binds.entrySet()) {
			callableStatement.setObject(entry.getKey(), entry.getValue());
		}
		callableStatement.executeUpdate();
		callableStatement.close();

	}


}