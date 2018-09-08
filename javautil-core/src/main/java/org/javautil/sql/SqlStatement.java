package org.javautil.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.javautil.util.ListOfLists;
import org.javautil.util.ListOfNameValue;
import org.javautil.util.NameValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// todo parse out strings
//https://stackoverflow.com/questions/51768480/parsing-pl-sql-with-regular-expressions-need-to-strip-out-the-literals/51768663#51768663
//'(''|[^'])*'
//a:=''' this is a string ^''^ is the delimiter' from dual
//b:=a' this is a string ^''^ is the delimiter' from dual
//b:=a  'dog' || ' this is a string  is the delimiter' from dual

// TODO convert StatementHelper and SqlStatement
// (?!\\B'[^']*)(:\\w+)(?![^']*'\\B)
// TODO need a NameValueNoCheck that doesn't look to  see if too many
public class SqlStatement {

	private static final Logger logger = LoggerFactory.getLogger(SqlStatement.class);
	private static final String pythonRegex = "%\\(([^)]*)\\)s";
	private static final String colonRegex = ":([a-zA-Z_0-9]*)";
	private static final Pattern pythonBindPattern = Pattern.compile(pythonRegex);
	private static final Pattern colonBindPattern = Pattern.compile(colonRegex);
	private String name;
	private String sql;
	private String sqlOrig;
	private String preparedSql;
	private String description;
	private String narrative;
	private Connection conn;
	private PreparedStatement preparedStatement = null;
	private boolean trace = false;
	private boolean batching = false;
	/**
	 *
	 */
	private Dialect dialect;
	public SqlStatement() {
		super();
	}

	public SqlStatement(SqlStatement stmt) {
		name = stmt.name;
		sql = stmt.sql;
		sqlOrig = stmt.sqlOrig;
		description = stmt.description;
		narrative = stmt.narrative;
		conn = stmt.conn;
	}

	public SqlStatement(String sql) {
		super();
		setSql(sql);
	}

	public SqlStatement(String name, String sql) {
		super();
		this.name = name;
		setSql(sql);
	}

	public SqlStatement(Map<String, String> map) {
		setName(map.get("name"));
		setSql(map.get("sql"));
		setNarrative(map.get("narrative"));
		setDescription(map.get("description"));

	}

	public SqlStatement(String name, String sql, String description, String narrative) {
		super();
		this.name = name;
		setSql(sql);
		this.description = description;
		this.narrative = narrative;

	}

	public SqlStatement(String sql, Connection connection) throws SQLException {
		setSql(sql);
		setConnection( connection);
	}

	public SqlStatement(Connection connection, String sql) throws SQLException {
		setSql(sql);
		setConnection(connection);
	}

	public static String noNamedBinds(String inSql, Binds binds) {
		String outSql = inSql;
		for (final String k : binds.keySet()) {
			outSql = outSql.replaceAll(':' + k, "?");
		}
		return outSql;

	}

	/**
	 * Replaces %(BIND_NAME)s with :BIND_NAME :param sql: :return: The sql with all
	 * of the binds in :bind format
	 *
	 * @param sql the SQL statement with %(bindname)s format bind variables
	 **/
	public static String toColonBinds(String sql) {

		final List<String> binds = findBinds(sql, pythonBindPattern);
		String newSql = sql;
		for (final String bind : binds) {
			final String pythonBind = "%(" + bind + ")s";
			final String colonBind = ":" + bind;
			newSql = newSql.replace(pythonBind, colonBind); //
		}
		return newSql;
	}

	public static List<String> findPythonBinds(String sql) {
		return findBinds(sql, pythonBindPattern);
	}

	/** TODO doesn't work with : in quoted strings as in
	 * to_char (current_timestamp, 'YYYY-MM-DD HH24:MI:SSXFF') || ',"' || 
	 * @param sql
	 * @param pattern
	 * @return
	 */
	public static List<String> findBinds(String sql, Pattern pattern) {
		if (sql == null) {
			throw new IllegalArgumentException("sql is null");
		}
		final List<String> retval = new ArrayList<>();

		final Matcher matcher = pattern.matcher(sql);
		while (matcher.find()) {
			final String grp = matcher.group(1);
			logger.debug("bind '" + grp + "'");
			if (grp.trim().length() > 0) {
			retval.add(grp);   // parsing plsql find := length 0
			}
		}
		logger.debug("found binds " + retval);

		return retval;
	}

