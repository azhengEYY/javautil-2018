/**
 * @(#) Stat.java
 */

package org.javautil.oracle.trace.record;

import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * STAT #<CURSOR> id=N cnt=0 [pid=0 pos=0 obj=0 op='SORT AGGREGATE ']
 * ----------------------------------------------------------------------------
 * 
 * STAT Lines report explain plan statistics for the numbered <CURSOR>.
 * 
 * <CURSOR> Cursor which the statistics apply to.
 * 
 * id Line of the explain plan which the row count applies to (starts at line
 * 1). This is effectively the row source row count for all row sources in the
 * execution tree. cnt Number of rows for this row source.
 * 
 * As of 7.3.3 the items in '[...]' are also reported:
 * 
 * pid Parent id of this row source. pos Position in explain plan. obj Object id
 * of row source (if this is a base object). op='...' The row source access
 * operation.
 * 
 * In reality we find this STAT #3 id=1 cnt=188220 pid=0 pos=1 obj=77538
 * op='INDEX FAST FULL SCAN FC_FCST_NDX_02 (cr=2366 pr=0 pw=0 time=1129914 us)'
 */
public class Stat extends CursorIdentifier {
    // private static Logger logger = LoggerFactory.getLogger(Stat.class.getName());
    private static final Logger logger = LoggerFactory.getLogger(Stat.class);
    private static Pattern         cursorNumberPattern  = Pattern.compile("^STAT #(\\d*) ");
    protected static final Pattern idPattern            = Pattern.compile("id=(\\d*)");

    protected static final Pattern cntPattern           = Pattern.compile("cnt=(\\d*)");
    protected static final Pattern parentIdPattern      = Pattern.compile("pid=(\\d*)");
    protected static final Pattern posPattern           = Pattern.compile("pos=(\\d*).*");
    protected static final Pattern objPattern           = Pattern.compile("obj=(\\d*)");
    protected static final Pattern operationPattern     = Pattern.compile("op='(.*) \\(");
    protected static final Pattern currentReadPattern   = Pattern.compile("\\(cr=(\\d*)");
    protected static final Pattern physicalReadPattern  = Pattern.compile("pr=(\\d*)");
    protected static final Pattern physicalWritePattern = Pattern.compile("pw=(\\d*)");
    protected static final Pattern timePattern          = Pattern.compile("time=(\\d*)");
    protected static final Pattern costPattern          = Pattern.compile("cost=(\\d*)");
    protected static final Pattern sizePattern          = Pattern.compile("size=(\\d*)");
    protected static final Pattern cardinalityPattern    = Pattern.compile("card=(\\d*)");

    /**
     * Index starting with 1.
     */
    private final int              id;
    private final int              cnt;
    /**
     * Id of the parent Stat Record
     */
    private final int              parentId;
    private final int              position;
    private final int              objectNumber;
    private final String           operation;
    private final int              consistentReads;
    private final int              physicalReads;
    private final int              physicalWrites;
    private final int              time;
    private final Long             cost;
    private final Long             size;
    private final Integer             cardinality;
    /**
     * How deep this is. If you follow the linked list
     */
    private int                    depth;
    private int sequenceNbr;

    public Stat(final String stmt, final int lineNumber) {
        super(lineNumber, stmt);
        id = getInt(stmt, idPattern);
        cnt = getInt(stmt, cntPattern);
        parentId = getInt(stmt, parentIdPattern);
        position = getInt(stmt, posPattern);
        objectNumber = getInt(stmt, objPattern);
        String operationText = getString(stmt,operationPattern);
      //  logger.info("operationText " + operationText);
        operation = operationText.trim();
        consistentReads = getInt(stmt, currentReadPattern);
        physicalReads = getInt(stmt, physicalReadPattern);
        physicalWrites = getInt(stmt, physicalWritePattern);
        
  
        int statTime = getInt(stmt,timePattern);
        //logger.info("stattime is " + statTime);
        time = statTime;
        cost = getLong(stmt,costPattern,false);
        size = getLong(stmt,sizePattern,false);
        cardinality = getInt(stmt,cardinalityPattern,false);
        
        
//        final Long cursorNumber = getLong(stmt, cursorNumberPattern);
//        setCursorNumber(cursorNumber);
    }

    public int getCnt() {
        return cnt;
    }

    public int getConsistentReads() {
        return consistentReads;
    }

    public int getId() {
        return id;
    }

    /**
     * @return the database object number
     */
    public int getObjectNumber() {
        return objectNumber;
    }

    /**
     * The op value in the trace log. Describes the operation.
     * 
     * @return the operation description.
     */
    public String getOperation() {
        return operation;
    }

    /**
     * 
     * @return the number of blocks read via operating system calls.
     */
    public int getPhysicalReads() {
        return physicalReads;
    }

    public int getPhysicalWrites() {
        return physicalWrites;
    }

    public int getPid() {
        return parentId;
    }

    /**
     * The position in the explain plan.
     * 
     * @return
     */
    public int getPosition() {
        return position;
    }

    @Override
    public RecordType getRecordType() {
        return RecordType.STAT;
    }

    public int getTime() {
        return time;
    }

    public int getObj() {
        return objectNumber;
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    /**
     * same as getPid
     * 
     * @return
     */
    public int getParentId() {
        return parentId;
    }

    public void setSequenceNbr(int sequenceNbr) {
       this.sequenceNbr = sequenceNbr;
        
    }

    public Long getCost() {
        return cost;
    }

    public Long getSize() {
        return size;
    }

    public Integer getCardinality() {
        return cardinality;
    }

}
