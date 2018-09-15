package org.javautil.oracle.trace.record;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Xctend extends AbstractRecord {
    
    //XCTEND rlbk=0, rd_only=1, tim=12205299536
    public static final String exctendRegex = "XCTEND rlbk=(\\d*), rd_only=(\\d*), tim=(\\d*)";



    protected static final Pattern recordPattern  = Pattern.compile(exctendRegex);
    private int rollback;
    private int readOnly;
    private long time;

    public Xctend(final String text, final int lineNumber) {
        super(lineNumber, text);
        final Matcher m = recordPattern.matcher(text);
        if (m.find()) {
            rollback = Integer.parseInt(m.group(1));
            readOnly= Integer.parseInt(m.group(2));
            time = Long.parseLong(m.group(3));
        } else {
            throw new IllegalStateException("failed to parse '" + text + "'");
        }
    }


    @Override
    public RecordType getRecordType() {
        return RecordType.XCTEND;
    }


    public int getRollback() {
        return rollback;
    }


    public int getReadOnly() {
        return readOnly;
    }


    public long getTime() {
        return time;
    }


}
