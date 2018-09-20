/**
 * @(#) RecordFactory.java
 */
package org.javautil.oracle.trace;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.javautil.oracle.trace.record.Action;
import org.javautil.oracle.trace.record.Close;
import org.javautil.oracle.trace.record.Exec;
import org.javautil.oracle.trace.record.Fetch;
import org.javautil.oracle.trace.record.Module;
import org.javautil.oracle.trace.record.Parse;
import org.javautil.oracle.trace.record.ParseError;
import org.javautil.oracle.trace.record.Parsing;
import org.javautil.oracle.trace.record.Record;
import org.javautil.oracle.trace.record.RecordType;
import org.javautil.oracle.trace.record.Stat;
import org.javautil.oracle.trace.record.Wait;
import org.javautil.oracle.trace.record.Xctend;
import org.javautil.oracle.trace.record.Error;
import org.javautil.util.EventHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 */
public class TraceFileReader implements Closeable {
    private final Logger                  logger              = LoggerFactory.getLogger(this.getClass().getName());
    private static EventHelper            events              = new EventHelper();
    private static final Integer          LOG_PARSED_RECORD   = Integer.valueOf(1);
    private final String                  fileName;
    //
    private final ArrayList<String>       filePreamble        = new ArrayList<String>();
    //
    private LineNumberReader              br;

    private boolean                       endOfFile           = false;
    private String                        line;
    // private boolean needsRead;
    private final HashMap<String, String> waitTypes           = new HashMap<String, String>();
    private boolean                       logSkippedRecords   = true;
    private final boolean                 logSkippedTimestamp = false;
    private final boolean                 logSkippedSeparator = false;
    private Record                        record              = null;
    // log events
    // private boolean logRead = false;
    private final boolean                 logParsingFound     = false;
    private final boolean                 logSkipsInBind      = false;
    private final boolean                 logInitialSkips     = false;

    private int                           getNextCount        = 0;
    private int                           nonSkipCount        = 0;
    private int                           bindCount           = 0;
    private int                           xctendCount         = 0;
    private int                           parsingCount        = 0;
    private int                           parseCount;
    private int                           execCount;
    private int                           waitCount;
    private int                           fetchCount;
    private int                           statCount;
    private int                           endOfStatementCount;
    private int                           ignoredCount;
    private List<Integer>                 unhandledRecords = new LinkedList<>();
    private List<ParseError>              parseErrors = new LinkedList<>();
    
    private boolean showError;
    private boolean showParseError;
    // static {
    // events.addEvent(LOG_PARSED_RECORD);
    // events.addEvent(LOG_RAW_RECORD);
    // }

    public TraceFileReader(final String fileName) throws IOException {
        logger.info("reading file {}", fileName);
        this.fileName = fileName;
        br = new LineNumberReader(new BufferedReader(new FileReader(fileName)));
        initResources();
    }

    public TraceFileReader(final InputStream inputStream) throws IOException {
        this.fileName = null;
        br = new LineNumberReader(new BufferedReader(new InputStreamReader(inputStream)));
        initResources();
    }

    @Override
    public void close() throws IOException {
        br.close();
    }

    public Record getNext() throws IOException {
        getNextCount++;
        record = null;
        while (!endOfFile && record == null) {
            final RecordType recordType = RecordType.getRecordType(line);

            if (recordType != null) {
                switch (recordType) {
                case BIND:
                    processBinds();
                    bindCount++;
                    break;
                case XCTEND:
                    record = new Xctend(line, br.getLineNumber());
                    xctendCount++;
                    break;
                case PARSING:
                    record = new Parsing(line, br.getLineNumber()); //  TODO why create a new one?
                    final Parsing stmt = (Parsing) record;
                    while ((line = readLine()) != null) {
                        if (line.equals("END OF STMT")) {
                            break;
                        }
                        stmt.addLine(line);
                    }
                    parsingCount++;
                    break;
                case PARSE:
                    record = new Parse(line, br.getLineNumber());
                    parseCount++;
                    break;
                case EXEC:
                    record = new Exec(line, br.getLineNumber());
                    execCount++;
                    break;
//                case EXE:
//                    record = new Exe(line, br.getLineNumber());
//                    break;
                case WAIT:
                    record = new Wait(line, br.getLineNumber());
                    final Wait wait = (Wait) record;
                    wait.setWaitType(getWaitType(wait.getWaitType()));
                    wait.clean(); // TODO ??
                    waitCount++;
                    break;
                case FETCH:
                    record = new Fetch(line, br.getLineNumber());
                    fetchCount++;
                    break;
                case STAT:
                    record = new Stat(line, br.getLineNumber());
                    statCount++;
                    break;
                case END_OF_STATEMENT:
                    endOfStatementCount++;
                    break;
                case IGNORED:
                    ignoredCount++;
                    break;
                case ACTION:
                    record = new Action(line, br.getLineNumber());
                    break;
                case MODULE:
                    record = new Module(line, br.getLineNumber());
                    break;
                case APP_NAME:
                    break;
                case ERROR:
                    record  = new Error(br.getLineNumber(),line);
                    if (showError) {
                        System.out.println(String.format("error found: %d %s",br.getLineNumber(),line));
                    }

                    break;
                case PARSE_ERROR:
                    
                    ParseError pe   = new ParseError(br.getLineNumber(),line);
                    record = pe;
                    logger.debug("parse error found: {} {}",br.getLineNumber(),line);
                    while ((line = readLine()) != null) {
                        if (line.startsWith("***")) {  // TOOD create regex or do a lookahead to see what next is
                            break;
                        }
                        if (showParseError) {
                            System.out.println(String.format("parse error found: %d %s",br.getLineNumber(),line));
                        }
                        pe.addLine(line);
                    }
                    parseErrors.add(pe);
                    break;
                case SEPARATOR:
                    break;
                case STARS:
                    break;
                case TIMESTAMP:
                    break;
                case UNMAP:
                    break;
                case CLOSE:
                    record = new Close(line, br.getLineNumber());
                    break;
                case LOBREAD:
                  
                default:
                    logUnhandled(br.getLineNumber(),line, recordType);
                   
       
                    logSkip();
                }
            }
            if (record != null) {
                nonSkipCount++;
                if (events.exists(LOG_PARSED_RECORD)) {
                    logger.debug(record.toString());
                }
            }
            readLine();
        }
        if (record != null) {
            logRecordType(record);
        }
        if (record == null) {
            logger.debug("returning null record");
        } else if (events.exists(LOG_PARSED_RECORD)) {
            logger.debug(record.toString());
        }
        return record;
    }

