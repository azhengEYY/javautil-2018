/**
 * @(#) Exec.java
 */

package org.javautil.oracle.trace.record;

/**
 * Despite the fact that this extends CursorOperation there is no
 * PhysicalBlocksRead, CurrentModeBlocks, LibraryCacheMissCount or RowCount
 * @author jjs
 *
 */

public class Close extends DbTime {
    private int depth;
    private long time;
    private long cursorNumber;


    public Close(final String record, final int lineNumber) {
        super(lineNumber, record);
        depth = getInt(record,depthPattern);
        time = getLong(record,timePattern);
        cursorNumber = getLong(record,cursorNumberPattern);
    }

    public long getCursorNumber() {
        return cursorNumber;
    }

    @Override
    public RecordType getRecordType() {
        return RecordType.CLOSE;
    }

    public int getRecursionDepth() {
        return depth;
    }

    public long getTime() {
        return time;
    }





}
