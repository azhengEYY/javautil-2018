package org.javautil.sql;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.regex.Pattern;

import org.javautil.io.ResourceHelper;
import org.javautil.text.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO remove System commments
/**
 * --#< begin comment block This line and all lines to end comment block are not
 * returned in sql --#> end comment block This may be used to skip sqlplus
 * directives, for example <code> --#< set echo on set serveroutput on --#> The
 * above stat
 * <p>
 * <p>
 * --/< begin statement block --/> end statement block
 * <p>
 * Alternativel --/ / end statement block sqlplus compatability ends plsql code
 * block
 * <p>
 * Can be a pair --::< begin of markdown block may include sql or procedural
 * code --::> end of markdown block --:: markdown or Representational Text
 */
// select name, type, sequence, line, position, text, attribute, message_number
// from user_errors
// order by name, sequence

// TODO need to close input stream after read exhausted or exception

public class SqlSplitter {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	LineNumberReader reader;
	boolean traceAnalyze = false;

	private int verbosity = 0;
	private InputStream is;
	private String inputName;
	private String inputType;
	private String inLine;
	private ArrayList<SqlSplitterLine> lines;
	private boolean analyzed = false;
	private int traceState = 0;
	private final static Pattern annotationPattern = Pattern.compile("^ *--@.*");
	private boolean trace = false;

	private boolean proceduresOnly;

	public SqlSplitter() {

	}

	// TODO accept a String Constructor
	public SqlSplitter(Object instantiator, String resourceName) throws FileNotFoundException {
		this.inputName = resourceName;
		this.inputType = "resource";
		// TODO dedupe
		this.is = ResourceHelper.getResourceAsInputStream(instantiator, resourceName);
	
		final InputStreamReader isr = new InputStreamReader(is);
		reader = new LineNumberReader(isr);
	}

	public SqlSplitter(File inputFile) throws FileNotFoundException {
		this.inputName = inputFile.getAbsolutePath();
		this.inputType = "file";
		this.is = new FileInputStream(inputFile);
		if (this.is == null) {
			final String message = "unable to open file: '" + inputFile.getAbsolutePath() + "'";
			logger.error(message);
			throw new IllegalStateException(message);
		}
		final InputStreamReader isr = new InputStreamReader(is);
		reader = new LineNumberReader(isr);
	}

	public SqlSplitter(InputStream splitterInputStream) {
		reader = new LineNumberReader(new InputStreamReader(splitterInputStream));
	}

	public int getVerbosity() {
		return verbosity;
	}

	public SqlSplitter setVerbosity(int verbosity) {
		this.verbosity = verbosity;
		return this;
	}

	public String lineInfo() {
		return String.format("line# %d, '%s'", reader.getLineNumber(), inLine);
	}

