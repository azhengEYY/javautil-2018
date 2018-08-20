package org.javautil.sql;

public class SqlStatementFormatter {
	public String format(SqlStatement statement) {
		final StringBuilder sb = new StringBuilder();
		sb.append(String.format("SqlStatement name: %s\n", statement.getName()));
		sb.append(String.format("sql:\n%s\n", statement.getSql()));
		if (statement.getDescription() != null) {
			sb.append(String.format("description:\n%s\n", statement.getDescription()));
		}
		return sb.toString();
	}
}