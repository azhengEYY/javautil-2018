/**
 * @(#) Cursor.java
 */
package org.javautil.oracle.trace.formatter;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Formatter;

import org.javautil.oracle.trace.CursorInfo;
import org.javautil.oracle.trace.CursorOperationAggregation;
import org.javautil.oracle.trace.CursorsStats;
import org.javautil.oracle.trace.OracleTraceProcessor;
import org.javautil.oracle.trace.record.Parsing;
import org.javautil.oracle.trace.record.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * There is nothing to aggregate on a Parsing.
 * 
 * @todo fix adding 1 to fetch count even if there is no fetch, maybe just on
 *       aggregation.
 */
public class OracleTraceReport {

    private final Logger        logger               = LoggerFactory.getLogger(getClass());

    private static final String newline              = System.getProperty("line.separator");

    private final Formatter     formatter;

    private OutputStream        os;

    private static final String secondsHeadingFormat = "%16s";
    private static final String secondsDashes        = "----------------";
    private static final String secondsFormat        = "%16.6f";
    private static final String tenDashes            = "----------";
    private static final String dashesFormat         = "%7s %10s " + secondsHeadingFormat + " " + secondsHeadingFormat
            + " %10s %10s %10s %10s";

    private String              columnHeaderFormat   = null;

    private final String[]      header               = new String[] {
            "********************************************************************************",                       //
            "count    = number of times OCI procedure was executed",                                                  //
            "cpu      = cpu time in seconds executing",                                                               //
            "elapsed  = elapsed time in seconds executing",                                                           //
            "disk     = number of physical reads of buffers from disk",                                               //
            "query    = number of buffers gotten for consistent read",                                                //
            "current  = number of buffers gotten in current mode (usually for update)",                               //
            "rows     = number of rows processed by the fetch or execute call",                                       //
            "********************************************************************************"                        //
    };

    private static final float  million              = 1000000;

    private String              numericFormat;

    private String              tracefileName;

    public OracleTraceReport(String tracefileName, String outputFileName) throws IOException {
        this.tracefileName = tracefileName;
        this.os = new FileOutputStream(outputFileName);
        formatter = new Formatter(os);
    }

    public OracleTraceReport(OutputStream os) {
        if (os == null) {
            throw new IllegalArgumentException("os is null");
        }
        this.os = os;
        formatter = new Formatter(os);
    }

    public static void process(String infileName, String outfileName, boolean includeSys) throws IOException {
        OracleTraceReport formatter = new OracleTraceReport(infileName, outfileName);
        formatter.process(includeSys);
    }

    public void process(boolean includeSys) throws IOException {
        OracleTraceProcessor tfr = new OracleTraceProcessor(tracefileName);
        tfr.process();

        format(tfr.getCursors(), includeSys);
        os.close();
    }

    public void format(CursorsStats cursors, boolean includeSys) throws IOException {
        emitHeader();
        emitColumnHeaders();
        for (CursorInfo aggStats : cursors.getCursorInfos(includeSys)) {
            format(aggStats);
        }
        //
        writeLn("\nOVERALL TOTALS FOR ALL NON-RECURSIVE STATEMENTS\n");
        CursorInfo nonSys = new CursorInfo();
        Collection<CursorInfo> nonSysCursors = cursors.getNonRecursiveCursorInfo();
        nonSys.aggregate(nonSysCursors);
        format(nonSys);
        //
        writeLn("\nOVERALL TOTALS FOR ALL RECURSIVE STATEMENTS\n");
        CursorInfo sys = new CursorInfo();
        Collection<CursorInfo> sysCursors = cursors.getRecursiveCursorInfo();
        sys.aggregate(sysCursors);
        format(nonSys);
    }

    protected void emitHeader() {
        for (String line : header) {
            formatter.format("%s\n", line);
            formatter.flush();
        }
        writeLn("\n");
    }

    protected void emitColumnHeaders() throws IOException {
        if (columnHeaderFormat == null) {
            columnHeaderFormat = "%7s %10s " + secondsHeadingFormat + " " + secondsHeadingFormat
                    + " %10s %10s %10s %10s" + newline;
            if (logger.isDebugEnabled()) {
                logger.debug("columnHeaderFormat: '" + columnHeaderFormat + "'");
            }
        }

        formatter.format(columnHeaderFormat, "call", "count", "cpu", "elapsed", "disk", "query", "current", "rows");
        // TODO convert to String.format final static
        emitDashes();

        formatter.flush();
    }

    public void format(CursorInfo cursor) throws IOException {
        writeLn("");
        writeLn("********************************************************************************");
        Parsing parsing = cursor.getParsing();
        if (parsing != null) {

            writeLn("SQL ID : " + parsing.getSqlid() + " uid: " + parsing.getUid());
            writeLn(parsing.getSqlText());
        }
        emitColumnHeaders();
        emitAggregate("Parse", cursor.getParseAggregation());
        emitAggregate("Exec", cursor.getExecAggregation());
        emitAggregate("Fetch", cursor.getFetchAggregation());
        emitDashes();
        writeLn(null);
        emitStats(cursor);
    }

    void emitStats(CursorInfo cursor) throws IOException {
        formatter.format("%s" + newline, "Rows     Row Source Operation");
        formatter.format("%s" + newline, "-------  ---------------------------------------------------");
        boolean hasStats = cursor.getStats() != null;

        if (!hasStats) {
            writeLn("no stats!!!");
        } else {
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
                if (logger.isDebugEnabled()) {
                    logger.debug("formatString: " + formatString);
                }
                formatter.format(formatString, stat.getCnt(), "", stat.getOperation(), stat.getConsistentReads(),
                        stat.getPhysicalReads(), stat.getPhysicalWrites(), 0, 0, 0 // TODO WTF
                );
            }
        }
    }

    void writeLn(String string) {
        if (string != null) {
            formatter.format("%s%s", string, newline);
        } else {
            formatter.format("%s", newline);
        }
    }

    protected void emitDashes() {

        formatter.format(dashesFormat, "-------", tenDashes, secondsDashes, secondsDashes, tenDashes, tenDashes,
                tenDashes, tenDashes);
        formatter.flush();
        writeLn("");
    }

    void emitAggregate(String operation, CursorOperationAggregation aggregation) {
        if (numericFormat == null) {
            numericFormat = "%-7s %10d " + secondsFormat + " " + secondsFormat + " %10d %10d %10d %10d" + newline;
        }
        if (aggregation != null) {
            formatter.format(numericFormat, operation, // 1
                    aggregation.getEventCount(), // 2
                    aggregation.getCpu() / million, // 3
                    aggregation.getElapsedMicroSeconds() / million, // 4
                    aggregation.getPhysicalBlocksRead(), // 5
                    aggregation.getConsistentReadBlocks(), // 6
                    aggregation.getCurrentModeBlocks(), // 7
                    aggregation.getRowCount()); // 8
        }
    }

    // TOOO add commandline parser
    public static void main(String[] args) throws IOException {
        String infile = args[0];
        String outfile = args[1];
        OracleTraceReport otr = new OracleTraceReport(infile, outfile);
        otr.process(false);

    }

}
