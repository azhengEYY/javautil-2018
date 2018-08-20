package org.javautil.sql;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

public class SqlStatements implements Iterable<SqlStatement> {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());


	private List<SqlStatement> statements = new ArrayList<>();


	public List<SqlStatement> getStatements() {
		return statements;
	}

	private Connection connection;
	private String yamlFileName;

	public SqlStatements(Connection conn) {
		this.connection = conn;
	}

	public SqlStatements(InputStream is, Connection conn) {
		this.connection = conn;
		loadYamlList(is);
	}

	public SqlStatements(String yamlFileName, Connection conn) throws IOException {
		this.connection = conn;
		this.yamlFileName = yamlFileName;

		final File f = new File(yamlFileName);
		if (!f.exists()) {
			throw new FileNotFoundException("no such file:\n" + yamlFileName + "\n" + f.getAbsolutePath());
		}
		final InputStream ios = new FileInputStream(new File(yamlFileName));

		loadYamlList(ios);
		ios.close();


	}


	public SqlStatements(Connection conn, List<SqlStatement> statements) {
		setStatements(statements);
		setConnection(conn);
	}

	public SqlStatements(List<SqlStatement> statements) {
		setStatements(statements);
	}

	protected SqlStatements() {

	}

	public Connection getConnection() {
		return connection;
	}

	public void setConnection(Connection connection) {
		this.connection = connection;
	}

	protected void setStatements(List<SqlStatement> statements) {
		this.statements = statements;
	}


	void loadYamlList(InputStream is) {
		final Yaml yaml = new Yaml();
		try {
			yaml.load(is);
		} catch (final ClassCastException cce) {
			throw new IllegalArgumentException(cce.getMessage() + " Did you mean to use NamedSqlStatements?");
		}
	}

	//    public SqlStatement getSqlStatement(String statementName) {
	//        SqlStatement ss = sqlStatementByName.get(statementName);
	//        ss.setConnection(connection);
	//        return ss;
	//    }

	public List<Exception> close() {
		final List<Exception> exceptions = new ArrayList<>();
		for (final SqlStatement sh : statements) {

			try {
				sh.close();
			} catch (final SQLException se) {
				exceptions.add(se);
			}
		}
		return exceptions;
	}

	public String getYamlFileName() {
		return yamlFileName;
	}

	@Override
	public Iterator<SqlStatement> iterator() {
		return statements.iterator();
	}

	public void add(SqlStatement stmt) {
		statements.add(stmt);
	}

	public void processStatements(Connection conn, Binds binds) throws SQLException {
		for (final SqlStatement stmt : statements) {
			stmt.executeUpdate(conn, binds);
		}
	}

	SqlStatements withColonBinds() {
		final SqlStatements retval = new SqlStatements(this.connection);
		final ArrayList<SqlStatement> colonStatements = new ArrayList<>();
		for (final SqlStatement statement : statements) {
			SqlStatement newStatement = statement;
			final String oldSql = statement.getSql();
			final String colonSql = SqlStatement.toColonBinds(statement.getSql());
			if (!oldSql.equals(colonSql)) {
				newStatement = new SqlStatement(statement);
				newStatement.setSql(colonSql);
				final String message = String.format("converted:\n%s\nto\n %s\", "
						+ statement.getSql(), newStatement.getSql());
				logger.info(message);
			}

			colonStatements.add(newStatement);
		}
		colonStatements.trimToSize();
		retval.setStatements(this.statements);
		return retval;
	}

	void closeAll() throws SQLException {
		for (final SqlStatement statement : statements) {
			if (statement.isPrepared()) {
				statement.close();
			}
		}
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		for (final SqlStatement ss : statements) {
			sb.append(ss.toString());
			sb.append("\n");
		}
		return sb.toString();
	}

	public SqlStatement get(int i) {
		return statements.get(i);
	}

	public int size() {

		return statements.size();
	}
}