	/**
	 * Convert :BIND_NAME to ?
	 *
	 * @param sql
	 * @return
	 */
	public String toQuestionBinds(String sql) {
		/**
		 * Replaces %(BIND_NAME)s with :BIND_NAME :param sql: :return: The sql with all
		 * of the binds in :bind format
		 **/
		final List<String> colonBinds = findBinds(sql, colonBindPattern);
		String newSql = sql;
		for (final String bind : colonBinds) {
			final String colonBind = ":" + bind;
			newSql = newSql.replace(colonBind, "?"); //
		}
		final List<String> pythonBinds = findBinds(sql, pythonBindPattern);
		for (final String bind : pythonBinds) {
			final String pythonBind = "%(" + bind + ")s";
			newSql = newSql.replace(pythonBind, "?"); //
		}
		if (trace) {
			logger.warn(String.format("toQuestionBinds: replaced\n%s\nwith:\n%s", sql, newSql));
		}
		return newSql;
	}

	public void setConnection(Connection conn) throws SQLException {
		if (conn == null) {
			throw new IllegalArgumentException("conn is null");
		}
		this.conn = conn;
		this.dialect = Dialect.getDialect(conn);
	}

	public PreparedStatement bind(Binds binds) throws SQLException {
		return bind(binds, sql);
	}

	public PreparedStatement bind(Binds binds, String sql) throws SQLException {
		prepare();
		final List<String> colonBindList = findBinds(sql, colonBindPattern);
		final List<String> pythonBindList = findBinds(sql, pythonBindPattern);
		List<String> bindList = new ArrayList<>();
		final boolean hasColonBinds = colonBindList.size() > 0;
		final boolean hasPythonBinds = pythonBindList.size() > 0;
		if (hasColonBinds && hasPythonBinds) {
			throw new SQLException("statement has two types of binds ");
		}
		if (hasColonBinds) {
			bindList = colonBindList;
		}
		if (hasPythonBinds) {
			bindList = pythonBindList;
		}
		logger.debug(String.format("binds: %s, sql: %s", binds, sql));
		int i = 1;
		for (final String bindName : bindList) {
			try {
				final Object value = binds.get(bindName);
				logger.debug("binding index " + i + " bindName: " + bindName + " value " + binds.get(bindName));
				if (value == null) {
					logger.debug("calling setNull for index " + i);
					preparedStatement.setNull(i, java.sql.Types.VARCHAR); // TODO WTF
				} else if (value instanceof java.sql.Timestamp) {
					java.sql.Timestamp ts = (java.sql.Timestamp) value;
					//System.out.println("binding " + i + " to " + ts.toString());
					preparedStatement.setTimestamp(i, ts);
				} else if (value instanceof java.util.Date) {
					final java.util.Date dtin = (java.util.Date) value;
					final java.sql.Date bindDate = new java.sql.Date(dtin.getTime());
					preparedStatement.setDate(i, bindDate);
				} else {
					preparedStatement.setObject(i, binds.get(bindName));
				}
			} catch (final SQLException e) {
				final String message = this.toString() + "\n" + binds + "\nexception: \n" + e.getMessage();
				logger.error(message);
				throw new SQLException(message);
			} catch (final IllegalArgumentException e) {
				final String message = this.toString() + "\n" + binds + "\nexception: \n" + e.getMessage();
				logger.error(message);
				throw new IllegalArgumentException(message, e);
			}
			i++;
		}
		return preparedStatement;
	}

	public void execute(Connection conn) throws SQLException {
		// TODO can this be reused
		final Statement stmt = conn.createStatement();
		stmt.execute(sql);
		stmt.close();
	}

	public ResultSet executeQuery(Binds binds) throws SQLException {
		bind(binds);
		ResultSet result;

		try {
			result = preparedStatement.executeQuery();
		} catch (final SQLException e) {
			final String message = toString() + "\nbinds: " + binds + "\n" + e.getMessage();
			logger.error(message);
			throw new SQLException(message);
		}
		return result;
	}

