/**
 * @(#) Parsing.java
 */
package org.javautil.oracle.trace.record;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PARSING IN CURSOR #<CURSOR> len=X dep=X uid=X oct=X lid=X tim=X hv=X ad='X'
 * <statement> END OF STMT
 * ----------------------------------------------------------------------------
 * <CURSOR> Cursor number.
 * 
 * len Length of SQL statement.
 * dep Recursive depth of the cursor.
 * uid Schema user id of parsing user.
 * oct Oracle command type.
 * lid Privilege user id.
 * tim Timestamp.
 * 
 * Pre-Oracle9i, the times recorded by Oracle only have a resolution
 * of 1/100th of a second (10mS). As of Oracle9i some times are available to
 * microsecond accuracy (1/1,000,000th of a second). The timestamp can be used
 * to determine times between points in the trace file. The value is the value
 * in V$TIMER when the line was written. If there are TIMESTAMPS in the file you
 * can use the difference between 'tim' values to determine an absolute time. hv
 * Hash id. ad SQLTEXT address (see <View:V$SQLAREA> and <View:V$SQLTEXT>).
 * 
 * <statement> The actual SQL statement being parsed.
 */
public class Parsing extends AbstractParsing {
    private static transient Logger logger              = LoggerFactory.getLogger(Parsing.class);
    private static final String cursorNumberRegex = "^PARSING IN CURSOR #(\\d*).*";
    private static final Pattern    cursorNumberPattern = Pattern.compile(cursorNumberRegex);
    protected static final Pattern sqlidPattern             = Pattern.compile(" sqlid='([^']*)");
    private static String sqlId;

    public Parsing(final String stmt, final int lineNumber) {
        super(lineNumber,stmt);
        try {
            final Matcher sqlidMatcher = sqlidPattern.matcher(getText());
            sqlidMatcher.find();
            sqlId = sqlidMatcher.group(1);
        } catch (java.lang.IllegalStateException ise) {
            throw new IllegalStateException("pattern '" + cursorNumberRegex + "' does not match '" + stmt + "'");
        }
    }
    
    
    @Override
    public RecordType getRecordType() {
        return RecordType.PARSING;
    }


    @Override
    long parseCursorNumber() {
      final Matcher matcher = cursorNumberPattern.matcher(getText());
      matcher.find();
      return Long.parseLong(matcher.group(1));
    }


    public String getSqlid() {
        return sqlId;
    }


   


  

}
