package org.javautil.sql;


import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SequenceHelper {

	private static final String oracleSequence = "select %s.nextval from dual";
	private static final String h2Sequence = "select nextval('%s')";
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final Connection connection;
	private String sequenceText;


	public SequenceHelper(Connection connection) throws SQLException {
		this.connection = connection;
		switch (Dialect.getDialect(connection)) {
		case ORACLE:
			sequenceText = oracleSequence;
			break;
		case H2:
			sequenceText = h2Sequence;
			break;
		default:
			throw new UnsupportedOperationException("dialect not supported");
		}
	}

	public long getSequence(String sequenceName) throws SQLException {
		final String selectText = String.format(sequenceText, sequenceName.toUpperCase());
		try {
			final Statement stmt = connection.createStatement();
			final ResultSet rset = stmt.executeQuery(selectText);
			rset.next();

			final long retval = rset.getLong(1);
			rset.close();
			stmt.close();
			return retval;
		} catch (final SQLException sqe) {
			final String message = String.format("sequenceName: '%s' sql: '%s' message: '%s'",
					sequenceName, selectText, sqe.getMessage());
			logger.error(message);
			throw new SQLException(String.format("while processing %s\n%s",
					message, sqe.getMessage()), sqe.getCause());
		}
	}
}
