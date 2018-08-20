/**
 * @(#) Exec.java
 */

package org.javautil.oracle.trace.record;

/**
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