	public PreparedStatement prepare() throws SQLException {
		if (sql == null || sql.trim().length() == 0) {
			throw new IllegalArgumentException("sql is: '" + sql + "'");
		}
		if ((dialect == null) || dialect.useQuestionBinds()) {
			preparedSql = toQuestionBinds(sql);
			if (trace) {
				String msg = String.format("prepare() after toQuestionBinds:\nsql:\n%s\npreparedSql:\n%s",sql,preparedSql);
				logger.info(msg);
			}
		} else {
			preparedSql = sql;
		}
		if (trace) {
			logger.info("prepare(): sql:\n{}\nisPrepared:{}\npreparedSql:\n{}",sql,isPrepared(),preparedSql);
		}
			
		if (!isPrepared()) {
			if (conn == null) {
				throw new IllegalStateException("conn is null");
			}
			try {
				if (trace) {
					logger.info("prepare(): connection.prepareStatement:\n{}", preparedSql);
				}
				preparedStatement = conn.prepareStatement(preparedSql);
			} catch (final SQLException sqe) {
				final String message = String.format("While preparing %s\n%s", sql, sqe.getMessage());
				logger.error(message);
				throw sqe;
			}
		}
		return preparedStatement;
	}

	public void close() throws SQLException {
		if (preparedStatement != null) {
			preparedStatement.close();
		}
	}

