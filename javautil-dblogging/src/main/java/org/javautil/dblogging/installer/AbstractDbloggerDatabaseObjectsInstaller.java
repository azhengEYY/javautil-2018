package org.javautil.dblogging.installer;

public class AbstractDbloggerDatabaseObjectsInstaller {

    protected boolean drop    = false;
    protected boolean showSql = false;
    protected boolean noFail  = false;
    protected boolean dryRun  = false;

    public AbstractDbloggerDatabaseObjectsInstaller() {
        super();
    }

    public boolean isNoFail() {
        return noFail;
    }

    public boolean isShowSql() {
        return showSql;
    }

}