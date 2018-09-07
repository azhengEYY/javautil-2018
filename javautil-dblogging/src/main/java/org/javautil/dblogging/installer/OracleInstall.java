package org.javautil.dblogging.installer;

import java.sql.Connection;

import org.javautil.sql.ApplicationPropertiesDataSource;
import org.javautil.sql.SqlRunner;
import org.javautil.sql.SqlSplitterException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OracleInstall {

    private final Connection connection;

    private boolean          drop    = false;
    private final Logger     logger  = LoggerFactory.getLogger(this.getClass());
    private boolean          showSql = false;

    private boolean          dryRun;

    private String           sqlOutputFilename;

    public OracleInstall(Connection conn, boolean drop, boolean showSql) {
        if (conn == null) {
            throw new IllegalArgumentException("conn is null");
        }
        connection = conn;
        this.drop = drop;
        this.showSql = showSql;
    }

    public OracleInstall(String outputFileName) {
        this.connection = null;
        this.showSql = true;
        this.dryRun = true;
        this.sqlOutputFilename = outputFileName;
    }

    public void process() throws Exception, SqlSplitterException {
        if (drop) {
            logger.info("dropping tables");
            new SqlRunner(this, "ddl/oracle/dblogger_uninstall.sr.sql").setConnection(connection)
                    .setPrintSql(showSql).setContinueOnError(true).setShowError(false).process();
        }

        logger.info("creating tables");
        final String createTablesResource = "ddl/oracle/dblogger_install_tables.sr.sql";

        new SqlRunner(this, createTablesResource).setConnection(connection).setContinueOnError(true).process();

        logger.info("creating logger_message_formatter");
        new SqlRunner(this, "ddl/oracle/logger_message_formatter.plsql.sr.sql").setConnection(connection)
                .setPrintSql(showSql).setProceduresOnly(true).setContinueOnError(true).process();
        logger.info("about to compile specs");
        new SqlRunner(this, "ddl/oracle/dblogger_install.pks.sr.sql").setConnection(connection)
                .setPrintSql(showSql).setProceduresOnly(true).setContinueOnError(true).process();
        logger.info("creating logger package body");
        new SqlRunner(this, "ddl/oracle/dblogger_install.pkb.sr.sql").setConnection(connection)
                .setPrintSql(showSql).setProceduresOnly(true).setContinueOnError(true).process();
        new SqlRunner(this, "cursor_stat.sql").setConnection(connection)
        .setPrintSql(showSql).setProceduresOnly(true).setContinueOnError(true).process();
        // final String plSqlErrors = OracleConnectionHelper.getPLSQLErrors(connection);
        // if (plSqlErrors != null) {
        // logger.error("\n" + plSqlErrors);
        // }

    }

    public OracleInstall setDrop(boolean drop) {
        this.drop = drop;
        return this;
    }

    public static void main(String[] args) throws Exception, SqlSplitterException {
        final ApplicationPropertiesDataSource apds = new ApplicationPropertiesDataSource();
        final Connection conn = apds.getDataSource().getConnection();
        new OracleInstall(conn, true, true).process();

    }

}
