package org.javautil.oracle.trace.record;

public interface CursorRecord extends Record {

    public long getCursorNumber();

    @Override
    public int getLineNumber();

    @Override
    public RecordType getRecordType();


    public String getSqlid();

    public void setSqlid(String sqlId);

  //  public String getCursorId();
}
