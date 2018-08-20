package org.javautil.sql;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.javautil.util.ListOfLists;
import org.javautil.util.ListOfNameValue;
import org.javautil.util.NameValue;

public class ResultSetHelper {

	/**
	 * @param rset
	 * @param toLowerCase column names will be lowercase
	 * @return
	 * @throws SQLException
	 */

	public static String[] getColumnNames(ResultSet rset, boolean toLowerCase) throws SQLException {
		final ResultSetMetaData metaData = rset.getMetaData();
		final String[] columnNames = new String[metaData.getColumnCount()];
		for (int i = 1; i <= metaData.getColumnCount(); i++) {
			final String columnName = toLowerCase ? metaData.getColumnName(i).toLowerCase() : metaData.getColumnName(i);
			columnNames[i - 1] = columnName;
		}
		return columnNames;
	}

	/**
	 * @param rset
	 * @param lowerCase names will be lowercased,
	 * @return
	 * @throws SQLException
	 */
	public static NameValue getNameValue(ResultSet rset, boolean lowerCase) throws SQLException {
		NameValue nv;
		try {
			nv = getRowAsNameValue(rset, lowerCase);
		} catch (final SQLException sqe) {
			rset.close();
			throw new SQLException("could not get row? " + sqe.getMessage(), sqe);
		}
		//noinspection CatchMayIgnoreException
		try {
			rset.next();
			throw new TooManyRowsException();
		} catch (final SQLException sqe) {
		} finally {
			rset.close();
		}
		return nv;
	}

	public static NameValue getRowAsNameValue(ResultSet rset, boolean lowerCase) throws SQLException {
		final ResultSetMetaData metaData = rset.getMetaData();
		final String[] columnNames = getColumnNames(rset, lowerCase);
		final NameValue row = new NameValue();
		for (int i = 1; i <= metaData.getColumnCount(); i++) {
			row.put(columnNames[i - 1], rset.getObject(i));
		}
		return row;
	}

	public static NameValue getRowAsNameValue(ResultSet rset) throws SQLException {
		return getRowAsNameValue(rset, false);
	}

	public static List<Object> getRowAsList(ResultSet rset) throws SQLException {
		final ResultSetMetaData metaData = rset.getMetaData();
		final ArrayList<Object> row = new ArrayList<>(metaData.getColumnCount());
		for (int i = 1; i <= metaData.getColumnCount(); i++) {
			row.add(rset.getObject(i));
		}
		return row;
	}


	public static ListOfNameValue getListOfNameValue(ResultSet rset, boolean toLowerCase) throws SQLException {
		final ListOfNameValue list = new ListOfNameValue();
		while (rset.next()) {
			list.add(getRowAsNameValue(rset, toLowerCase));
		}
		rset.close();
		return list;
	}

	/**
	 * Convenience method for getListOfNameValue(rset, true)  lower case names
	 * @param rset
	 * @return
	 * @throws SQLException
	 */
	public static ListOfNameValue getListOfNameValue(ResultSet rset) throws SQLException {
		return getListOfNameValue(rset,true);
	}

	public static ListOfLists toListOfLists(ResultSet rset) throws SQLException {
		final ListOfLists rows = new ListOfLists();
		while (rset.next()) {
			rows.add(getRowAsList(rset));
		}
		rset.close();
		rows.trimToSize();
		return rows;
	}


}
