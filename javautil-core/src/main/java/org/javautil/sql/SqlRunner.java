package org.javautil.sql;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.javautil.io.ResourceHelper;
import org.javautil.lang.ThreadUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SqlRunner {
    private final Logger  logger                      = LoggerFactory.getLogger(this.getClass());

    private Connection    connection;
    private boolean       continueOnError;
    private boolean       printSql                    = false;
    private boolean       commit                      = false;

    private int           verbosity                   = 0;
    private String        inputName;
    private String        inputType;
    private boolean       proceduresOnly              = false;                                   // blocks only end in
                                                                                                 // "/"
    private SqlSplitter   splitter;
    private SqlStatements statements;

    private boolean       sqlStatementTrace           = false;
    private boolean       trace                       = false;
    private InputStream   splitterInputStream;
    private boolean       showError                   = true;

    private boolean       listStatementsBeforeExecute = false;
    private String constructorDescr;

    // TODO test that cursors are used once and closed;
    public SqlRunner() {

    }

    public SqlRunner(Object instantiator, String resourceName) throws IOException, SqlSplitterException {

        splitterInputStream = ResourceHelper.getResourceAsInputStream(instantiator, resourceName);
        splitter = new SqlSplitter(splitterInputStream);
        constructorDescr = "SqlRunner(Object instantiator, String resourceName)";
    }

    public SqlRunner(File inputFile) throws IOException, SqlSplitterException {
        splitterInputStream = new FileInputStream(inputFile);
        splitter = new SqlSplitter(splitterInputStream);
        constructorDescr = "SqlRunner(File inputFile) ";
    }

    public SqlRunner(Connection connection, SqlStatements sqlStatements) {
        this.connection = connection;
        this.statements = sqlStatements;
        logger.debug("statements size: {}", statements.size());
        constructorDescr = "SqlRunner(Connection connection, SqlStatements sqlStatements) ";
    }

    public SqlRunner(Connection connection, NamedSqlStatements sqlStatements, String... statementNames) {
        this.connection = connection;
        if (sqlStatements == null) {
            throw new IllegalArgumentException("sqlStatements is null");
        }
        ArrayList<SqlStatement> statementsToRun = sqlStatements.getStatements(statementNames);
        statements = new NamedSqlStatements(statementsToRun);
        logger.debug("statements size: {}", statements.size());
        constructorDescr = "SqlRunner(Connection connection, NamedSqlStatements sqlStatements, String... statementNames)";
    }

    public int getVerbosity() {
        return verbosity;
    }

    public SqlRunner setVerbosity(int verbosity) {
        this.verbosity = verbosity;
        return this;
    }

    public String getInputName() {
        return inputType + ":" + inputName;
    }

    public void processStatements(Connection connection, Binds binds, List<SqlStatement> statements)
			throws SQLException {

		logger.info("processStatements: showSql?: {}, continueOnError? {}",printSql,continueOnError);
	

		int statementNumber = 0;
		for (final SqlStatement ss : statements) {
			statementNumber++;
			if (ss.getSql() == null || ss.getSql().trim().length() == 0) {
				throw new IllegalStateException(String.format("sql is '%s'", ss.getSql()));
			}
			String name = ss.getName();
			if (sqlStatementTrace  || printSql) {
				if (name == null) {
					System.out.println(String.format("-- SqlRunner - #%d --\n%s", statementNumber, ss.getSql()));
				} else {
					System.out.println(String.format("-- SqlRunner - #%d name: '%s'\n%s", statementNumber, name, ss.getSql()));
				}
			}

			
			try { // TODO clean this crap up
				ss.setTrace(sqlStatementTrace);
				int rc;
			
					if (proceduresOnly) {
						rc = ss.executeUpdate(connection, null); // nasty hack for quoted : 'yy:mm:dd'
						//logger.info("execute 1 complete {}",rc);
					} else {
						rc = ss.executeUpdate(connection, binds);
						//  logger.info("execute 2 complete {}",rc);
					}
				
				ss.close();
			} catch (final SQLException s) {
				// TODO check if this is redundant with respect to SqlStatement perhaps a new
				// exception type with original and SqlStatement
			    final String message = String.format("While processing: \nSQL:'\n%s\n'\n%s", ss.getSql(),
                        s.getMessage());
			    if (showError) {
				
				logger.error(message);
			    }
				if (!continueOnError) {
					throw new SQLException(message, s);
				}
			}
		}
		if (commit) {
			connection.commit();
		}
	}

    public void process() throws SQLException {
        process(new Binds());
    }

    public void process(Binds binds) throws SQLException {
        if (statements == null && splitter != null) {

            statements = splitter.getSqlStatements();
        }

        List<SqlStatement> listOfStatements = statements.getStatements();

        if (listStatementsBeforeExecute || trace) {
            logger.info("--   ========= statementsToBeExecuted: " + listOfStatements.size());
            int i = 1;
            for (SqlStatement ss : listOfStatements) {
                String message = String.format("#%d:\n%s", i, ss.getSql());
                System.out.println(message);
                i++;
            }
        }

        processStatements(connection, binds, listOfStatements);
    }

    public SqlRunner setConnection(Connection connection) {
        this.connection = connection;
        return this;
    }

    public boolean isContinueOnError() {
        return continueOnError;
    }

    public SqlRunner setContinueOnError(boolean continueOnError) {
        this.continueOnError = continueOnError;
        return this;
    }

    public boolean isPrintSql() {
        return printSql;
    }

    public SqlRunner setPrintSql(boolean printSql) {
//        System.out.println(ThreadUtil.getStackTrace());
//        logger.info("setPrintSql: {} ================", printSql);
        this.printSql = printSql;
        return this;
    }

    public boolean isCommit() {
        return commit;
    }

    public SqlRunner setCommit(boolean commit) {
        this.commit = commit;
        return this;
    }

    public boolean isProceduresOnly() {
        return proceduresOnly;
    }

    public SqlRunner setProceduresOnly(boolean proceduresOnly) {
        this.proceduresOnly = proceduresOnly;
        splitter.setProceduresOnly(proceduresOnly);
        return this;
    }

    public SqlRunner setSqlStatementTrace(boolean traceSqlStatement) {
        this.sqlStatementTrace = traceSqlStatement;
        return this;
    }

    public boolean isTrace() {
        return trace;
    }

    public SqlRunner setTrace(boolean trace) {
        this.trace = trace;
        return this;
    }

    public boolean isShowError() {
        return showError;
    }

    public SqlRunner setShowError(boolean showError) {
        this.showError = showError;
        return this;
    }

    public boolean isListStatementsBeforeExecute() {
        return listStatementsBeforeExecute;
    }

    public SqlRunner setListStatementsBeforeExecute(boolean listStatementsBeforeExecute) {
        this.listStatementsBeforeExecute = listStatementsBeforeExecute;
        return this;
    }

    public SqlSplitter getSplitter() {
        return splitter;
    }
}
