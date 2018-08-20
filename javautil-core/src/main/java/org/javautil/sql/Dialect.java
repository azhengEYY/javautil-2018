package org.javautil.sql;

import java.sql.Connection;
import java.sql.SQLException;

import org.slf4j.LoggerFactory;

public enum Dialect {

	POSTGRES, SQLITE, H2, ORACLE, UNSPECIFIED;

	public static Dialect getDialect(Connection connection) throws SQLException {
		LoggerFactory.getLogger(Dialect.class);
		if (connection.isWrapperFor(oracle.jdbc.OracleConnection.class)) {
			return ORACLE;
		}
		if (connection.isWrapperFor(org.h2.jdbc.JdbcConnection.class) ||
				connection.getClass().equals(org.h2.jdbc.JdbcConnection.class)) {
			return H2;
		}
	//	System.out.println(connection.getClass());
		throw new IllegalArgumentException("Unknown driver " + connection);
		//return UNSPECIFIED;
	}

	boolean useQuestionBinds() {
		switch (this) {
		case ORACLE:
			return false;
		default:
			return true;
		}
	}

}
