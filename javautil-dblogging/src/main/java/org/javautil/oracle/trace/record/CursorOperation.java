/**
 * @(#) Operation.java
 */

package org.javautil.oracle.trace.record;

import java.util.regex.Pattern;

import org.javautil.util.EventHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO FORMAT javadoc
/**
 * ----------------------------------------------------------------------------
 * PARSE #<CURSOR>:c=0,e=0,p=0,cr=0,cu=0,mis=0,r=0,dep=0,og=4,tim=0 
 * EXEC #<CURSOR>:c=0,e=0,p=0,cr=0,cu=0,mis=0,r=0,dep=0,og=4,tim=0 
 * FETCH #<CURSOR>:c=0,e=0,p=0,cr=0,cu=0,mis=0,r=0,dep=0,og=4,tim=0 
 * UNMAP #<CURSOR>:c=0,e=0,p=0,cr=0,cu=0,mis=0,r=0,dep=0,og=4,tim=0
 * ----------------------------------------------------------------------------
 * - OPERATIONS:
 * 
 * PARSE Parse a statement.
 * EXEC Execute a pre-parsed statement.
 * FETCH Fetch rows from a cursor.
 * UNMAP If the cursor uses a temporary table, when the
 * cursor is closed you see an UNMAP when we free up the temporary table
 * locks.(Ie: free the lock, delete the state object, free the temp segment) In
 * tkprof, UNMAP stats get added to the EXECUTE statistics. SORT UNMAP As above,
 * but for OS file sorts or TEMP table segments.
 * 
 * c CPU time (100th's of a second in Oracle7 ,8 and 9).
 * e Elapsed time (100th's of a second Oracle7, 8 Microseconds in Oracle 9
 * onwards).
 * p Number of physical reads.
 * cr Number of buffers retrieved for CR reads.
 * cu Number of buffers retrieved in current mode.
 * mis Cursor missed in the cache.
 * r Number of rows processed.
 * dep Recursive call depth (0 = user SQL, >0 = recursive).
 * og Optimizer goal:
 * 1=All_Rows,
 * 2=First_Rows,
 * 3=Rule,
 * 4=Choose
 * tim Timestamp (large number in 100ths of a second). Use this to determine the
 * time between
 * any 2 operations.
 *                              
#          cursorNumber         
c          CpuMicroSec
cr         consistentR
cu         currentMode
dep        depth
e          elapsedMicr
mis        libraryCach
og         optimizerGo
p          physicalBlo
plh
r          rowCount
tim

 */
public abstract class CursorOperation extends CursorIdentifier implements Record {
    private static Logger          logger                       = LoggerFactory
            .getLogger(CursorOperation.class.getName());

   
    private static EventHelper     events                       = new EventHelper();
    private static final Integer   LOG_CONSTRUCTOR              = 1;

    protected static final Pattern consistentReadBlocksPattern  = Pattern.compile("cr=(\\d*).*");
    protected static final Pattern cpuPattern                   = Pattern.compile("c=(\\d*)");
    protected static final Pattern currentModePattern           = Pattern.compile("cu=(\\d*)");
 
    protected static final Pattern depthPattern                 = Pattern.compile("dep=(\\d*)");
    protected static final Pattern elapsedMicrosecondsPattern   = Pattern.compile("e=(\\d*)");
    protected static final Pattern libraryCacheMissCountPattern = Pattern.compile("mis=(\\d*)");
    protected static final Pattern ogPattern                    = Pattern.compile("og=(\\d*)");
    protected static final Pattern physicalBlocksReadPattern    = Pattern.compile("p=(\\d*)");
    protected static final Pattern rowPattern                   = Pattern.compile("[^a-z]r=(\\d*)");
    protected static final Pattern timePattern                  = Pattern.compile("tim=(\\d*)");
    protected static final Pattern explainHashPattern                  = Pattern.compile("plh=(\\d*)");
//  protected static final Pattern cursorNumberPattern      = Pattern.compile(cursorNumberRegex);
  //  public static final Pattern cursorNumberPattern = Pattern.compile(".*#(\\d*).*");
    /**
     * Number of blocks read from cache in consistent mode.
     * 
     * In trace file as "cr".
     */
    private int                    consistentReadBlocks;
    /*****************************************************************
     * elapsed times are centiseconds in 7,8 and microseconds in 8,10
     */
    /**
     * Total cpu consumption for call. Represented in trace file as c=%d c CPU
     * time (100th's of a second in Oracle7 ,8 and 9).
     */
    private int                    cpu;

    /**
     * Number of blocks retrieved from cache in current mode.
     * In trace file as "cu".
     */
    private int                    currentModeBlocks;
    /**
     * Recursive depth. In trace file as "dep".
     */
//    private int                    depth;

    /**
     * Elapsed duration of the call. In trace file as "e".
     */
    /**
     * Elapsed curation of timed event;
     */
    private long                   elapsedMicroSeconds;
    /**
     * Number of misses on the library cache.
     */
    private int                    libraryCacheMissCount;

    /**
     * Optimizer goal. In trace file as "og". og Optimizer goal:
     * 1=All_Rows,
     * 2=First_Rows,
     * 3=Rule,
     * 4=Choose
     * TODO english
     */
    private int                    optimizerGoal;

    /**
     * Number of database blocks obtained by O.S. read calls.
     * 
     * In trace file as "p".
     */
    private int                    physicalBlocksRead;

    /**
     * Number of rows returned. In trace file as "r".
     */
    private int                    rowCount;

    private long                   time;

    private boolean                echoInput;

    private long explainHash;
    
   // private Long                   cursorNumber;
    
