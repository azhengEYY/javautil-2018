/**
 * @(#) Exec.java
 */

package org.javautil.oracle.trace.record;

/**
*
*/
public class Exec extends CursorOperation {
    public Exec(final String record, final int lineNumber) {
        super(lineNumber, record);
    }

    @Override
    public RecordType getRecordType() {
        return RecordType.EXEC;
    }

}
