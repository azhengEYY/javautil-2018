package org.javautil.dblogging.installer;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import org.javautil.sql.ApplicationPropertiesDataSource;
import org.javautil.sql.SqlRunner;
import org.javautil.sql.SqlSplitterException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DbloggerOracleInstall {

    private final Connection connection;

    private boolean          drop    = true;
    private final Logger     logger  = LoggerFactory.getLogger(this.getClass());
    private boolean          showSql = true;

    private boolean          dryRun;

    private String           sqlOutputFilename;

    public DbloggerOracleInstall(Connection connection, boolean drop, boolean showSql) {
        this.connection = connection;
        this.drop = drop;
        this.showSql = showSql;
    }

    public DbloggerOracleInstall(String outputFileName) {
        this.connection = null;
        this.showSql = true;
        this.dryRun = true;
        this.sqlOutputFilename = outputFileName;
    }

    public void process() throws Exception, SqlSplitterException {
        if (drop) {
            drop();
        }

        logger.info("creating tables");
        loggerObjectInstall();
        installCursorTables();
        // final String plSqlErrors = OracleConnectionHelper.getPLSQLErrors(connection);
        // if (plSqlErrors != null) {
        // logger.error("\n" + plSqlErrors);
        // }

    }

    public void drop() throws SqlSplitterException, SQLException, IOException {
        logger.info("dropping tables");
        new SqlRunner(this, "ddl/oracle/dblogger_uninstall.sr.sql").setConnection(connection)
                .setPrintSql(showSql).setContinueOnError(true).setShowError(false).process();
        new SqlRunner(this, "cursor_stat_drop.sql").setConnection(connection).setTrace(true)
                .setPrintSql(true).setContinueOnError(true).setShowError(false).process();
    }

    public void loggerObjectInstall() throws SqlSplitterException, SQLException, IOException {
        final String createTablesResource = "ddl/oracle/dblogger_install_tables.sr.sql";
        new SqlRunner(this, createTablesResource).setConnection(connection).setContinueOnError(true).process();

        logger.info("======= creating logger_message_formatter");
        new SqlRunner(this, "ddl/oracle/logger_message_formatter.plsql.sr.sql").setConnection(connection)
                .setPrintSql(showSql).setProceduresOnly(true).setContinueOnError(true).process();
        logger.info("======= about to compile specs ddl/oracle/dblogger_install.pks.sr.sql");
        new SqlRunner(this, "ddl/oracle/dblogger_install.pks.sr.sql").setConnection(connection)
                .setPrintSql(showSql).setProceduresOnly(true).setContinueOnError(true).process();
        logger.info("======== creating logger package body ddl/oracle/dblogger_install.pkb.sr.sql");
        new SqlRunner(this, "ddl/oracle/dblogger_install.pkb.sr.sql").setConnection(connection)
                .setPrintSql(showSql).setProceduresOnly(true).setContinueOnError(true).process();
    }

    public void installCursorTables() throws SqlSplitterException, SQLException, IOException {
        logger.info("======== creating logger cursor_stat.sqls");

        new SqlRunner(this, "cursor_stat.sql").setConnection(connection).setTrace(true)
                .setPrintSql(true).setContinueOnError(true).setShowError(true).process();
    }

    public DbloggerOracleInstall setDrop(boolean drop) {
        this.drop = drop;
        return this;
    }

    public static void main(String[] args) throws Exception, SqlSplitterException {
        final ApplicationPropertiesDataSource apds = new ApplicationPropertiesDataSource();
        final Connection conn = apds.getDataSource().getConnection();
        new DbloggerOracleInstall(conn, true, true).process();

    }

}
