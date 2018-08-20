/**
 * @(#) Xctend.java
 */

package org.javautil.oracle.trace.record;

/**
 * XCTEND rlbk=%d rd_only=%d
 * ----------------------------------------------------------------------------
 * XCTEND A transaction end marker.
 * 
 * rlbk 1 if a rollback was performed, 0 if no rollback (commit). rd_only 1 if
 * transaction was read only, 0 if changes occurred.
 */

// @todo parse
public class Action extends AbstractRecord {

    public Action(final String text, final int lineNumber) {
        super(lineNumber, text);

    }

    @Override
    public RecordType getRecordType() {
        return RecordType.ACTION;
    }
}
