package org.javautil.oracle;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import org.javautil.sql.Binds;
import org.javautil.sql.ConnectionHelper;
import org.javautil.sql.SqlStatement;
import org.javautil.util.ListOfNameValue;
import org.javautil.util.NameValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import oracle.jdbc.OracleConnection;

/**
 * References https://docs.oracle.com/cd/B28359_01/server.111/b28320/dynviews_3027.htm#REFRN30232
 *
 * @author jjs
 * https://docs.oracle.com/cd/B28359_01/server.111/b28310/diag006.htm#ADMIN12484
 */
public class OracleConnectionHelper extends ConnectionHelper {

	public static final String startTraceCall = "begin dbms_session.set_sql_trace(true); end;";
	private final static Logger log = LoggerFactory.getLogger(OracleConnectionHelper.class);
	public static boolean isOracleConnection(Connection connection) {
		if (connection == null) {
			throw new IllegalArgumentException("connection is null");
		}
		try {
			return connection.isWrapperFor(OracleConnection.class);
		} catch (final SQLException e) {
			log.error("Error when checking isOracleConnection: {}", e.getMessage());
			throw new RuntimeException(e);
		}
	}

	public static void purgeRdbmsPipe(Connection conn, String pipeName) throws SQLException {
		final String text = String.format("begin sys.dbms_pipe.purge('%s'); end;", pipeName);
		log.info(String.format("text: %s", text));
		final CallableStatement purgeStatement = conn.prepareCall(text);
		purgeStatement.execute();
		purgeStatement.close();
	}

	public static void testConnection(Connection connection) throws SQLException {
		final Statement s = connection.createStatement();
		s.close();
	}

	public static String getPLSQLErrors(Connection connection) throws SQLException {
		String retval = null;

		final String sql = "	select name, type, sequence, line, position, text, attribute, message_number\n"
				+ "		from user_errors\n" + "		order by name, sequence";
		final SqlStatement errorStatement = new SqlStatement(connection, sql);
		final ListOfNameValue lonv = errorStatement.getListOfNameValue(new Binds(), true);
		final StringBuilder sb = new StringBuilder();
		for (final NameValue nv : lonv) {
			final String line = String.format("%s %d, %s\n", nv.getString("name"), nv.getLong("line"), nv.getString("text"));
			sb.append(line);
		}
		if (sb.length() > 0) {
			retval = sb.toString();
		}
		return retval;

	}

//	public static void clobWrite(Clob clob, Writer writer) throws SQLException, IOException {
//		final int length = 1024 * 1024;
//		final Reader reader = clob.getCharacterStream();
//		final char[] buffer = new char[length];
//		int count;
//		while ((count = reader.read(buffer)) != -1) {
//			writer.write(buffer, 0, count);
//		}
//		clob.free();
//	    reader.close();
//	}

	public static String getDuplicateCursors(Connection connection) throws SQLException {
		final SqlStatement dupeCursors = new SqlStatement(connection, "select * from my_duplicate_cursor");
		final StringBuilder sb = new StringBuilder();
		final ListOfNameValue dupes = dupeCursors.getListOfNameValue(new Binds());
		for (final NameValue nv : dupes) {
			sb.append(String.format("count: %d sqlText: %s\n", nv.getLong("sql_count"), nv.getString("sql_text")));
		}
		dupeCursors.close();
		return sb.toString();
	}

	public static String getCursors(Connection connection) throws SQLException {
		final String sql = "select count(*) sql_count, sql_text from my_open_cursor group by sql_text";
		final SqlStatement cursors = new SqlStatement(connection, sql);
		final StringBuilder sb = new StringBuilder();
		final ListOfNameValue cursorsRows = cursors.getListOfNameValue(new Binds());
		for (final NameValue nv : cursorsRows) {
			((BigDecimal) nv.get("sql_count")).longValue();
			sb.append(String.format("count: %d sqlText: %s\n", nv.getLong("sql_count"), nv.get("sql_text")));
		}
		cursors.close();
		return sb.toString();
	}

	public void commitNoWait(Connection connection) {
		final String commitBatchNoWaitText = "commit work write batch nowait";
		try {

			final PreparedStatement ps = connection.prepareStatement(commitBatchNoWaitText);
			ps.executeUpdate();
			ps.close();

		} catch (final SQLException e) {
			log.error("Cannot commit because: {}", e);
			throw new RuntimeException(e);
		}
	}

	public void kill(Connection connection, int sid, int serialNbr) throws SQLException {
		final String killSQL = String.format("alter system kill session(%d,%d)", sid, serialNbr);
		final Statement killStatement = connection.createStatement();
		killStatement.execute(killSQL);
		killStatement.close();

	}

}
