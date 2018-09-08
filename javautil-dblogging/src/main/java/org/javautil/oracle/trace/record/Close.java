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

public class Close extends CursorOperation {
    public Close(final String record, final int lineNumber) {
        super(lineNumber, record);
    }

    @Override
    public RecordType getRecordType() {
        return RecordType.CLOSE;
    }

}
