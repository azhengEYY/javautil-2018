package org.javautil.conditionidentification;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import org.javautil.sql.SqlRunner;
import org.javautil.sql.SqlSplitterException;
import org.javautil.sql.SqlStatement;
import org.javautil.util.NameValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreateDatabaseObjects {
	private static final String condiDdl = "condition_identification/ut_condition_tables.sr.sql";
	
	SqlRunner runner;

	Logger logger = LoggerFactory.getLogger(getClass());
	//private Connection connection;
	
	private boolean showSql = false;
	private Connection connection;
	
	public CreateDatabaseObjects(Connection connection) throws IOException, SqlSplitterException {
	
		runner = new SqlRunner(this,condiDdl);
		this.connection = connection;
		runner.setConnection(connection);
		runner.setSqlStatementTrace(true);
	
	}
	
	public void process() throws SQLException {
		runner.setPrintSql(showSql);
		runner.process();
		SqlStatement ss = new SqlStatement(connection, "select count(*) ut_condition_row_msg_count from ut_condition_row_msg");
		NameValue rowCount = ss.getNameValue();
		logger.info(rowCount.toString());
	}

	public boolean isShowSql() {
		return showSql;
	}

	public CreateDatabaseObjects setShowSql(boolean showSql) {
		this.showSql = showSql;
		return this;
	}
}
