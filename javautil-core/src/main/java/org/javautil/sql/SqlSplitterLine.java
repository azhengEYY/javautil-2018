package org.javautil.sql;

public class SqlSplitterLine {
	private final int lineNumber;

	private int statementNumber;

	private int statementLineNumber;

	private final SqlSplitterLineType type;

	private SqlSplitterBlockType blockType = SqlSplitterBlockType.INDETERMINATE;

	private final String text;

	public SqlSplitterLine(int lineNumber, String text) {
		super();
		this.lineNumber = lineNumber;
		this.text = text;
		this.type = SqlSplitterLineType.getSqlSplitterLineType(text);

	}


	public int getLineNumber() {
		return lineNumber;
	}

	public int getStatementNumber() {
		return statementNumber;
	}

	public void setStatementNumber(int statementNumber) {
		this.statementNumber = statementNumber;
	}

	public int getStatementLineNumber() {
		return statementLineNumber;
	}

	public void setStatementLineNumber(int lineNbr) {
		statementLineNumber = lineNbr;

	}

	public String getText() {
		return text;
	}

	public SqlSplitterLineType getType() {
		return type;
	}

	public String toString(String preamble) {
		return String.format("%-20s %s", preamble, toString());
	}

	@Override
	public String toString() {
		return String.format(" %5d %4d %4d %-16s %-16s %s", lineNumber, statementNumber, statementLineNumber, type, blockType, text);
	}

	public SqlSplitterBlockType getBlockType() {
		return blockType;
	}

	public void setBlockType(SqlSplitterBlockType blockType) {
		this.blockType = blockType;
		//	logger.info("setBlockType: " + this.toString());
	}
}
