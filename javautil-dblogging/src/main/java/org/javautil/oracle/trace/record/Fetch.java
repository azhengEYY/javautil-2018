/**
 * @(#) Fetch.java
 */

package org.javautil.oracle.trace.record;

/**
*
*/
public class Fetch extends CursorOperation {
    public Fetch(final String record, final int lineNbr) {
        super(lineNbr, record);
    }

    @Override
    public RecordType getRecordType() {
        return RecordType.FETCH;
    }
}