	public ArrayList<SqlSplitterLine> processLines() {
		try {
		if (lines == null) {
			lines = new ArrayList<>();
			String line;
			while ((line = reader.readLine()) != null) {
				lines.add(new SqlSplitterLine(reader.getLineNumber(), line));
			}
		}
		return lines;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * A very simple finite state machine.
	 * 
	 * @throws IOException
	 * @throws SqlSplitterException
	 */
	public void analyze() throws SqlSplitterException {
		if (analyzed) {
			return;
		}
		logger.debug("SqlSplitter proceduresOnly{}",proceduresOnly);
		final int NO_BLOCK = 0;
		final int IN_SQL_BLOCK = 1;
		final int IN_COMMENT_BLOCK = 2;
		final int IN_PROCEDURE_BLOCK = 3;
		processLines();
	

		int state = NO_BLOCK;
		int statementNumber = 0;
		int statementLineNumber = 1;
		// TODO test name no sql
		// TODO exception with line
		int lineNumber = 1;
		logger.debug("SqlSplitter:analyze traceState:{} " , traceState);
		for (final SqlSplitterLine srl : lines) {
			if (traceState > 3) {
				System.out.println("state " + state);
				System.out.println(srl.toString("analyze"));
			}

			switch (srl.getType()) {
			case PROCEDURE_START_DIRECTIVE:
				state = IN_PROCEDURE_BLOCK;
				statementLineNumber = 1;
				statementNumber++;
				break;
			case COMMENT_BLOCK_BEGIN:
				srl.setBlockType(SqlSplitterBlockType.COMMENT);
				state = IN_COMMENT_BLOCK;
				break;
			case COMMENT_BLOCK_END:
				state = NO_BLOCK;
				srl.setBlockType(SqlSplitterBlockType.COMMENT);
				break;
			case COMMENT:
				srl.setBlockType(SqlSplitterBlockType.COMMENT);
				break;
			case SEMICOLON:
				if (state == IN_SQL_BLOCK) {
					state = NO_BLOCK;
				}
				// TODO break?
			case PROCEDURE_BLOCK_END:
				state = NO_BLOCK;
				break;
			case SQL_WITH_SEMICOLON:
				if (state == NO_BLOCK) {
					statementLineNumber = 1;
					statementNumber++;
					srl.setBlockType(SqlSplitterBlockType.SQL);
					srl.setStatementLineNumber(statementLineNumber++);
					srl.setStatementNumber(statementNumber);
					state = NO_BLOCK;
				}
				if (state == IN_SQL_BLOCK) {
					srl.setBlockType(SqlSplitterBlockType.SQL);
					srl.setStatementLineNumber(statementLineNumber++);
					srl.setStatementNumber(statementNumber);
					state = NO_BLOCK;
				}
				if (state == IN_PROCEDURE_BLOCK) {
					srl.setBlockType(SqlSplitterBlockType.PROCEDURE);
					srl.setStatementLineNumber(statementLineNumber++);
					srl.setStatementNumber(statementNumber);
				}
				break;

			case STATEMENT_NAME:
				switch (state) {
				case IN_SQL_BLOCK:

					String message = "While processing " + lineNumber + " found " + srl.toString()
							+ "\n Was a statement not properly terminated? \n" + lineState();
					logger.error(message);
					throw new SqlSplitterException(srl, message);

				}
			case INDETERMINATE:
				switch (state) {
				case IN_COMMENT_BLOCK:
					srl.setBlockType(SqlSplitterBlockType.COMMENT);
					break;

				case IN_PROCEDURE_BLOCK:
					srl.setBlockType(SqlSplitterBlockType.PROCEDURE);
					srl.setStatementLineNumber(statementLineNumber++);
					srl.setStatementNumber(statementNumber);
					break;
				case IN_SQL_BLOCK:
					srl.setBlockType(SqlSplitterBlockType.SQL);
					srl.setStatementLineNumber(statementLineNumber++);
					srl.setStatementNumber(statementNumber);
					break;
				case NO_BLOCK:
					if (srl.getText().trim().length() == 0) {
						srl.setBlockType(SqlSplitterBlockType.IGNORED);
						break;
					}
					if (proceduresOnly) {
						srl.setBlockType(SqlSplitterBlockType.PROCEDURE);
						state = IN_PROCEDURE_BLOCK;
					} else {
						state = IN_SQL_BLOCK;
						srl.setBlockType(SqlSplitterBlockType.SQL);
					}
					statementLineNumber = 1;
					statementNumber++;
					srl.setStatementNumber(statementNumber);
					srl.setStatementLineNumber(statementLineNumber++);
				
				}
				break;
			case STMT_END_NO_SQL:
				state = NO_BLOCK;
				srl.setBlockType(SqlSplitterBlockType.DIRECTIVE);
				break;
			case MARKDOWN_BLOCK_BEGIN:
				break;
			case MARKDOWN_BLOCK_END:
				break;
			case PROCEDURE_BLOCK_START:
				break;
			case SQL:
				break;
			default:
				break;
			}
			if (traceState > 0) {
				System.out.println("new state " + state + " " + srl.toString());
				// System.out.println(srl.toString("analyze 2"));
			}
			lineNumber++;
		}
		analyzed = true;
	}

	public static String stripAnnotations(String in) {
		String[] lines = StringUtil.getLines(in);
		StringBuilder sb = new StringBuilder();
		for (String line : lines) {
			if (annotationPattern.matcher(in).matches()) {
				continue;
			}
			sb.append(line);
			sb.append("\n");
		}
		return sb.toString();

	}
	
	private String lineState() {
		final StringBuilder sb = new StringBuilder();
		for (final SqlSplitterLine srl : lines) {
			sb.append(srl.toString());
			sb.append("\n");
		}
		return sb.toString();
	}

	public String snapshot() throws  SqlSplitterException {
		
		if (lines == null) {
			throw new IllegalStateException("analyze has not been called");
		}
		final StringBuilder sb = new StringBuilder();
		sb.append(lineState());
		int i = 1;
		System.out.println("SqlSplitter snapshot: sqltexts");
		for (String sqlText : getSqlTexts()) {
			System.out.println(String.format("#%d\n%s",i++,sqlText));
		}
		i = 1;
		System.out.println("Sqlsplitter sqlStatements");
		for (SqlStatement ss : getSqlStatements()) {
			System.out.println(String.format("#%d\n%s",i++,ss.toString()));
		}
		return sb.toString();
	}

	public ArrayList<SqlSplitterLine> getStatement(int statementNumber) throws SqlSplitterException {
		analyze();
		final ArrayList<SqlSplitterLine> stmtLines = new ArrayList<>();
		for (final SqlSplitterLine srl : lines) {
			if (srl.getStatementNumber() == statementNumber) {
				stmtLines.add(srl);
			}
		}
		return stmtLines;
	}

	@SuppressWarnings("incomplete-switch")
	String getSqlText(ArrayList<SqlSplitterLine> lines) {
		if (lines.size() == 0) {
			throw new IllegalArgumentException("no lines");
		}
		final SqlSplitterBlockType blockType = lines.get(0).getBlockType();
		//System.out.println("blockType " + blockType + " line " + lines.get(0));
		final StringBuilder sb = new StringBuilder();
		for (int i = 0; i < (lines.size() - 1); i++) {
			sb.append(lines.get(i).getText());
			sb.append("\n");
		}
		final String lastLine = lines.get(lines.size() - 1).getText();
		switch (blockType) {
		
		case SQL:
		//	System.out.println("is SQL");
			final int semiIndex = lastLine.indexOf(";");
			if (semiIndex > -1) {
				sb.append(lastLine, 0, semiIndex);
			} else {
				sb.append(lastLine);
			}
			break;
		case PROCEDURE:
		//	System.out.println("is procedure");
			sb.append(lastLine);
		}
		final String retval = sb.toString();
		return retval;

	}

	void dumpLines(ArrayList<SqlSplitterLine> lines) {
		for (final SqlSplitterLine line : lines) {
			System.out.println(line.toString());
		}
	}

	String getSqlText(int stmtNumber) throws  SqlSplitterException {
		final ArrayList<SqlSplitterLine> lines = getStatement(stmtNumber);
		// dumpLines(lines);
		final boolean isSql = lines.get(0).getBlockType().equals(SqlSplitterLineType.SQL);
		if (lines.size() == 0) {
			throw new IllegalArgumentException("no lines for statement " + stmtNumber);
		}
		String retval = getSqlText(lines);
		if (isSql) {
			System.out.println("trimming ';' from " + retval);
			retval = retval.replace(";", "");
		}
		logger.debug("returning {}\n{}", stmtNumber, retval);
		return retval;
	}

	int getStatementCount() {
		int i = lines.size() - 1;
		int statementCount = 0;
		while (i >= 0) {
			statementCount = lines.get(i).getStatementNumber();
			if (statementCount > 0) {
				break;
			}
			i--;
		}
		return statementCount;
	}

	public ArrayList<String> getSqlTexts() throws  SqlSplitterException {
		processLines();

		analyze();
		final ArrayList<String> sqlTexts = new ArrayList<>();
		final int statementCount = getStatementCount();
		logger.debug("statement count: {}", statementCount);
		for (int i = 0; i < getStatementCount(); i++) {
			final String sql = getSqlText(i + 1);
			sqlTexts.add(sql);
		}
		return sqlTexts;
	}

	public ArrayList<SqlStatement> getSqlStatementList() throws SqlSplitterException {
		final ArrayList<SqlStatement> statements = new ArrayList<>();

		ArrayList<String> sqlTexts = getSqlTexts();
		logger.debug("getSqlStatementList {} {}","getSqlTexts returned ",sqlTexts.size() );
		for (final String sqlText : getSqlTexts()) {
			final String sqlTextLines[] = sqlText.split("\n");
			String name = null;
			for (final String textLine : sqlTextLines) {
				if (textLine.toUpperCase().contains("@NAME ")) {
					if (name != null) {
						throw new IllegalStateException("@NAME already specified for " + sqlText);
					}
					final int index = textLine.toUpperCase().indexOf("@NAME ");
					name = textLine.substring(index + "@NAME ".length()).trim();
				}
			}
			final SqlStatement ss = new SqlStatement(sqlText);
			ss.setName(name);
			statements.add(ss);
		}
		logger.debug("getSqlStatementList: size() {}", statements.size());
		return statements;
	}

	public SqlStatements getSqlStatements() throws SqlSplitterException {
		final SqlStatements sqlStatements = new SqlStatements(getSqlStatementList());
		return sqlStatements;
	}

	public String getInputName() {
		return inputType + ":" + inputName;
	}

	public SqlSplitter setProceduresOnly(boolean proceduresOnly) {
		logger.debug("proceduresOnly {} " , proceduresOnly);
		this.proceduresOnly = proceduresOnly;
		return this;
	}

	public SqlSplitter setTraceState(int traceState) {
		if ((traceState < 0) || (traceState > 9)) {
			throw new IllegalArgumentException("must be 0-9");
		}
		this.traceState = traceState;
		return this;

	}

	public boolean isTrace() {
		return trace;
	}

	public void setTrace(boolean trace) {
		this.trace = trace;
	}

}
