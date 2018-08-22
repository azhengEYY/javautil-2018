package org.javautil.sql;

public class SqlSplitterException extends RuntimeException {
    private SqlSplitterLine line;

    private String          message;

    SqlSplitterException(SqlSplitterLine line, String message) {
        this.line = line;
        this.message = message;
    }

    public SqlSplitterLine getLine() {
        return line;
    }

    public String getMessage() {
        return message;
    }

}