  //  private long cursorNumber;
    
    private int recursionDepth;

    public CursorOperation(int lineNumber, final String traceLine) {
        super(lineNumber, traceLine);
//        setCursorNumber(getLong(traceLine, cursorNumberPattern));
        setCpu(getInt(traceLine, cpuPattern));
        setElapsedMicroSeconds(getLong(traceLine, elapsedMicrosecondsPattern));
        setPhysicalBlocksRead(getInt(traceLine, physicalBlocksReadPattern));
        setConsistentReadBlocks(getInt(traceLine, consistentReadBlocksPattern));
        setCurrentModeBlocks(getInt(traceLine, currentModePattern));
        setLibraryCacheMissCount(getInt(traceLine, libraryCacheMissCountPattern));
        setTime(getLong(traceLine, timePattern));
        recursionDepth = getInt(traceLine, depthPattern);
        setOptimizerGoal(getInt(traceLine, ogPattern));
        setRowCount(getInt(traceLine, rowPattern));
        setExplainHash(getLong(traceLine,explainHashPattern));

        if (events.exists(LOG_CONSTRUCTOR)) {
            logRecord(traceLine);
        }
    }

   

    // TODO what the hell is this?
    private void append(final StringBuilder b, final String label, final long stat) {
        b.append(label);
        b.append(": ");
        b.append(stat);
        b.append(" ");
    }

    /**
     * @return
     */
    public int getConsistentReadBlocks() {
        return consistentReadBlocks;
    }

    /**
     * @return
     */
    public int getCpu() {
        return cpu;
    }

    /**
     * @return
     */
    public int getCurrentModeBlocks() {
        return currentModeBlocks;
    }

//    /**
//     * @return Returns the dep.
//     */
//    protected int getDepth() {
//        return depth;
//    }

    /**
     * @return Returns the e.
     */
    public long getElapsedMicroSeconds() {
        return elapsedMicroSeconds;
    }

    /**
     * @return Returns the mis.
     */
    public int getLibraryCacheMissCount() {
        return libraryCacheMissCount;
    }

    /**
     * @return Returns the og.
     */
    protected int getOptimizerGoal() {
        return optimizerGoal;
    }

    /**
     * @return
     */
    public int getPhysicalBlocksRead() {
        return physicalBlocksRead;
    }



    /**
     * @return Returns the r.
     */
    public int getRowCount() {
        return rowCount;
    }

    public long getTime() {
        return time;
    }

    // TODO nuke
    private void logRecord(final String traceLine) {
        final StringBuilder b = new StringBuilder();
        if (echoInput) {
            b.append("input: " + traceLine + "\n");
        }
        b.append(toString());
        logger.info(b.toString());
    }

    /**
     * @param cr
     *            The cr to set.
     */
    protected void setConsistentReadBlocks(final int cr) {
        // System.out.println("CursorOperation setConsistentReadBlocks: " + cr);
        this.consistentReadBlocks = cr;
    }

    /**
     * @param c
     *            The c to set.
     */
    protected void setCpu(final int c) {
        this.cpu = c;
    }

    /**
     * @param cu
     *            The cu to set.
     */
    protected void setCurrentModeBlocks(final int cu) {
        this.currentModeBlocks = cu;
    }
    
//    private void setCursorNumber(long cursorNumber) {
//        this.cursorNumber= cursorNumber;
//    }

//    /**
//     * @param dep
//     *            The dep to set.
//     */
//    protected void setDepth(final int dep) {
//        this.depth = dep;
//    }

    /**
     * @param ela
     *            The ela to set.
     */
    protected void setElapsedMicroSeconds(final long ela) {
        this.elapsedMicroSeconds = ela;
    }

    /**
     * @param mis
     *            The mis to set.
     */
    protected void setLibraryCacheMissCount(final int mis) {
        this.libraryCacheMissCount = mis;
    }

    /**
     * @param og
     *            The og to set.
     */
    protected void setOptimizerGoal(final int og) {
        this.optimizerGoal = og;
    }

    /**
     * @param p
     *            The p to set.
     */
    protected void setPhysicalBlocksRead(final int p) {
        this.physicalBlocksRead = p;
    }

    /**
     * @param r
     *            The r to set.
     */
    protected void setRowCount(final int r) {
        this.rowCount = r;
    }

    private void setTime(final long time) {
        if (time == 0) {
            logger.info("time not set");
        }
        this.time = time;
    }
    
    private void setExplainHash(long explainHash) {
        this.explainHash = explainHash;
        
    }
    
    public long getExplainHash() {
        return explainHash; 
    }
    
    public int getRecursionDepth() {
        return recursionDepth;
    }
    
//  public long getCursorNumber() {
//  return cursorNumber;
//}
//
    
//    @Override
//    public String toString() {
//        Gson gson = new GsonBuilder().setPrettyPrinting().create();
//        return gson.toJson(this);
//    }

    @Override
    public String toString() {
        final StringBuilder b = new StringBuilder();

        b.append("recordType: " + getRecordType() + " ");
        b.append("lineNumber: " + getLineNumber() + " ");
        b.append("sqlid: " + getSqlid() + " ");
        append(b, "cursorNumber", getCursorNumber());
        append(b, "cpu", cpu);
        append(b, "elapsed", elapsedMicroSeconds);
        append(b, "pr", physicalBlocksRead);
        append(b, "pw", consistentReadBlocks);
        append(b, "cu", currentModeBlocks);
        append(b, "mis", libraryCacheMissCount);
        append(b, "time", time);
        append(b, "dep", getRecursionDepth());
        append(b, "og", optimizerGoal);
        return b.toString();
    }
}
