package org.javautil.oracle.trace.formatter;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import org.javautil.oracle.trace.CursorInfo;
import org.javautil.oracle.trace.CursorOperationAggregation;
import org.javautil.oracle.trace.CursorsStats;
import org.javautil.oracle.trace.record.Stat;
import org.javautil.sql.Binds;
import org.javautil.sql.NamedSqlStatements;
import org.javautil.sql.SequenceHelper;
import org.javautil.sql.SqlSplitterException;
import org.javautil.sql.SqlStatement;
import org.javautil.util.ListOfNameValue;
import org.javautil.util.ShaHasher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SqlMarshaller {

    private NamedSqlStatements statements;
    private SqlStatement       insertSql;
    private SequenceHelper     sequenceHelper;
    // private SqlStatement insertCursor;
    private SqlStatement       cursorTextInsert;
    private SqlStatement       cursorInfoRunInsert;
    private SqlStatement       cursorInfoInsert;
    private SqlStatement       cursorStatInsert;
    private Connection         connection;
    private Logger             logger = LoggerFactory.getLogger(getClass());

    public SqlMarshaller(Connection connection) throws SQLException, SqlSplitterException, IOException {

        // statements =
        // NamedSqlStatements.getNameSqlStatementsFromSqlSplitterResource(this,
        // "cursor_stat_dml.sr.sql");
        statements = NamedSqlStatements.fromSqlSplitter(connection,
                new File("src/main/resources/cursor_stat_dml.sr.sql"));
        this.connection = connection;
        sequenceHelper = new SequenceHelper(connection);

        cursorStatInsert = statements.getSqlStatement("cursor_stat_insert");
        cursorStatInsert.setConnection(connection);

        cursorTextInsert = statements.getSqlStatement("cursor_text_insert");
        cursorTextInsert.setConnection(connection);

        cursorInfoRunInsert = statements.getSqlStatement("cursor_info_run_insert");
        cursorInfoRunInsert.setConnection(connection);

        cursorInfoInsert = statements.getSqlStatement("cursor_info_insert");
        cursorInfoInsert.setConnection(connection);
    }

    public long saveAll(CursorsStats cursors) throws SQLException {
        Binds cursorRunBinds = cursorRunInsert();

        for (CursorInfo cursor : cursors.getCursorInfos(false)) {
            saveCursor(cursor);
        }
        return cursorRunBinds.getLong("cursor_info_run_id");
    }

    public Binds cursorRunInsert() throws SQLException {
        long runId = sequenceHelper.getSequence("cursor_info_run_id_seq");
        Binds binds = new Binds();
        binds.put("cursor_info_run_id", runId);
        binds.put("cursor_info_run_descr", null);
        cursorInfoRunInsert.executeUpdate(binds);
        return binds;
    }

    public void saveCursor(CursorInfo cursor) throws SQLException {

        String planHash = cursorPlanInsert(cursor);
        String sqlTextHash = cursorTextInsert(cursor);

        Binds cursorBinds = cursorInfoInsert(cursor, planHash, sqlTextHash);
        long cursorId = cursorBinds.getLong("cursor_info_id");

        int sequenceNbr = 1;
        if (cursor.getStats() != null) {
            for (Stat stat : cursor.getStats()) {
                cursorStatInsert(stat, cursorId, sequenceNbr++);
            }
        }
    }

    public String cursorTextInsert(CursorInfo cursor) throws SQLException {

        String sqlText = cursor.getParsing().getSqlText();
        String sqlHash = ShaHasher.hashAsBase64(sqlText);
        SqlStatement byHashSs = statements.getSqlStatement("cursor_text_by_hash");
        byHashSs.setConnection(connection);
        Binds binds = new Binds();
        binds.put("sql_text_hash", sqlHash);
        ListOfNameValue nv = byHashSs.getListOfNameValue(binds, true);
        if (nv.size() == 0) {
            binds.put("sql_text_hash", sqlHash);
            binds.put("sql_text", sqlText);
            logger.debug("sql_text_hash =====: {}", sqlHash);
            SqlStatement cursorTextInsert = statements.get("cursor_text_insert");
            cursorTextInsert.setConnection(connection);
            cursorTextInsert.executeUpdate(binds);
        }
        logger.debug("cursor_text:\n{}", binds);
        return sqlHash;
    }

    public String cursorPlanInsert(CursorInfo cursor) throws SQLException {

        String planText = cursor.formatExplainPlan();
        String planHash = ShaHasher.hashAsBase64(planText);
        SqlStatement byHashSs = statements.getSqlStatement("cursor_plan_by_hash");
        byHashSs.setConnection(connection);
        Binds binds = new Binds();
        binds.put("explain_plan_hash", planHash);
        ListOfNameValue nv = byHashSs.getListOfNameValue(binds, true);
        if (nv.size() == 0) {
            binds.put("explain_plan_hash", planHash);
            binds.put("explain_plan", planText);
            logger.debug("explain_plan:\n{}", planText);
            SqlStatement cursorPlanInsert = statements.get("cursor_plan_insert");
            cursorPlanInsert.setConnection(connection);
            cursorPlanInsert.executeUpdate(binds);
        }
        logger.debug("cursor_plan:\n{}", binds);
        return planHash;
    }

    public Binds cursorInfoInsert(CursorInfo cursor, String explainPlanHash, String sqlTextHash) throws SQLException {

        CursorOperationAggregation parse = cursor.getParseAggregation();
        CursorOperationAggregation fetch = cursor.getFetchAggregation();
        CursorOperationAggregation exec = cursor.getExecAggregation();
        CursorOperationAggregation unmap = cursor.getUnmapAggregation();
        // CursorOperationAggregation close = cursor.getCloseAggregation();

        Binds binds = new Binds();
        Long cursorInfoId = sequenceHelper.getSequence("cursor_info_id_seq");
        binds.put("cursor_info_id", cursorInfoId);
        binds.put("explain_plan_hash", explainPlanHash);
        // binds.put("cursor_stat_id", cursorStatId);
        binds.put("sql_text_hash", sqlTextHash);
        binds.put("parse_cpu_micros", parse.getCpu());
        binds.put("parse_elapsed_micros", parse.getElapsedMicroSeconds());
        binds.put("parse_blocks_read", parse.getPhysicalBlocksRead());
        binds.put("parse_consistent_blocks", parse.getConsistentReadBlocks());
        binds.put("parse_current_blocks", parse.getCurrentModeBlocks());
        // binds.put("parse_lib_miss",parse.getLibMiss() );
        binds.put("parse_row_count", parse.getRowCount());
        binds.put("exec_cpu_micros", exec.getCpu());
        binds.put("exec_elapsed_micros", exec.getElapsedMicroSeconds());
        binds.put("exec_blocks_read", exec.getPhysicalBlocksRead());
        binds.put("exec_consistent_blocks", exec.getConsistentReadBlocks());
        binds.put("exec_current_blocks", exec.getCurrentModeBlocks());
        // binds.put("exec_lib_miss",exec.getLibMiss() );
        binds.put("exec_row_count", exec.getRowCount());
        boolean hasFetch = fetch != null;
        binds.put("fetch_cpu_micros", hasFetch ? fetch.getCpu() : null);
        binds.put("fetch_elapsed_micros", hasFetch ? fetch.getElapsedMicroSeconds() : null);
        binds.put("fetch_blocks_read", hasFetch ? fetch.getPhysicalBlocksRead() : null);
        binds.put("fetch_consistent_blocks", hasFetch ? fetch.getConsistentReadBlocks() : null);
        binds.put("fetch_current_blocks", hasFetch ? fetch.getCurrentModeBlocks() : null);
        // binds.put("fetch_lib_miss",fetch.getLibMiss() );
        binds.put("fetch_row_count", hasFetch ? fetch.getRowCount() : null);
        logger.debug("cursor_info:\n{}", binds);
        cursorInfoInsert.executeUpdate(binds);

        return binds;
    }

    void cursorStatInsert(Stat stat, Long cursorInfoId, int seqNbr) throws SQLException {

        Binds binds = new Binds();
        binds.put("cursor_info_id", cursorInfoId);
        binds.put("seq_nbr", seqNbr);
        binds.put("operation_depth", stat.getDepth());
        binds.put("operation", stat.getOperation());
        binds.put("consistent_reads", stat.getConsistentReads());
        binds.put("physical_reads", stat.getPhysicalWrites());
        binds.put("physical_writes", stat.getPhysicalWrites());
        binds.put("elapsed_millis", stat.getTime());
        cursorStatInsert.setConnection(connection);
        cursorStatInsert.executeUpdate(binds);
        logger.debug("cursor_stat:\n{}", binds);
    }

}
