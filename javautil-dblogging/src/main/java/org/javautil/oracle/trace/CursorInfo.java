package org.javautil.oracle.trace;

import java.util.ArrayList;
import java.util.Collection;

import org.javautil.io.Tracer;
import org.javautil.oracle.trace.record.CursorOperation;
import org.javautil.oracle.trace.record.Parse;
import org.javautil.oracle.trace.record.Parsing;
import org.javautil.oracle.trace.record.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class CursorInfo {
    private transient Logger           logger = LoggerFactory.getLogger(getClass());

    // private transient Cursor cursor;
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

    // public CursorInfo(Cursor cursor) {
    // this.cursor = cursor;
    // this.cursorNumber = cursor.getCursorNumber();
    // }

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
        this.parse = record;
    }

    public CursorInfo(Stat record) {
        this.cursorNumber = record.getCursorNumber();
    }

    public CursorInfo() {
    }

    public void aggregate(Collection<CursorInfo> cursors) {
        parseAggregation = new CursorOperationAggregation();
        fetchAggregation = new CursorOperationAggregation();
        execAggregation = new CursorOperationAggregation();
        unmapAggregation = new CursorOperationAggregation();
        closeAggregation = new CursorOperationAggregation();
        parseAggregation.setTracer(tracer);
        fetchAggregation.setTracer(tracer);
        execAggregation.setTracer(tracer);
        closeAggregation.setTracer(tracer);
        parseAggregation.setTracer(tracer);

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
}