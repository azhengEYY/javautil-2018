package org.javautil.oracle.trace.formatter;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.javautil.oracle.trace.CursorInfo;
import org.javautil.oracle.trace.CursorOperationAggregation;
import org.javautil.oracle.trace.CursorsStats;
import org.javautil.oracle.trace.record.Stat;
import org.javautil.sql.Binds;
import org.javautil.sql.NamedSqlStatements;
import org.javautil.sql.SequenceHelper;
import org.javautil.sql.SqlSplitterException;
import org.javautil.sql.SqlStatement;

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

    public SqlMarshaller(Connection connection) throws SQLException, SqlSplitterException, IOException {

        // statements =
        // NamedSqlStatements.getNameSqlStatementsFromSqlSplitterResource(this,
        // "cursor_stat_dml.sr.sql");
        statements = NamedSqlStatements.fromSqlSplitter(connection,
                new File("src/main/resources/cursor_stat_dml.sr.sql"));
        this.connection = connection;
        sequenceHelper = new SequenceHelper(connection);
        // insertCursor = statements.getSqlStatement("cursor_insert");
        cursorTextInsert = statements.getSqlStatement("cursor_text_insert");
        cursorInfoRunInsert = statements.getSqlStatement("cursor_info_run_insert");
        cursorInfoInsert = statements.getSqlStatement("cursor_info_insert");
        cursorStatInsert = statements.getSqlStatement("cursor_stat_insert");
        // insertCursor.setConnection(connection);
        cursorTextInsert.setConnection(connection);
        cursorInfoRunInsert.setConnection(connection);
        cursorInfoInsert.setConnection(connection);

    }

    public long persist(CursorsStats cursors) throws SQLException {
        long runId = sequenceHelper.getSequence("cursor_info_run_id_seq");
        Binds binds = new Binds();
        binds.put("cursor_info_run_id", runId);
        binds.put("cursor_info_run_descr", null);
        cursorInfoRunInsert.executeUpdate(binds);
        for (CursorInfo cursor : cursors.getCursorInfos(false)) {
            save(cursor);
        }
        return runId;
    }

    public void save(CursorInfo cursor) throws SQLException {
        Long textId = sequenceHelper.getSequence("cursor_text_id_seq");
        Long cursorId = sequenceHelper.getSequence("cursor_info_id_seq");
        Binds sqlBinds = getSqlBinds(cursor, textId);
        cursorTextInsert.executeUpdate(sqlBinds);
        //
        Binds cursorBinds = toBinds(cursor, cursorId, textId);
        cursorInfoInsert.executeUpdate(cursorBinds);
        // insertCursor.executeUpdate(cursorBinds);
        int sequenceNbr = 1;
        if (cursor.getStats() != null) {
            for (Stat stat : cursor.getStats()) {
                insertStat(stat, cursorId, sequenceNbr++);
            }
        }
    }

    public Binds getSqlBinds(CursorInfo cursor, Long cursorTextId) {
        Binds binds = new Binds();
        binds.put("cursor_text_id", cursorTextId);
        binds.put("sql_text", cursor.getParsing().getSqlText());
        binds.put("explain_plan", formatStats(cursor).toString());
        binds.put("sql_text_hash", null); /* TODO */

        return binds;

    }

    public static void setStatDepths(List<Stat> stats) {
        int statNbr = 0;
        int seqNbr = 1;
        for (Stat stat : stats) {
            stat.setSequenceNbr(seqNbr++);
            if (statNbr == 0) {
                stat.setDepth(0);
            }
            Stat parent = stats.get(stat.getParentId());
            stat.setDepth(parent.getDepth() + 1);
        }
    }

    // TODO this is copy paste from OracleTraceReport
    StringBuilder formatStats(CursorInfo cursor) {
        StringBuilder sb = new StringBuilder();

        String heading = String.format("%s\n", "Rows     Row Source Operation");
        String dashes = String.format("%s\n", "-------  ---------------------------------------------------");
        sb.append(heading);
        sb.append(dashes);
        if (cursor.getStats() != null) {

            int statNbr = 0;
            ArrayList<Stat> stats = cursor.getStats();
            for (Stat stat : stats) {
                if (statNbr == 0) {
                    stat.setDepth(0);
                }
                Stat parent = stats.get(stat.getParentId());
                stat.setDepth(parent.getDepth() + 1);
            }

            for (Stat stat : cursor.getStats()) {
                int indent = stat.getDepth() + 1;
                String formatString = "%7d" + "%" + indent + "s" + "%s"
                        + " (cr=%d pr=%d pw=%d cost=%d size=%d card=%d)\n";

                String line = String.format(formatString, stat.getCnt(), "", stat.getOperation(),
                        stat.getConsistentReads(),
                        stat.getPhysicalReads(), stat.getPhysicalWrites(), 0, 0, 0 // TODO WTF
                );
                sb.append(line);
            }
        }
        return sb;
    }

    public Binds toBinds(CursorInfo cursor, Long cursorInfoId, Long cursorTextId) {

        CursorOperationAggregation parse = cursor.getParseAggregation();
        CursorOperationAggregation fetch = cursor.getFetchAggregation();
        CursorOperationAggregation exec = cursor.getExecAggregation();
        CursorOperationAggregation unmap = cursor.getUnmapAggregation();
        // CursorOperationAggregation close = cursor.getCloseAggregation();

        Binds binds = new Binds();

        binds.put("cursor_info_id", cursorInfoId);
        // binds.put("cursor_stat_id", cursorStatId);
        binds.put("cursor_text_id", cursorTextId);
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
        return binds;
    }

    void insertStat(Stat stat, Long cursorInfoId, int seqNbr) throws SQLException {

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
    }

}