    private void logUnhandled(int lineNumber, String line, RecordType recordType) {
       unhandledRecords.add(lineNumber);
       logger.warn("unhandled recordType {} text: '{}'", recordType, line);
        
    }

    private String getWaitType(final String type) {
        String returnValue = waitTypes.get(type);
        if (returnValue == null) {
            returnValue = type;
            waitTypes.put(type, type);
        }
        return returnValue;
    }

    private void initResources() throws IOException {
        try {
            readPreamble();
        } catch (final FileNotFoundException fnfe) {
            logger.error("Cannot locate input file! " + fnfe.getMessage());
            throw fnfe;
        }
    }

    private void logRecordType(final Record rec) {
        if (rec.getRecordType() != null) {
            switch (rec.getRecordType()) {
            case PARSING:
                if (logParsingFound) {
                    logger.info("found parsing");
                }
                break;
            default:
                break;
            case BIND:
            }
        }
    }

    private void logSkip() {
        if (logSkippedRecords) {
            if (nonSkipCount == 0 && !logInitialSkips) {
                return;
            }
            if (line == null) {
                throw new IllegalStateException("line is null");
            }
            while (true) {
                if (line.startsWith("*** 20") && !logSkippedTimestamp) {
                    break;
                }
                if (line.startsWith("=====") && !logSkippedSeparator) {
                    break;
                }
                if (line.trim().length() > 0) {
                    logger.warn("skipped {} '{}'", br.getLineNumber(), line);
                }
                break;
            }
        }
    }

    private String readLine() throws IOException {

        line = br.readLine();
        if (line == null) {
            endOfFile = true;
        } else {
            if (events.exists(LOG_PARSED_RECORD)) {
                logger.info("line # " + br.getLineNumber() + ": " + line);
            }
        }
        return line;
    }

    private void readPreamble() throws IOException {
        while (  (line = br.readLine()) != null) {
          
            if (line.startsWith("==")) {
                break;
            }
            if (logger.isDebugEnabled()) {
                logger.debug("preamble " + line);
            }
            filePreamble.add(line);
        }
    }

    void processBinds() throws IOException {
        final boolean saveLogSkipsInBind = logSkipsInBind;
        logSkippedRecords = logSkipsInBind;
        while ((line = readLine()) != null) {
            if (line.charAt(0) != ' ') {
                break;
            }

        }
        logSkippedRecords = saveLogSkipsInBind;

    }

    public int getGetNextCount() {
        return getNextCount;
    }

    public int getNonSkipCount() {
        return nonSkipCount;
    }

    public int getBindCount() {
        return bindCount;
    }

    public int getXctendCount() {
        return xctendCount;
    }

    public int getParsingCount() {
        return parsingCount;
    }

    public int getParseCount() {
        return parseCount;
    }

    public int getExecCount() {
        return execCount;
    }

    public int getWaitCount() {
        return waitCount;
    }

    public int getFetchCount() {
        return fetchCount;
    }

    public int getStatCount() {
        return statCount;
    }

    public int getEndOfStatementCount() {
        return endOfStatementCount;
    }

    public int getIgnoredCount() {
        return ignoredCount;
    }

    public String getFileName() {
        return fileName;
    }

    public int getLineNumber() {

        return br.getLineNumber();
    }
}
