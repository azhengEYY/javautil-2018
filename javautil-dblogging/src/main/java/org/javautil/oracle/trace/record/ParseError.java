
package org.javautil.oracle.trace.record;


import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PARSE ERROR #%d:len=%ld dep=%d uid=%ld oct=%d lid=%ld tim=%lu err=%d
 * <statement> ...
 * ----------------------------------------------------------------------------
 * 
 * PARSE ERROR In Oracle 7.2+ parse errors are reported to the trace file.
 * 
 * len Length of SQL statement. dep Recursive depth of the statement uid User
 * id. oct Oracle command type (if known). lid Privilege user id. tim Timestamp.
 * err Oracle error code (e.g. ORA-XXXXX) reported
 * 
 * <statement> The SQL statement that errored. If this contains a password, the
 * statement is truncated as indicated by '...' at the end.
 * 
 * PARSE ERROR #140729315264320:len=3885 dep=0 uid=105 oct=1 lid=105 tim=100406013140 err=922
 * PARSING IN CURSOR #<CURSOR> len=X dep=X uid=X oct=X lid=X tim=X hv=X ad='X'
 */
public class ParseError extends AbstractParsing {

    private static final Pattern parseErrorCursorNumberPattern = Pattern.compile("^PARSE ERROR #(\\d*)");
    private static final Pattern errorNumberPattern            = Pattern.compile(".*err=(\\d*)");
    private int errorNumber;
    private Logger logger = LoggerFactory.getLogger(AbstractParsing.class);
    private List<String> errors = new LinkedList<>();

    public ParseError(int lineNumber, String stmt) {
    
        super(lineNumber, stmt);
        logger.info("called Super");
        Matcher errorMatcher = errorNumberPattern.matcher(stmt);
        if (! errorMatcher.find()) {
            logger.error("no match for " + stmt);
        }
        errorNumber = Integer.parseInt(errorMatcher.group(1));
    }

    @Override
    public RecordType getRecordType() {
        return RecordType.PARSE_ERROR;
    }

    public int getErrorNumber() {
       return errorNumber;

    }

    long parseCursorNumber() {
        String stmt = getText();
        final Matcher cursorNumberMatcher = parseErrorCursorNumberPattern.matcher(stmt);
        if (!cursorNumberMatcher.find()) {
            throw new IllegalStateException("cursorNumberMatcher failed on " + stmt);
        }

        long cursorNumber = Long.parseLong(cursorNumberMatcher.group(1));
        return cursorNumber;

    }
    
    public void addLine(String text) {
        errors.add(text);
    }
}
