/**
 * @(#) Operation.java
 */

package org.javautil.oracle.trace.record;

import java.util.regex.Pattern;

import org.javautil.util.EventHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO this needs to be the parent of all the appropriate classes
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
public class TimeAndIO extends DbTime {
    private static Logger          logger                       = LoggerFactory
            .getLogger(TimeAndIO.class.getName());

   
    private static EventHelper     events                       = new EventHelper();
    private static final Integer   LOG_CONSTRUCTOR              = 1;

    protected static final Pattern consistentReadBlocksPattern  = Pattern.compile("cr=(\\d*).*");
    protected static final Pattern currentModePattern           = Pattern.compile("cu=(\\d*)");
 
    protected static final Pattern physicalBlocksReadPattern    = Pattern.compile("p=(\\d*)");
    protected static final Pattern timePattern                  = Pattern.compile("tim=(\\d*)");
    /**
     * Number of blocks read from cache in consistent mode.
     * 
     * In trace file as "cr".
     */
    private int                    consistentReadBlocks;
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
     * Number of database blocks obtained by O.S. read calls.
     * 
     * In trace file as "p".
     */
    private int                    physicalBlocksRead;

  

    private long                   time;

    
   // private int recursionDepth;

    public TimeAndIO(int lineNumber, final String traceLine) {
        super(lineNumber, traceLine);
        setPhysicalBlocksRead(getInt(traceLine, physicalBlocksReadPattern));
        setConsistentReadBlocks(getInt(traceLine, consistentReadBlocksPattern));
        setCurrentModeBlocks(getInt(traceLine, currentModePattern));
        setTime(getLong(traceLine, timePattern));
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
    public int getCurrentModeBlocks() {
        return currentModeBlocks;
    }


    /**
     * @return
     */
    public int getPhysicalBlocksRead() {
        return physicalBlocksRead;
    }



    public long getTime() {
        return time;
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
     * @param cu
     *            The cu to set.
     */
    protected void setCurrentModeBlocks(final int cu) {
        this.currentModeBlocks = cu;
    }
    


    /**
     * @param p
     *            The p to set.
     */
    protected void setPhysicalBlocksRead(final int p) {
        this.physicalBlocksRead = p;
    }



    private void setTime(final long time) {
        if (time == 0) {
            logger.info("time not set");
        }
        this.time = time;
    }


    @Override
    public RecordType getRecordType() {
        throw new UnsupportedOperationException();
    }
    


    
}
