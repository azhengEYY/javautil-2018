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

public class CreateConditionIdentificationDatabaseObjects {
	private static final String condiDdl = "condition_identification/ut_condition_tables.sr.sql";
	private static final String conditionObjectsDrop = "condition_identification/condition_identification_drop.sr.sql";
	SqlRunner runner;
	
	private static final String version = "1.2.1";

	Logger logger = LoggerFactory.getLogger(getClass());
	
	private boolean drop;
	//private Connection connection;
	
	private boolean showSql = false;
	private Connection connection;
	
	public CreateConditionIdentificationDatabaseObjects(Connection connection) throws IOException, SqlSplitterException {
	    logger.info("running version " + version + " " + condiDdl);
		runner = new SqlRunner(this,condiDdl);
		this.connection = connection;
		runner.setConnection(connection);
		runner.setSqlStatementTrace(true);
	
	}
	
	public void process() throws SQLException, SqlSplitterException, IOException {
	    if (drop) {
	        drop();
	    }
		runner.setPrintSql(showSql);
		runner.process();
		SqlStatement ss = new SqlStatement(connection, "select count(*) ut_condition_row_msg_count from ut_condition_row_msg");
		NameValue rowCount = ss.getNameValue();
		logger.info(rowCount.toString());
	}
	
	public void drop() throws SqlSplitterException, IOException, SQLException {
	    logger.info("dropping condition objects");
	    SqlRunner sr = new SqlRunner(this,conditionObjectsDrop);
	    sr.setConnection(connection);
	    sr.setPrintSql(true);
	    sr.setContinueOnError(true);
	    sr.process();
	}

	public boolean isShowSql() {
		return showSql;
	}

	public CreateConditionIdentificationDatabaseObjects setShowSql(boolean showSql) {
		this.showSql = showSql;
		return this;
	}

    public boolean isDrop() {
        return drop;
    }

    public CreateConditionIdentificationDatabaseObjects setDrop(boolean drop) {
        this.drop = drop;
        return this;
    }
}
