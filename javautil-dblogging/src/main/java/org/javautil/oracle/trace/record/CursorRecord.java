package org.javautil.oracle.trace.record;

public interface CursorRecord extends Record {
    public Long getCursorNumber();

    @Override
    public int getLineNumber();

    @Override
    public RecordType getRecordType();

    public void setCursorNumber(Long cursorNumber);

    public String getSqlid();

    public void setSqlid(String sqlId);

    public String getCursorId();
}
