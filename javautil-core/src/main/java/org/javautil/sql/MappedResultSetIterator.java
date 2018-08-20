package org.javautil.sql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;

import org.javautil.util.NameValue;

public class MappedResultSetIterator implements Iterable<NameValue>, Iterator<NameValue> {

	private final ResultSet rset;

	private long rowCount = 0;

	public MappedResultSetIterator(ResultSet rset) {
		this.rset = rset;
	}

	@Override
	public boolean hasNext() {
		boolean hasMore = true;
		try {
			hasMore = rset.next();
			if (hasMore) {
				rowCount++;
			}
		} catch (final SQLException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		return hasMore;
	}

	@Override
	public NameValue next() {
		final NameValue binds = new NameValue();
		int colCount;
		// TODO this should come from ResultSetHelper
		try {
			colCount = rset.getMetaData().getColumnCount();
			for (int i = 1; i <= colCount; i++) {
				final String key = rset.getMetaData().getColumnName(i);
				final Object value = rset.getObject(i);
				binds.put(key, value);
			}
		} catch (final SQLException e) {
			throw new RuntimeException(e);
		}
		return binds;
	}

	@Override
	public Iterator<NameValue> iterator() {
		return this;
	}

	public long getRowCount() {
		return rowCount;
	}


}
