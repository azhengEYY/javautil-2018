/**
 * @(#) RecordFactory.java
 */
package org.javautil.oracle.trace;

import java.io.File;
import java.io.IOException;

import org.javautil.io.Tracer;
import org.javautil.oracle.trace.record.CursorOperation;
import org.javautil.oracle.trace.record.Parsing;
import org.javautil.oracle.trace.record.Record;
import org.javautil.oracle.trace.record.Stat;
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

    private File                  inputFile;
    private Tracer                tracer  = new Tracer();

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
