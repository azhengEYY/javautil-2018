package org.javautil.oracle.trace;

import org.javautil.dblogging.OracleInstall;
import org.javautil.oracle.trace.formatter.OracleTraceReport;
import org.javautil.sql.SqlSplitterException;

public class CLI {

    public static void main(String[] args) throws SqlSplitterException, Exception {
        if ("traceformatter".equals(args[0])) {
            OracleTraceReport.main(args);
        }
        if ("generateOracleScripts".equals(args[0])) {
            OracleInstall.main(args);
        }
    }

}
