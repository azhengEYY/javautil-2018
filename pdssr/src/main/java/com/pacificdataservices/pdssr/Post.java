package com.pacificdataservices.pdssr;

import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;

import org.javautil.io.ResourceHelper;
import org.javautil.sql.Binds;
import org.javautil.sql.Dialect;
import org.javautil.sql.NamedSqlStatements;
import org.javautil.sql.SqlRunner;
import org.javautil.sql.SqlStatement;
import org.javautil.sql.SqlStatements;
import org.javautil.util.NameValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Populates post_sale record from qualifying etl_sale records // // Updates the
 * etl_sale.product_id based on case_gtin // // Upserts product_nomen with
 * distributor identifier for authoritative manufacturer information // """
 *
 * @author jjs
 *
 */
public class Post {
    private final Logger     logger = LoggerFactory.getLogger(this.getClass());
    private final Connection connection;

    private SqlStatements    sqlStatements;
    private SqlStatements    postQueries;

    public Post(Connection conn, int verbosity) throws SQLException, FileNotFoundException {
        this.connection = conn;

        loadDml();
    }

    Date getEffectiveDate(long etlFileId) throws SQLException, InvalidLoadFileException {

        final Date retval = null;
        // String sql = "select file_create_dt from etl_sale_tot "
        // + "where etl_file_id = :ETL_FILE_ID";
        final String sql = " select max(ship_dt) from etl_sale " + "where etl_file_id = :ETL_FILE_ID";
        final SqlStatement ss = new SqlStatement(connection, sql);
        final Binds binds = new Binds();
        logger.info("getting sale date from " + etlFileId);
        binds.put("ETL_FILE_ID", etlFileId);
        final NameValue row = ss.getNameValue(binds, true);

        row.getDate("file_create_dt");

        return retval;
    }

    public void process(long etlFileId) throws SQLException, InvalidLoadFileException {
        logger.info("processing etl_file_id " + etlFileId);
        final Binds binds = new Binds();
        final Date effectiveDate = getEffectiveDate(etlFileId);
        binds.put("EFF_DT", effectiveDate);
        binds.put("ETL_FILE_ID", etlFileId);

        final SqlRunner runner = new SqlRunner(connection, sqlStatements);
        runner.process(binds);
        connection.commit();
    }

    private void loadDml() throws SQLException, FileNotFoundException {
        String path = null;
        String qpath = null;
        switch (Dialect.getDialect(connection)) {
        case ORACLE:
            path = "pdssr/oracle/post_dml.yaml";
            qpath = "pdssr/oracle/etl_posting_queries.yaml";
            break;
        case H2:
            path = "pdssr/h2/post_dml.yaml";
            qpath = "pdssr/h2/etl_posting_queries.yaml";
            break;
        default:
            throw new UnsupportedOperationException();
        }

        sqlStatements = new SqlStatements(ResourceHelper.getResourceAsInputStream(this, path), connection);
        postQueries = new NamedSqlStatements(ResourceHelper.getResourceAsInputStream(this, qpath), connection);
    }

    // SqlStatements getSqlStatements(String path, boolean isMap) {
    // if (path == null) {
    // throw new IllegalArgumentException("path is null");
    // }
    // SqlStatements retval = null;
    //
    // File file = ResourceHelper.getResourceAsFile(this, path);
    // logger.info(file.getAbsolutePath());
    // try {
    // retval = new SqlStatements(file.getAbsolutePath(), connection, isMap);
    // } catch (FileNotFoundException e) {
    // throw new RuntimeException(e);
    // }
    // return retval;
    // }

}
