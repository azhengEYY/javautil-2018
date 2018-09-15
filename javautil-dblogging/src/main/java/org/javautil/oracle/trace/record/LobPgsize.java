package org.javautil.oracle.trace.record;

public class LobPgsize extends Lob {
    
    public LobPgsize(final String record, final int lineNumber) {
        super(lineNumber, record);
    }

    @Override
    public RecordType getRecordType() {
        return RecordType.LOBPGSIZE;
    }


}
