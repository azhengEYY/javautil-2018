
package org.javautil.oracle.trace.record;

public class Exe extends AbstractRecord {

    public Exe(final String text, final int lineNumber) {
        super(lineNumber, text);
    }

    @Override
    public RecordType getRecordType() {
        return RecordType.EXE;
    }
}
