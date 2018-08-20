package org.javautil.oracle.trace.record;

public interface Record {

    public int getLineNumber();

    public String getText();

    public RecordType getRecordType();

    public String getLineAndText();

}
