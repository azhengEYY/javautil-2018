package com.pacificdataservices.pdssr;

import java.sql.Connection;
import java.sql.Statement;

import org.javautil.dblogging.CreateDbloggerDatabaseObjects;
import org.javautil.dblogging.H2Install;
import org.javautil.dblogging.OracleInstall;
import org.javautil.sql.Dialect;
import org.javautil.sql.SqlRunner;
import org.javautil.sql.SqlSplitterException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreateSchema {

    private final static Logger logger     = LoggerFactory.getLogger(CreateSchema.class);
    /**
     * Loads a CDS format file into ETL tables
     *
     */
    private Connection          connection = null;
    private Dialect             dialect    = null;
    String                      salesReportingDdl;
    String                      conditionDdl;
    String                      dbloggerDdl;
    boolean                     drop       = true;
    private final boolean       showSql;

    public CreateSchema(Connection conn, boolean drop, boolean showSql) throws Exception {
        logger.warn("creating with dialect " + dialect);
        this.connection = conn;
        this.dialect = Dialect.getDialect(conn);
        this.drop = drop;
        this.showSql = showSql;

    }

    public void installH2() throws Exception, SqlSplitterException {
        logger.info("installH2");
        final Statement dropAll = connection.createStatement();
        dropAll.execute("DROP ALL OBJECTS DELETE FILES");

        final CreateDbloggerDatabaseObjects dbloggerInstall = new H2Install(connection).setDrop(true).setNoFail(true)
                .setShowSql(true);
        dbloggerInstall.process();
        logger.info("***create ut_condition_tables");
        // TODO create condition tables should be in javautil-core
        final SqlRunner condi = new SqlRunner(this, "pdssr/h2/ut_condition_tables.sr.sql").setConnection(connection)
                .setPrintSql(true);
        condi.process();
        logger.info("***create sales tables");
        new SqlRunner(this, "pdssr/h2/sales_reporting_ddl.sr.sql").setConnection(connection)
                .setPrintSql(showSql).process();

    }

    public void installOracle() throws Exception, SqlSplitterException {
        if (drop) {
            logger.info("dropping sa");
            new SqlRunner(this, "pdssr/oracle/drop_sa.sql").setConnection(connection).setContinueOnError(true)
                    .setPrintSql(true).process();

            logger.info("sa dropped");
        }
        logger.info("creating condition tables");
        new SqlRunner(this, "pdssr/oracle/ut_condition_tables.sr.sql").setConnection(connection).setPrintSql(showSql)
                .process();
        logger.info("creating sales tables");
        new SqlRunner(this, "pdssr/oracle/sales_reporting_ddl.sr.sql").setConnection(connection).setPrintSql(showSql)
                .process();
        logger.info("creating dblogger");
        final OracleInstall dbloggerInstall = new OracleInstall(connection, true, showSql);
        dbloggerInstall.process();

        logger.info("seeding database");
    }

    public void process() throws Exception, SqlSplitterException {
        switch (Dialect.getDialect(connection)) {

        case H2:
            installH2();
            break;
        case ORACLE:
            installOracle();
            break;
        default:
            throw new IllegalArgumentException("Unsupported Dialect");
        }
        final SeedSalesReportingDatabase seeder = new SeedSalesReportingDatabase(connection);
        seeder.process(true);
        logger.info("done");
    }
}