	public SqlStatement useColonBind() {
		sql = toColonBinds(sqlOrig);
		return this;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the sql
	 */
	public String getSql() {
		return sql;
	}

	/**
	 * @param sql the sql to set
	 */
	public void setSql(String sql) {
		if (sql == null) {
			throw new IllegalArgumentException("sql may not be null");
		}
		this.sql = sql;
		this.sqlOrig = sql;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the narrative
	 */
	public String getNarrative() {
		return narrative;
	}

	/**
	 * @param narrative the narrative to set
	 */
	public void setNarrative(String narrative) {
		this.narrative = narrative;
	}

	// todo should use formatter
	@Override
	public String toString() {
		return format(this);
	}

	public String format(SqlStatement sqlStatement) {
		final StringBuilder sb = new StringBuilder();
		sb.append(String.format("SqlStatement name: %s\n", sqlStatement.getName()));
		sb.append(String.format("sql:\n%s\n", sqlStatement.getSql()));
		if (sqlStatement.getDescription() != null) {
			sb.append(String.format("description:\n%s\n", sqlStatement.getDescription()));
		}
		return sb.toString();
	}


	boolean isPrepared() {
		return preparedStatement != null;
	}


	public MappedResultSetIterator iterator(Connection connection, Binds binds) {
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("resource")
    public ListOfLists getListOfLists(Binds binds) throws SQLException {
		final ResultSet rset = executeQuery(binds);
		return ResultSetHelper.toListOfLists(rset);
	}

	public void checkConnectionArgument(Connection conn) {
		if (conn == null) {
			throw new IllegalArgumentException("Connection is null");
		}
	}

	public void checkConnection() {
		if (conn == null) {
			throw new IllegalStateException("setConnection not yet called");
		}
	}
	// TODO might be single use and preparing is a waste of time
		// if being passed a connection TODO!!! should reset the prepared Statement or check if the connection is different
		// allow mixing if so declared as in "setMixedConnections"?
	// TODO consolidate with executeUpdate
		public int executeUpdateNoError(Connection conn, Binds binds) throws SQLException {
			int retval = -1;
			if (trace) {
				//System.out.println("executeUpdate tracing " + trace);
				String message = String.format("executeUpdate sql:\n%s\nbinds:\n%s", sql,binds);
				System.out.println(message);
			}
			try {
				checkConnectionArgument(conn);
				setConnection(conn);
				prepare();
				if (binds != null) {
					bind(binds);
				}
				retval = preparedStatement.executeUpdate();
			} catch (final SQLException sqe) {
				final String message = String.format("While processing:\nsql:\n'%s'\nbinds:\n%s\n%s", sql, binds, sqe.getMessage());
				logger.debug(message);

			}
			return retval;
		}
		

	// TODO might be single use and preparing is a waste of time
	// if being passed a connection TODO!!! should reset the prepared Statement or check if the connection is different
	// allow mixing if so declared as in "setMixedConnections"?
	public int executeUpdate(Connection conn, Binds binds) throws SQLException {
		if (trace) {
			String message = String.format("executeUpdate sql:\n%s\nbinds:\n%s", sql,binds);
		System.out.println(message);
		}
		try {
			checkConnectionArgument(conn);
			setConnection(conn);
			prepare();
			if (binds != null) {
				bind(binds);
			}
	

			return preparedStatement.executeUpdate();
		} catch (final SQLException sqe) {
			final String message = String.format("While processing:\nsql:\n'%s'\nbinds:\n%s\n%s", sql, binds, sqe.getMessage());
			logger.error(message);
			throw new SQLException(message);
		}
	}

	public int executeUpdate(Connection conn) throws SQLException {
		checkConnectionArgument(conn);
		try {
			return conn.createStatement().executeUpdate(sql);
		} catch (final SQLException e) {
			logger.error(String.format("While executing:\n %s\nencountered\n%s\n", sql, e.getMessage()));
			throw e;
		}
	}

	public ListOfNameValue getListOfNameValue(Connection conn, boolean toLowerCase) throws SQLException {
		// TODO can this be a prepared statement
		checkConnectionArgument(conn);
		final Statement s = conn.createStatement();
		final ResultSet rset = s.executeQuery(sql);
		ListOfNameValue retval =  ResultSetHelper.getListOfNameValue(rset, toLowerCase);
		rset.close();
		s.close();
		return retval;
	}
	
	
	public void executeBatch() throws SQLException {
        if (batching) {
        preparedStatement.executeBatch();
        batching = false;
        }
    }

	
	public void clearBatch() throws SQLException {
	    if (batching) {
	    preparedStatement.clearBatch();
	    batching = false;
	    }
	}
	
	
	public void addBatch(Binds binds) throws SQLException {

	        if (trace) {
	            String message = String.format("executeUpdate sql:\n%s\nbinds:\n%s", sql,binds);
	        System.out.println(message);
	        }
	        try {
	            checkConnectionArgument(conn);
	            setConnection(conn);
	            prepare();
	            if (binds != null) {
	                bind(binds);
	            }
	    
	            batching = true;
	            preparedStatement.addBatch();
	            
	        } catch (final SQLException sqe) {
	            final String message = String.format("While processing:\nsql:\n'%s'\nbinds:\n%s\n%s", sql, binds, sqe.getMessage());
	            logger.error(message);
	            throw new SQLException(message);
	        }
	    
	}

	public int executeUpdate(Binds b) throws SQLException {
		/* nasty hack for statements with : in 'yy:mm:dd' type strings*/
		
		return executeUpdate(conn, b);
	}

	public int executeUpdateShow(Binds b) throws SQLException {
		logger.info("sql:\n{}\nbinds:\n{}", getSql(), b.toString());
		return executeUpdate(b);
	}

	public NameValue getNameValue(Binds binds, boolean lowerCase) throws SQLException {
		final ResultSet rset = executeQuery(binds);
		NameValue retval = null;
		try {
			rset.next();
		} catch (final SQLException sqe) {
			final String message = String.format("getNameValue for %s\nbinds: %s returned no rows", sql, binds);
			logger.error(message);
			rset.close();
			throw new DataNotFoundException(message, sqe);
		}
		try {
			retval = ResultSetHelper.getNameValue(rset, lowerCase);
		} catch (final SQLException sqe) {
			final String message = String.format("getNameValue for %s\nbinds: %s returned no rows", sql, binds);
			logger.error(message);
			throw new DataNotFoundException(message, sqe);
		} finally {
			rset.close();
		}
		return retval;
	}


	public ListOfNameValue getListOfNameValue(Binds binds, boolean toLowerCase) throws SQLException {
		return ResultSetHelper.getListOfNameValue(executeQuery(binds), toLowerCase);
	}

	public ListOfNameValue getListOfNameValue(Binds binds) throws SQLException {
		return getListOfNameValue(binds, true);
	}

	public NameValue getNameValue() throws SQLException {
		final boolean namesToLower = true;
		return getNameValue(new Binds(), namesToLower);
	}

	public boolean isTrace() {
		return trace;
	}

	public void setTrace(boolean trace) {
//		if (trace) {
//			Thread.dumpStack();
	//	}
		this.trace = trace;
	}

}