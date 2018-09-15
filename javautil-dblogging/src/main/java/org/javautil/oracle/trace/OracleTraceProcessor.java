/**
 * @(#) RecordFactory.java
 */
package org.javautil.oracle.trace;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;

import org.javautil.io.Tracer;
import org.javautil.oracle.trace.record.Close;
import org.javautil.oracle.trace.record.CursorOperation;
import org.javautil.oracle.trace.record.Xctend;
import org.javautil.oracle.trace.record.Parsing;
import org.javautil.oracle.trace.record.Record;
import org.javautil.oracle.trace.record.Stat;
import org.javautil.oracle.trace.record.Xctend;
import org.javautil.oracle.trace.record.ParseError;
import org.javautil.oracle.trace.record.Error;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO put fileName in trace File
 * https://asktom.oracle.com/pls/asktom/f?p=100:11:0::::P11_QUESTION_ID:5289107304876
 * https://docs.oracle.com/cd/B10500_01/server.920/a96533/o_trace.htm
 */

/**
 * TODO a trace file may not have all of the information necessary
 * for example if the trace file name is changed then the Parsing might not be
 * in
 * the file.
 * 
 * @author jjs
 *
 */
public class OracleTraceProcessor {
    private final Logger          logger  = LoggerFactory.getLogger(getClass().getName());
    private final TraceFileReader reader;
    private Record                record  = null;

    private int                   recordCount;
    private final CursorsStats    cursors = new CursorsStats();
    private TreeMap<Long,Xctend>  xctendByTimestamp = new TreeMap<>();

    private File                  inputFile;
    private Tracer                tracer  = new Tracer();
    private List<ParseError>      parseErrors= new LinkedList<>();
    private List<Error>     errors= new LinkedList<>();

    public OracleTraceProcessor(final String fileName) throws IOException {
        reader = new TraceFileReader(fileName);
        logger.info("processing file " + fileName);
    }

    public OracleTraceProcessor(final File file) throws IOException {
        this.inputFile = file;
        reader = new TraceFileReader(file.getAbsolutePath());
        logger.info("processing file " + file.getAbsolutePath());
    }

    public void trace(String text) {
        tracer.traceln(text);
    }

    public void process() throws IOException {
        cursors.setTracer(tracer);
        trace("processing file " + this.inputFile);
        while ((record = reader.getNext()) != null) {
            trace(record.getLineAndText());
            switch (record.getRecordType()) {
            case PARSING:
                Parsing parsing = (Parsing) record;
                cursors.handle(parsing);
                trace("parsing " + parsing.getLineAndText());
                break;
            case CLOSE:
                Close close = (Close) record;
                cursors.handle(close);
                break;
            case UNMAP:
            case EXEC:
            case FETCH:
            case PARSE:
                trace("found cursor " + record.getLineAndText());
                CursorOperation operation = (CursorOperation) record;
                cursors.handle(operation);
                break;
            case STAT:
                Stat stat = (Stat) record;
                cursors.handle(stat);
                break;
            case XCTEND:
                Xctend exctend = (Xctend) record;
                xctendByTimestamp.put(exctend.getTime(), exctend);
             //   logger.warn("Xctend not handled {}",record);
                break;
            case PARSE_ERROR:
                parseErrors.add((ParseError) record);
                break;
            case ERROR:
                errors.add((Error) record);
                break;
            default:
                logger.warn("unhandled: {}", record);
            }
        }
        recordCount = reader.getLineNumber();
        reader.close();
    }

    public CursorsStats getCursors() {
        return cursors;
    }

    public void setTracer(Tracer tracer) {
        this.tracer = tracer;
    }

    public int getRecordCount() {
        return recordCount;
    }
}
