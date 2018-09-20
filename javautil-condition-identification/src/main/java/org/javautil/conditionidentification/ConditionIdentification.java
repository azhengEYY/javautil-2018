package org.javautil.conditionidentification;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.javautil.dblogging.logger.Dblogger;
import org.javautil.misc.Timer;
import org.javautil.sql.Binds;
import org.javautil.sql.SqlSplitterException;
import org.javautil.sql.SqlStatement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

public class ConditionIdentification {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	/**
	 * Runs a series of sql statements that return as the first column, the primary
	 * key of a table being examined an 0 or more fields used in formatting a
	 * message.
	 * <p>
	 * https://dzone.com/articles/java-string-format-examples
	 */

	private List<Map<String, Object>> rules;

	private List<ConditionRule> conditionRules;

	private Connection connection;

	private ConditionIdentificationPersistence persister;

	private ArrayList<String> messages;
	
	private Dblogger dblogger; // = new DatabaseLoggerNoOperation();


	private int verbosity;

	public ConditionIdentification(Connection conn, List<Map<String, Object>> rules, boolean verbose,
			int verbosity) throws IOException, SQLException, SqlSplitterException {
		this.rules = rules;
		initialize(conn, verbose, verbosity);
	}

	public ConditionIdentification(Connection conn, String yamlRuleFileName, boolean verbose,
			int verbosity) throws IOException, SQLException, SqlSplitterException {
		final FileInputStream fis = new FileInputStream(yamlRuleFileName);
		final BufferedInputStream bis = new BufferedInputStream(fis);
		loadRules(bis);
		bis.close();
		initialize(conn, verbose, verbosity);
	}

	public ConditionIdentification(Connection conn, InputStream yamlRules, boolean verbose,
			int verbosity) throws IOException, SQLException, SqlSplitterException {
		loadRules(yamlRules);
		initialize(conn, verbose, verbosity);
	}

	private void initialize(Connection conn, boolean verbose, int verbosity) throws IOException, SqlSplitterException {
		this.connection = conn;

		this.verbosity = verbosity;

		persister = new ConditionIdentificationPersistence(conn, verbosity);

		conditionRules = new ArrayList<>();
		for (final Map<String, Object> ruleMap : rules) {
			final ConditionRule rule = new ConditionRule(ruleMap);
			conditionRules.add(rule);
		}
	}

	@SuppressWarnings("unchecked")
	private void loadRules(InputStream is) {
		final Yaml yaml = new Yaml();
		rules = (List<Map<String, Object>>) yaml.load(is);
	}

	/**
	 * A row is inserted into ut_condition_run The bind
	 * variables are inserted into ut_condition_run_parm for each rule The rule is
	 * retrieved from ut_condition and created if necessary. For each row returned a
	 * row is inserted into ut_condition_row_msg A row is inserted into
	 * ut_condition_run_step
	 *
	 * @param binds
	 * @param verbosity level of logging outpub
	 * @throws SQLException
	 */
	public List<String> process(Binds binds, int verbosity) throws SQLException {
		final Timer t = new Timer(getClass(), getClass().getName(), binds.toString());
		messages = new ArrayList<>();
		final long utConditionRunId = persister.startRun(binds); // # Record the start of the run

		for (final ConditionRule rule : conditionRules) {
			final long start_ts = System.nanoTime();
			final long run_step_id = persister.utConditionRunStepInsert(utConditionRunId, start_ts, rule);
			processRule(run_step_id, rule, binds);
		}
		t.logElapsed();
		// TODO this should not be in javautil this application specific
//		logger.info("condition_count for " + binds.getLong("etl_file_id") + count);
		return messages;

	}
	
	

	int processRule(long runStepId, ConditionRule rule, Binds binds) throws SQLException {
		new Timer(getClass(), "ConditionIdentification:processRule", rule.getRuleName() +
				" " + binds.toString());

	//	dblogger.showConnectionInformation();
		if (verbosity > 6) {
			logger.info("processing rule " + rule.getRuleName());
		}
		final String conditionSql = rule.getSqlText();
		dblogger.insertStep("ConditionIdentification",rule.getRuleName(),getClass().getName());
		final SqlStatement sh = new SqlStatement(conditionSql, connection);
		final ResultSet rset = sh.executeQuery(binds);
		final int columnCount = rset.getMetaData().getColumnCount();
		int rowCount = 0;
		while (rset.next()) {
			rowCount++;

			final Integer pk = rset.getInt(1);
			final ArrayList<Object> argList = new ArrayList<>();
			for (int i = 1; i <= columnCount; i++) {
				argList.add(rset.getObject(i));
			}

			final Object[] args = argList.toArray();
			String formattedString;

			try {
				formattedString = String.format(rule.getJavaFormat(), args);
			} catch (final java.util.MissingFormatArgumentException e) {
				rset.close();
				throw new java.util.MissingFormatArgumentException(
						"rule '" + rule + "' " + Arrays.toString(args) + " " + e.getMessage());
			}
			persister.utConditionRowMsgInsert(runStepId, pk, formattedString);
			if (verbosity > 5) {
				logger.info(formattedString);
			}
			messages.add(formattedString);

		}
		rset.close();
		sh.close();
		dblogger.finishStep();
		logger.info("processed rule " + rule.getRuleName() + " rows: " + rowCount);
		return rowCount;
	}

	public Dblogger getDblogger() {
		return dblogger;
	}

	public void setDblogger(Dblogger dblogger) {
		this.dblogger = dblogger;
	}

}