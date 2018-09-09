package org.javautil.dblogging.installer;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.javautil.sql.Dialect;
import org.javautil.sql.SqlRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class H2Install extends AbstractDbloggerDatabaseObjectsInstaller implements CreateDbloggerDatabaseObjects {

    private final Logger     logger = LoggerFactory.getLogger(this.getClass());

    private final Connection connection;

    public H2Install(Connection conn) throws SQLException {

        if (conn == null) {
            throw new IllegalArgumentException("conn is null");
        }
        if (Dialect.getDialect(conn).equals(Dialect.H2)) {
            connection = conn;
        } else {
            throw new IllegalArgumentException("not an H2 connection");
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.javautil.dblogging.CreateDbloggerDatabaseObjects#process()
     */
    @Override
    public void process() throws Exception {
        if (isDrop()) {
            dropObjects();
        }
        logger.info("creating logger tables for h2");
        final String createTablesResource = "ddl/h2/dblogger_install_tables.sr.sql";
        logger.info("about to call SqlRunner showSql {}", showSql);

        new SqlRunner(this, createTablesResource).setConnection(connection).setPrintSql(showSql)
                .setContinueOnError(noFail).process();
//        new SqlRunner(this, "cursor_stat.sql").setConnection(connection)
//        .setPrintSql(showSql).setProceduresOnly(true).setContinueOnError(true).process();
        logger.info("SqlRunner complete");
        connection.commit();
        // connection.close();
        System.out.println("H2Install:process logger tables installed");
    }

    @Override
    public void dropObjects() throws SQLException {

        final Statement dropAll = connection.createStatement();
        System.out.println("drop everything");
        logger.info("drop everything");
        dropAll.execute("DROP ALL OBJECTS");
        System.out.println("All objects  dropped not datafile!!!!");

    }

    @Override
    public H2Install setDrop(boolean drop) {
        this.drop = drop;
        return this;
    }

    @Override
    public H2Install setNoFail(boolean noFail) {
        this.noFail = noFail;
        return this;
    }

    @Override
    public H2Install setShowSql(boolean showSql) {
        this.showSql = showSql;
        return this;
    }

    @Override
    public boolean isDrop() {
        return drop;
    }

    @Override
    public H2Install setDryRun(boolean dryRun) {
        this.dryRun = dryRun;
        return this;
    }

}
