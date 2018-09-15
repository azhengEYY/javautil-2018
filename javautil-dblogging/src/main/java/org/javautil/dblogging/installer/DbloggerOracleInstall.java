package org.javautil.dblogging.installer;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.javautil.dblogging.DbloggerPropertiesDataSource;
import org.javautil.sql.Binds;
import org.javautil.sql.SqlRunner;
import org.javautil.sql.SqlSplitterException;
import org.javautil.sql.SqlStatement;
import org.javautil.util.ListOfLists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DbloggerOracleInstall {

    private final Connection connection;

    private boolean          drop    = true;
    private final Logger     logger  = LoggerFactory.getLogger(this.getClass());
    private boolean          showSql = false;

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
        logger.info("process begins===================================");
        if (drop) {
            drop();
        }
        logger.info("existing tables ==================");
        SqlStatement ss = new SqlStatement("select table_name from user_tables order by table_name");
        ss.setConnection(connection);
        ListOfLists lols = ss.getListOfLists(new Binds());
        StringBuilder sb = new StringBuilder();
        for (List<Object> tablename : lols) {
            sb.append(tablename.get(0));
            sb.append("\n");
        }
        
        logger.info("tables: {}",sb.toString());
        ss.close();

        logger.info("creating tables showSql ");
        loggerObjectInstall();
      
    }

    public void drop() throws SqlSplitterException, SQLException, IOException {
        logger.info("dropping tables");
        new SqlRunner(this, "ddl/oracle/dblogger_uninstall.sr.sql").setConnection(connection)
                .setPrintSql(showSql).setContinueOnError(true).setShowError(false).process();
    }

    public void loggerObjectInstall() throws SqlSplitterException, SQLException, IOException {
        logger.info("loggerObjectInstall showSql: {}",showSql);
        final String createTablesResource = "ddl/oracle/dblogger_install_tables.sr.sql";
        new SqlRunner(this, createTablesResource).setConnection(connection).
        setContinueOnError(true).setPrintSql(showSql).process();

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

//    public void installCursorTables() throws SqlSplitterException, SQLException, IOException {
//        logger.info("======== creating logger cursor_stat.sqls");
//
//        new SqlRunner(this, "cursor_stat.sql").setConnection(connection).setTrace(true)
//                .setPrintSql(true).setContinueOnError(true).setShowError(true).process();
//    }

    public DbloggerOracleInstall setDrop(boolean drop) {
        this.drop = drop;
        return this;
    }

    public static void main(String[] args) throws Exception, SqlSplitterException {
        final DbloggerPropertiesDataSource apds = new DbloggerPropertiesDataSource("logger_and_application.properties");
        final Connection conn = apds.getDataSource().getConnection();
        new DbloggerOracleInstall(conn, true, true).process();

    }

}
