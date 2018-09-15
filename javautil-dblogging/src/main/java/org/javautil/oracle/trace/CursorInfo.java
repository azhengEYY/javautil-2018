package org.javautil.oracle.trace;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.javautil.io.Tracer;
import org.javautil.oracle.trace.record.CursorOperation;
import org.javautil.oracle.trace.record.Parse;
import org.javautil.oracle.trace.record.Parsing;
import org.javautil.oracle.trace.record.Stat;
import org.javautil.oracle.trace.record.Close;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class CursorInfo {
    private static transient Logger    logger = LoggerFactory.getLogger(CursorInfo.class);

    /**
     * This number is unique for every parse of a SQL Statement, as opposed to sqlId
     * which remains the same for a given SQL text.
     */
    private transient Long             cursorNumber;

    private Parsing                    parsing;

    private Parse                      parse;

    private CursorOperationAggregation parseAggregation;
    private CursorOperationAggregation fetchAggregation;
    private CursorOperationAggregation execAggregation;
    private CursorOperationAggregation unmapAggregation;
    private CursorOperationAggregation closeAggregation;

    private ArrayList<Stat>            stats;

    private transient Tracer           tracer = new Tracer();
    
    private Close close;

    public CursorInfo(CursorOperation record) {
        if (parsing == null) {
            String message = String.format(
                    "Cursor information: %s found for cursor #%d at line #%d but parsing record not in this file",
                    record.getRecordType(), record.getCursorNumber(), record.getLineNumber());
            switch (record.getRecordType()) {
            case CLOSE:
                logger.debug(message);
                break;
            default:
                logger.warn(message);
            }
        }
        this.cursorNumber = record.getCursorNumber();
        aggregate(record);
    }

    public CursorInfo(Parsing record) {
        this.cursorNumber = record.getCursorNumber();
        this.parsing = record;
    }

    public CursorInfo(Parse record) {
        this.cursorNumber = record.getCursorNumber();
        this.parse = record;
    }

    public CursorInfo(Stat record) {
        this.cursorNumber = record.getCursorNumber();
        stats.add(record);
    }
    
    public CursorInfo(Close record) {
        this.cursorNumber = record.getCursorNumber();
        this.close = record;
    }

    public CursorInfo() {
    }

    public void aggregate(Collection<CursorInfo> cursors) {
        parseAggregation = new CursorOperationAggregation(tracer);
        fetchAggregation = new CursorOperationAggregation(tracer);
        execAggregation = new CursorOperationAggregation(tracer);
        unmapAggregation = new CursorOperationAggregation(tracer);
        closeAggregation = new CursorOperationAggregation(tracer);

        for (CursorInfo ci : cursors) {
            parseAggregation.aggregate(ci.getParseAggregation());
            fetchAggregation.aggregate(ci.getFetchAggregation());
            execAggregation.aggregate(ci.getExecAggregation());
            unmapAggregation.aggregate(ci.getUnmapAggregation());
            closeAggregation.aggregate(ci.getCloseAggregation());
        }
    }

    private CursorOperationAggregation getCloseAggregation() {
        return closeAggregation;
    }

    public CursorOperationAggregation aggregate(CursorOperation operation) {
        CursorOperationAggregation aggregator = null;
        switch (operation.getRecordType()) {
        case FETCH:
            if (fetchAggregation == null) {
                fetchAggregation = new CursorOperationAggregation();
            }
            aggregator = fetchAggregation;
            break;
        case EXEC:
            if (execAggregation == null) {
                execAggregation = new CursorOperationAggregation();
            }
            aggregator = execAggregation;
            break;
        case PARSE:
            if (parseAggregation == null) {
                parseAggregation = new CursorOperationAggregation();
            }
            aggregator = parseAggregation;
            break;
        case UNMAP:
            if (unmapAggregation == null) {
                unmapAggregation = new CursorOperationAggregation();
            }
            aggregator = unmapAggregation;
            break;
        case CLOSE:
            if (closeAggregation == null) {
                closeAggregation = new CursorOperationAggregation();
            }
            aggregator = closeAggregation;
            break;
        default:
            throw new IllegalArgumentException(operation.toString());
        }
        if (aggregator == null) {
            throw new IllegalStateException("aggregator is null");
        }
        aggregator.setTracer(tracer);
        aggregator.aggregate(operation);
        return aggregator;
    }

    public CursorOperationAggregation getExecAggregation() {
        return execAggregation;
    }

    public void setExecAggregation(CursorOperationAggregation execAggregation) {
        this.execAggregation = execAggregation;
    }

    // public Cursor getCursor() {
    // return cursor;
    // }

    public CursorOperationAggregation getFetchAggregation() {
        return fetchAggregation;
    }

    public CursorOperationAggregation getParseAggregation() {
        return parseAggregation;
    }

    public CursorOperationAggregation getUnmapAggregation() {
        return unmapAggregation;
    }

    public Long getCursorNumber() {
        return cursorNumber;
    }

    public Parsing getParsing() {
        return parsing;
    }

    public void addStat(Stat record) {
        if (stats == null) {
            stats = new ArrayList<Stat>();
        }
        if (tracer.isTracing()) {
            String parsingMessage = parsing == null ? "No parsing"
                    : parsing.getSqlid() + " " + parsing.getLineAndText();
            String message = String.format("Adding stat: %s\nto:%s", record.getLineAndText(), parsingMessage);
            tracer.traceln(message);

            for (Stat stat : stats) {
                tracer.traceln(stat.getLineAndText());
            }
        }
        stats.add(record);
    }

    public ArrayList<Stat> getStats() {
        return stats;
    }

    public void setTracer(Tracer tracer) {
        this.tracer = tracer;
    }

    /** to Json String */

    @Override
    public String toString() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(this);
    }

    public static void setStatDepths(List<Stat> stats) {
        logger.debug("============");
        int statNbr = 0;
        int seqNbr = 1;
        int depth = 0;
        for (Stat stat : stats) {
            stat.setSequenceNbr(seqNbr++);
            if (statNbr == 0) {
                stat.setDepth(0);
            } else {
                Stat parent = stats.get(stat.getParentId());
                int parentDepth = parent.getDepth();
                
                depth = parent.getDepth() + 1;
                logger.debug("parent depth : {} depth {)",parentDepth, depth);
                stat.setDepth(depth);
            }
            statNbr++;
            logger.debug("set depth to {} attempted {} {}", stat.getDepth(), depth, stat);
        }
    }

    // TODO this is copy paste from OracleTraceReport
    public String formatExplainPlan() {
        StringBuilder sb = new StringBuilder();

        if (getStats() != null) {
            setStatDepths(getStats());

            for (Stat stat : getStats()) {
                int indent = stat.getDepth() + 1;
                String formatString = "%-" + indent + "s%s\n";
             
                String line = String.format(formatString, "", stat.getOperation());
                //logger.debug("formatted: {}",line);
                sb.append(line);
                logger.debug("\n" + sb.toString());
            }
        }
        logger.debug("returning:\n{}",sb.toString());
        return sb.toString();
    }

    public Close getClose() {
        return close;
    }

    public void setClose(Close close) {
        this.close = close;
    }

}
