/**
 * @(#) Error.java
 */

package org.javautil.oracle.trace.record;

import java.util.LinkedList;
import java.util.List;

/**
 * ERROR #%d:err=%d tim=%lu
 * ----------------------------------------------------------------------------
 * SQL Error shown after an execution or fetch error.
 * 
 * err Oracle error code (e.g. ORA-XXXXX) at the top of the stack. tim
 * Timestamp.
 */
public class Error extends CursorIdentifier implements Record {
    
    private long time;
    
    private List<String> errors = new LinkedList<>();

    public Error(int lineNumber, String stmt) {
        super(lineNumber, stmt);
        time = getLong(stmt, timePattern);

    }

    @Override
    public RecordType getRecordType() {
        return RecordType.ERROR;
    }
    
    public long getTime() {
        return time;
    }
    
    public void addLine(String text) {
        errors.add(text);
    }
}
