package org.javautil.sql;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

public class NamedSqlStatements extends SqlStatements implements Iterable<SqlStatement> {

	//private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private Map<String, SqlStatement> sqlStatementByName = null;
	Logger logger = LoggerFactory.getLogger(getClass());
	public NamedSqlStatements(Connection conn) {
		super(conn);
	}

	public NamedSqlStatements(InputStream is, Connection conn) throws SQLException {
		super(conn);
		loadYamlMap(is);
		for (SqlStatement ss : sqlStatementByName.values()) {
			ss.setConnection(conn);
		}
	}

	public NamedSqlStatements(File yamlFile, Connection conn) throws IOException {
		super(conn);

		if (!yamlFile.exists()) {
			throw new FileNotFoundException("no such file:\n" + yamlFile + "\n" + yamlFile.getAbsolutePath());
		}
		final InputStream ios = new FileInputStream(yamlFile);
		loadYamlMap(ios);
		ios.close();
	}

	public NamedSqlStatements(Connection conn, List<SqlStatement> statements) {
		super(conn, statements);
	}

	public NamedSqlStatements(List<SqlStatement> statements) {
		super();
		setStatements(statements);
	}

	public NamedSqlStatements(SqlStatements ss) {
		super();
		setStatements(ss.getStatements());
	}
	
	public static NamedSqlStatements fromSqlSplitter(Connection conn, File srFileName) throws FileNotFoundException {
	    SqlSplitter sr = new SqlSplitter(srFileName);
	    NamedSqlStatements retval = new NamedSqlStatements(conn);
	    retval.setStatements(sr.getSqlStatementList());
	    return retval;
	}
	
	@Override
	public void setStatements(List<SqlStatement> statements) {
		super.setStatements(statements);
		sqlStatementByName = new TreeMap<>();
		for (final SqlStatement statement : statements) {
			String name = statement.getName();
			if (name == null) {
				throw new IllegalArgumentException("statement has no name");
			}
			final Object old = sqlStatementByName.put(name, statement);
			if (old != null) {
				throw new IllegalArgumentException("duplicate name " + statement.getName());
			}
		}
	}

	/**
	 * @param caller
	 * @param resourceName a SqlSplitter resource
	 * @return
	 * @throws IOException
	 * @throws SqlSplitterException 
	 */
	public static NamedSqlStatements getNameSqlStatementsFromSqlSplitterResource(Object caller, String resourceName) throws IOException, SqlSplitterException {
		final SqlSplitter sr = new SqlSplitter(caller, resourceName);
		final SqlStatements ss = sr.getSqlStatements();
		final NamedSqlStatements named = new NamedSqlStatements(ss);
		return named;
	}

	void loadYamlMap(InputStream is) {
		final Yaml yaml = new Yaml();
		@SuppressWarnings("unchecked")
		final Map<String, Map<String, String>> sqlMap = (Map<String, Map<String, String>>) yaml.load(is);
		sqlStatementByName = new LinkedHashMap<>();
		for (final Entry<String, Map<String, String>> e : sqlMap.entrySet()) {
			e.getValue().put("name", e.getKey());
			final SqlStatement ess = new SqlStatement(e.getValue());
			sqlStatementByName.put(e.getKey(), ess);
		}
	}

	
	public SqlStatement get(String statementName) {
		return getSqlStatement(statementName);
	}
	
	public SqlStatement getSqlStatement(String statementName) {
		final SqlStatement ss = sqlStatementByName.get(statementName);
		
		if (ss == null) {
			ArrayList<String> names = getStatementNames();
			String message = "no such statement '" + statementName + "' in " + names;
			logger.error(message);
			throw new IllegalArgumentException(message);
		}
		return ss;
	}

	@Override
	public List<Exception> close() {
		final List<Exception> exceptions = new ArrayList<>();
		for (final Entry<String, SqlStatement> e : sqlStatementByName.entrySet()) {
			final SqlStatement sh = e.getValue();
			try {
				sh.close();
			} catch (final SQLException se) {
				exceptions.add(se);
			}
		}
		return exceptions;
	}

	public ArrayList<String> getStatementNames() {
		Set<String> names = sqlStatementByName.keySet();
		ArrayList<String> retval = new ArrayList<>();
		retval.addAll(names);
	
		return retval;
	}

	/**
	 * Number of sql statements
	 *
	 * @return
	 */
	@Override
	public int size() {
		return sqlStatementByName.size();
	}
	

	public ArrayList<SqlStatement> getStatements(String ... statementNames) {
		ArrayList<SqlStatement> sqlStatements = new ArrayList<>();
		for (String name : statementNames) {
			sqlStatements.add(get(name));
		}
		return sqlStatements;
	}
}
