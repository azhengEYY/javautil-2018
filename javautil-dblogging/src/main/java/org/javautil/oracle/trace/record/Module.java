/**
 * @(#) Xctend.java
 */

package org.javautil.oracle.trace.record;

public class Module extends AbstractRecord {

    public Module(final String text, final int lineNumber) {
        super(lineNumber, text);

    }

    // TODO put a get record type in RecordType
    @Override
    public RecordType getRecordType() {
        return RecordType.MODULE;
    }
}
