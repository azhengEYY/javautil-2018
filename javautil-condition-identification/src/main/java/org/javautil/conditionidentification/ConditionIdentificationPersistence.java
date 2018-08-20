package org.javautil.conditionidentification;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map.Entry;

import org.javautil.sql.Binds;
import org.javautil.sql.NamedSqlStatements;
import org.javautil.sql.SequenceHelper;
import org.javautil.sql.SqlRunner;
import org.javautil.sql.SqlSplitterException;
import org.javautil.sql.SqlStatement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConditionIdentificationPersistence {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());


	private final Connection connection;


	private SequenceHelper sequenceHelper;

	final String resourceName = "condition_identification/UtConditionPersistenceDml.sr.sql";
	
	private NamedSqlStatements sqlStatements;


	public ConditionIdentificationPersistence(Connection conn, int verbosity) throws IOException, SqlSplitterException {
		this.connection = conn;

		try {
			sequenceHelper = new SequenceHelper(conn);
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
		sqlStatements = NamedSqlStatements.getNameSqlStatementsFromSqlSplitterResource(this,resourceName);
		System.out.println("ConditionIdentificationPersistence: " + sqlStatements.getStatementNames());
	}
    
	/**
	 * @param binds
	 * @return ut_condition.ut_condition_id for newly created row
	 * @throws SQLException
	 */
	long startRun(Binds binds) throws SQLException {
		final SqlStatement stmt = sqlStatements.getSqlStatement("ut_condition_run_insert");

		final java.sql.Date start_ts = new java.sql.Date(System.currentTimeMillis());
		final Long utConditionRunId = sequenceHelper.getSequence("ut_condition_run_id_seq");
		binds.put("start_ts", start_ts);
		binds.put("ut_condition_run_id", utConditionRunId);
		stmt.executeUpdate(connection, binds);
		bindsInsert(utConditionRunId, binds);
		logger.info("inserted ut_condition_run: " + utConditionRunId);
		return utConditionRunId;
	}

	void bindsInsert(long utConditionRunId, Binds binds) throws SQLException {
		final SqlStatement sh = sqlStatements.getSqlStatement("ut_condition_run_parm_insert");

		for (final Entry<String, Object> e : binds.entrySet()) {
			final Binds b = new Binds();
			b.put("UT_CONDITION_RUN_ID", utConditionRunId);
			b.put("PARM_NM", e.getKey());
			b.put("PARM_VALUE", e.getValue().toString());
			b.put("PARM_TYPE", e.getValue().getClass().getName());
			sh.setConnection(connection);
			sh.executeUpdate(b);
		}

	}

	long utConditionRunStepInsert(long utConditionRunId, long beginNanos, ConditionRule rule) throws SQLException {
		final SqlStatement sh = sqlStatements.getSqlStatement("ut_condition_run_step_insert");
		final Binds binds = new Binds();
		final long utConditionId = utConditionInsert(rule);
		binds.put("UT_CONDITION_RUN_ID", utConditionRunId);
		binds.put("UT_CONDITION_ID", utConditionId);

		final long runStepId = sequenceHelper.getSequence("ut_condition_run_step_id_seq");
		binds.put("ut_condition_run_step_id", runStepId);
		binds.put("START_TS", new java.sql.Date(System.currentTimeMillis()));
		sh.executeUpdate(connection, binds);
		return runStepId;

	}

	long utConditionInsert(ConditionRule rule) throws SQLException {
		// could check if this already exists and then just reuse TODO
		final SqlStatement sel = sqlStatements.getSqlStatement("ut_condition_select");
		sel.setConnection(connection);
		sel.executeQuery(rule.getBinds());

		final SqlStatement insert = sqlStatements.getSqlStatement("ut_condition_insert");
		final Binds binds = rule.getBinds();
		final long utConditionId = sequenceHelper.getSequence("ut_condition_id_seq");
		binds.put("ut_condition_id", utConditionId);
		logger.debug("Binds for condition: " + binds);
		insert.executeUpdate(connection, binds);
		return utConditionId;
	}

	void utConditionRowMsgInsert(long runStepId, long pk, String formattedString) throws SQLException {
		final SqlStatement sh = sqlStatements.getSqlStatement("ut_condition_row_msg_insert");

		final Binds binds = new Binds();
		binds.put("UT_CONDITION_RUN_STEP_ID", runStepId);
		binds.put("PRIMARY_KEY", pk);
		binds.put("MSG", formattedString);
		sh.executeUpdate(connection, binds);
	}
	
	public void purgeRun(long runNbr) throws SQLException {

		SqlRunner runner = new SqlRunner(connection,sqlStatements,"deleteRow","deleteStep","deleteParm","deleteRun");
		Binds binds = new Binds();
		binds.put("ut_condition_run_id", runNbr);
		runner.process(binds);
		
	}
}