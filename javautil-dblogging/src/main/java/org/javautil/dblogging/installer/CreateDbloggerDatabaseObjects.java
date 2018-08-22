package org.javautil.dblogging.installer;

import java.sql.SQLException;

import org.javautil.sql.SqlSplitterException;

public interface CreateDbloggerDatabaseObjects {

    void process() throws Exception, SqlSplitterException;

    CreateDbloggerDatabaseObjects setDrop(boolean drop);

    CreateDbloggerDatabaseObjects setNoFail(boolean noFail);

    CreateDbloggerDatabaseObjects setShowSql(boolean showSql);

    CreateDbloggerDatabaseObjects setDryRun(boolean showSql);

    boolean isDrop();

    void dropObjects() throws SQLException;

}