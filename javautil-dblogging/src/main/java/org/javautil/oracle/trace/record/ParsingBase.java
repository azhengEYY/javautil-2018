///**
// * @(#) Parsing.java
// */
//package org.javautil.oracle.trace.record;
//
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
///**
// * PARSING IN CURSOR #<CURSOR> len=X dep=X uid=X oct=X lid=X tim=X hv=X ad='X'
// * <statement> END OF STMT
// * ----------------------------------------------------------------------------
// * <CURSOR> Cursor number.
// * 
// * len Length of SQL statement.
// * dep Recursive depth of the cursor.
// * uid Schema user id of parsing user.
// * oct Oracle command type.
// * lid Privilege user id.
// * tim Timestamp.
// * 
// * Pre-Oracle9i, the times recorded by Oracle only have a resolution
// * of 1/100th of a second (10mS). As of Oracle9i some times are available to
// * microsecond accuracy (1/1,000,000th of a second). The timestamp can be used
// * to determine times between points in the trace file. The value is the value
// * in V$TIMER when the line was written. If there are TIMESTAMPS in the file you
// * can use the difference between 'tim' values to determine an absolute time. hv
// * Hash id. ad SQLTEXT address (see <View:V$SQLAREA> and <View:V$SQLTEXT>).
// * 
// * <statement> The actual SQL statement being parsed.
// */
//public abstract class AbstractParsing extends AbstractCursorEvent {
//    private static transient Logger logger              = LoggerFactory.getLogger(AbstractParsing.class);
//    // TODO can't this all be in one expression
//  //
//  //  private static final Pattern    cursorNumberPattern = Pattern.compile("^PARSING IN CURSOR #(\\d*) ");
//
//
//    private final int               sqlTextLength;
//    /**
//     * Recursion depth
//     */
//    private final int               recursionDepth;
//    private final int               uid;
//    /*
//     * oct Oracle command type.
//     */
//    private final int               oracleCommandType;
//    private final int               lid;
//    private final long              sqlTextHashValue;
//    private final long              timestamp;
//    
//    /**
//     * Optimizer goal. In trace file as "og". og Optimizer goal:
//     * 1=All_Rows,
//     * 2=First_Rows,
//     * 3=Rule,
//     * 4=Choose
//     * TODO english
//     */
//    private int                    optimizerGoal;
//    /**
//     * ad SQLTEXT address (see <View:V$SQLAREA> and <View:V$SQLTEXT>).
//     */
//    private final String            sgaAddress;
//    private StringBuilder           sqltextBuffer       = new StringBuilder();
//
//
//    public AbstractParsing(final String stmt, final int lineNumber) {
//        super(lineNumber, stmt);
//     
//        final Matcher lenMatcher = sqlTextLengthPattern.matcher(stmt);
//        final Matcher depMatcher = recursionDepthPattern.matcher(stmt);
//        final Matcher uidMatcher = userIdPattern.matcher(stmt);
//        final Matcher octMatcher = oracleCommandTypePattern.matcher(stmt);
//        final Matcher lidMatcher = lidPattern.matcher(stmt);
//        final Matcher timMatcher = timPattern.matcher(stmt);
//        final Matcher hvMatcher = sqltextHashValuePattern.matcher(stmt);
//        final Matcher adMatcher = sgaAddressPattern.matcher(stmt);
//        final Matcher sqlidMatcher = sqlidPattern.matcher(stmt);
////        if (!cursorNumberMatcher.find()) {
////            throw new IllegalStateException("cursorNumberMatcher failed on " + stmt);
////        }
//        if (!lenMatcher.find()) {
//            throw new IllegalStateException("lenMatcher failed ");
//        }
//        if (!depMatcher.find()) {
//            throw new IllegalStateException("depMatcher failed ");
//        }
//        if (!uidMatcher.find()) {
//            throw new IllegalStateException("uidMatcher failed ");
//        }
//        if (!octMatcher.find()) {
//            throw new IllegalStateException("octMatcher failed ");
//        }
//        if (!lidMatcher.find()) {
//            throw new IllegalStateException("lidMatcher failed ");
//        }
//        if (!timMatcher.find()) {
//            throw new IllegalStateException("timMatcher failed ");
//        }
//        if (!hvMatcher.find()) {
//            throw new IllegalStateException("hvMatcher failed ");
//        }
//        if (!adMatcher.find()) {
//            throw new IllegalStateException("adMatcher failed ");
//        }
//        if (!sqlidMatcher.find()) {
//            throw new IllegalStateException("sqlidMatcher failed ");
//        }
//  //      setCursorNumber(Long.parseLong(cursorNumberMatcher.group(1)));
//        sqlTextLength = Integer.parseInt(lenMatcher.group(1));
//        recursionDepth = Integer.parseInt(depMatcher.group(1));
//        uid = Integer.parseInt(uidMatcher.group(1));
//        oracleCommandType = Integer.parseInt(octMatcher.group(1));
//        timestamp = Long.parseLong(timMatcher.group(1));
//        lid = Integer.parseInt(lidMatcher.group(1));
//        sqlTextHashValue = Long.parseLong(hvMatcher.group(1));
//        sgaAddress = adMatcher.group(1);
//     
//        super.setSqlid(sqlidMatcher.group(1));
//        logger.debug(this.toString());
//    }
//
//    public void addLine(final String line) {
//        if (sqltextBuffer.length() > 0) {
//            sqltextBuffer.append("\n");
//        }
//        sqltextBuffer.append(line);
//    }
//
//    // @todo ensure called
//    public void clean() {
//        sqltextBuffer = null;
//    }
//
//    /**
//     * @return Returns the address in hex at which the cursor was located in the
//     *         SGA.
//     * 
//     *         Identified by ad in the trace file.
//     */
//    public String getSgaAddress() {
//        return sgaAddress;
//    }
//
//    /**
//     * @return Returns the dep.
//     * 
//     *         dep is the tag for recursion depth.
//     */
//    public int getRecursionDepth() {
//        return recursionDepth;
//    }
//
//    /**
//     * @return The hash value of the sqlText as store in the SGA.
//     * 
//     *         Not really useful here other than for checking duplicate
//     *         statements.
//     */
//    public long getSqlTextHashValue() {
//        return sqlTextHashValue;
//    }
//
//    /**
//     * @return Returns the lid.
//     */
//    public int getLid() {
//        return lid;
//    }
//
//    /**
//     * @return Returns the oct.
//     * 
//     *         Oct is the Oracle Command Type.
//     * 
//     * @todo document what the value means
//     */
//    public int getOracleCommandType() {
//        return oracleCommandType;
//    }
//
//
//
//    public String getSqlText() {
//        return sqltextBuffer.toString();
//    }
//
//    /**
//     * @return Returns the len.
//     * 
//     *         Len is the length of the statement text.
//     */
//    public int getSqlTextLength() {
//        return sqlTextLength;
//    }
//
//    public long getTimestamp() {
//        return timestamp;
//    }
//
//    /**
//     * @return Returns the uid.
//     */
//    public int getUid() {
//        return uid;
//    }
//    
//
//    public int getOptimizerGoal() {
//        return optimizerGoal;
//    }
//
//    
//    
//
//}
