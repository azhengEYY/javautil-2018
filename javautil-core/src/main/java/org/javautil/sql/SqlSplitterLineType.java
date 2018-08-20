package org.javautil.sql;

import java.util.regex.Pattern;

public enum SqlSplitterLineType {
	SQL, COMMENT, COMMENT_BLOCK_BEGIN, COMMENT_BLOCK_END,
	MARKDOWN_BLOCK_BEGIN, MARKDOWN_BLOCK_END, SQL_WITH_SEMICOLON, INDETERMINATE,
	PROCEDURE_BLOCK_END, PROCEDURE_BLOCK_START, STMT_END_NO_SQL, PROCEDURE_START_DIRECTIVE,
	STATEMENT_NAME, SEMICOLON;

	private static Pattern semiPattern = Pattern.compile(".*;\\s*--.*");
	public static SqlSplitterLineType getSqlSplitterLineType(String text) {
		final String trimmed = text.trim();

		if (trimmed.startsWith("--#<")) {
			return COMMENT_BLOCK_BEGIN;
		}
		if (trimmed.startsWith("--#>")) {
			return COMMENT_BLOCK_END;
		}
		if (trimmed.startsWith("--#")) {
			return COMMENT;
		}
		if (trimmed.startsWith("--::<")) {
			return MARKDOWN_BLOCK_BEGIN;
		}
		if (trimmed.startsWith("--::>")) {
			return MARKDOWN_BLOCK_END;
		}
		if (trimmed.equals("/")) {
			return PROCEDURE_BLOCK_END;
		}
		if (trimmed.startsWith("--/<")) {
			return PROCEDURE_START_DIRECTIVE;
		}
		if (trimmed.startsWith(";--")) {
			return STMT_END_NO_SQL;
		}
		if (trimmed.toUpperCase().startsWith("--@NAME")) {
			return STATEMENT_NAME;
		}
		if (trimmed.endsWith(";")) {
			if (trimmed.length() == 1) {
				return SEMICOLON;
			} else {
				return SQL_WITH_SEMICOLON;
			}
		}
		if (semiPattern.matcher(trimmed).matches()) {
			return SQL_WITH_SEMICOLON;
		}


		return INDETERMINATE;
	}
}
