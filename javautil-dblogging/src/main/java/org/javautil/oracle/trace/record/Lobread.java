package org.javautil.oracle.trace.record;

public class Lobread extends Lob implements Record{
    
    public Lobread(final String record, final int lineNumber) {
        super(lineNumber, record);
    }

    @Override
    public RecordType getRecordType() {
        return RecordType.LOBREAD;
    }


}
