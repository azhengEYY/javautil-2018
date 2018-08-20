package org.javautil.sql;

import java.sql.SQLException;

public class DataNotFoundException extends SQLException {

	/**
	 *
	 */
	private static final long serialVersionUID = 5741255065577422694L;

	public DataNotFoundException(SQLException sqe) {
		super(sqe);
	}

	public DataNotFoundException(String message) {
		super(message);
	}

	public DataNotFoundException(String message, SQLException sqe) {
		super(message, sqe);
	}

}
