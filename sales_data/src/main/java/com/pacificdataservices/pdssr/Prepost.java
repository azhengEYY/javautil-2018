package com.pacificdataservices.pdssr;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;

import org.javautil.dblogging.logger.Dblogger;
import org.javautil.io.ResourceHelper;
import org.javautil.sql.Binds;
import org.javautil.sql.SqlRunner;
import org.javautil.sql.SqlStatement;
import org.javautil.sql.SqlStatements;
import org.javautil.util.NameValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Populates post_sale record from qualifying etl_sale records // // Updates the
 *
 * etl_sale.product_id based on case_gtin // // Upserts product_nomen with
 * distributor identifier for authoritative manufacturer information // """
 *
 * @author jjs TODO this was just copy paste of POST, clean up common
 */
public class Prepost {
    private final Logger            logger = LoggerFactory.getLogger(this.getClass());
    private final Connection        connection;

    private SqlStatements           prepostQueries;
    private Dblogger dblogger;

    public Prepost(Connection conn, int verbosity, Dblogger dblogger) throws IOException {
        this.connection = conn;
        this.dblogger = dblogger;
        loadDml();
    }

    Date getEffectiveDate(long etlFileId) throws SQLException, InvalidLoadFileException {

        Date retval = null;
        final String sql = "select file_create_dt from etl_sale_tot where etl_file_id = :ETL_FILE_ID";
        final SqlStatement ss = new SqlStatement(connection, sql);
        final Binds binds = new Binds();
        binds.put("ETL_FILE_ID", etlFileId);
        final NameValue row = ss.getNameValue(binds, true);
        retval = (Date) row.get("file_create_dt");
        ss.close();
        return retval;
    }

    public void process(long etlFileId) throws SQLException, InvalidLoadFileException {
        dblogger.insertStep("Posting", "etl_file_id = " + etlFileId, getClass().getCanonicalName());
        logger.info("preposting processing etl_file_id " + etlFileId);
        final Binds binds = new Binds();
        final Date effectiveDate = getEffectiveDate(etlFileId);
        binds.put("EFF_DT", effectiveDate);
        binds.put("ETL_FILE_ID", etlFileId);

        final SqlRunner runner = new SqlRunner(connection, prepostQueries);
        runner.process(binds);
        dblogger.finishStep();
        connection.commit();
    }

    // TODO every dialect is wired in could be externalized but that is just more
    // external coding
    // TODO could infer name
    // TODO
    private void loadDml() throws IOException {
        String path = null;
        path = "pdssr/oracle/prepost_dml.yaml";
        // switch (this.dialect) {
        // case H2:
        // path ="com/pacificdataservices/pdssr/prepost_dml.yaml"; // eliminated with
        // for record counts should use
        // // qpath = "com/pacificdataservices/pdssr/post_queries.h2.yaml";
        // break;
        // case POSTGRES:
        // path ="com/pacificdataservices/pdssr/prepost_dml.postgres.yaml"; //
        // eliminated with for record counts should use
        // break;
        // default:
        // throw new IllegalStateException("unsupported Dialect " + dialect);

        // }

        prepostQueries = getSqlStatements(path, false);
    }

    SqlStatements getSqlStatements(String path, boolean isMap) throws IOException {
        if (path == null) {
            throw new IllegalArgumentException("path is null");
        }
        SqlStatements retval = null;

        final File file = ResourceHelper.getResourceAsFile(this, path);
        logger.info(file.getAbsolutePath());
        try {
            retval = new SqlStatements(file.getAbsolutePath(), connection);
            // .getResourceAsInputFile(file), connection, isMap);
        } catch (final FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        return retval;
    }

}
