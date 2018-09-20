package com.pacificdataservices.pdssr;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import org.javautil.dblogging.logger.Dblogger;
import org.javautil.io.ResourceHelper;
import org.javautil.sql.Binds;
import org.javautil.sql.Dialect;
import org.javautil.sql.NamedSqlStatements;
import org.javautil.sql.SequenceHelper;
import org.javautil.sql.SqlStatement;
import org.javautil.util.NameValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CdsBatchDataLoader implements FilenameFilter {

    private static final Logger       logger        = LoggerFactory.getLogger(CdsBatchDataLoader.class);
    /**
     * Loads a CDS format file into ETL tables
     *
     */
    private Connection                connection    = null;

    private InputStream               etlPersistenceStream;

    NamedSqlStatements                sqlStatements;

    private final Map<String, String> sqlNameByType = new HashMap<>();

    private final SequenceHelper      sequenceHelper;
    Dblogger           dblogger;

    // TODO pretend we need the sales pk
    public CdsBatchDataLoader(Connection conn, Dblogger dblogger) throws SQLException, IOException {
        if (conn == null) {
            logger.error("conn is null");
            throw new IllegalArgumentException("conn is null");
        }
        switch (Dialect.getDialect(conn)) {
        case ORACLE:
            etlPersistenceStream = ResourceHelper.getResourceAsInputStream(this,
                    "pdssr/oracle/etl_persistence_colon.yaml");
            break;
        case H2:
            etlPersistenceStream = ResourceHelper.getResourceAsInputStream(this, "pdssr/h2/etl_persistence_colon.yaml");
            break;
        default:
            throw new UnsupportedOperationException("unsupported dialect");
        }

        this.connection = conn;
        // CreateSchema.getInfo(connection);
        connection.setAutoCommit(false);
        sqlNameByType.put("CD", "etl_customer_insert");
        sqlNameByType.put("CT", "etl_customer_tot_insert");
        sqlNameByType.put("IR", "etl_inventory_insert");
        sqlNameByType.put("IT", "etl_inventory_tot_insert");
        sqlNameByType.put("SA", "etl_sale_insert");
        sqlNameByType.put("AT", "etl_sale_tot_insert");
        sqlStatements = new NamedSqlStatements(etlPersistenceStream, conn);
        sequenceHelper = new SequenceHelper(conn);
        this.dblogger = dblogger;
    }

    /**
     *
     * @param dataStream
     * @param conn
     * @param distributor_cd
     * @param validate
     *
     * @throws ParseException
     * @throws IOException
     * @throws SQLException
     */
    public long process(InputStream dataStream, String dataStreamDescr, String distributorCd, boolean validate)
            throws ParseException, IOException, SQLException {
    //    logger.info("CDSDataLoader:process job_log_id {}",dblogger.getUtProcessStatusId());
        dblogger.insertStep("CdsDataLoader", dataStreamDescr, getClass().getName());
        dblogger.setModule("CdsDataLoader", null);
        dblogger.setAction("initial insert");
        long etlFileId;
        try {
            etlFileId = loadFile(dataStream, dataStreamDescr, distributorCd, validate);
            dblogger.finishStep();
            connection.commit();

        } catch (SQLException | IOException | ParseException e) {

            throw e;
        }
        return etlFileId;
    }

    long loadFile(InputStream dataStream , String datastreamDescr, String distributorCd, boolean validate)
            throws SQLException, ParseException, IOException {
        Binds binds = new Binds();
        binds.put("ORG_CD", distributorCd);

        final CdsFileReader reader = new CdsFileReader(dataStream);
        final Long fileId = initialInsert(binds);
        binds.put("ETL_FILE_ID", fileId);
        logger.info("Processing================== " + datastreamDescr);
        logger.info("fileId " + fileId);
        // TODO ETL_FILE should exist

        dblogger.setAction("Load ETL data");
        while ((binds = reader.readLine()) != null) {

            binds.put("ETL_FILE_ID", fileId);
            binds.put("LINE_NUMBER", reader.getLineNumber());
            if (binds.containsKey("EXTENDED_NET_AMT")) {
                final Integer extNetAmt = (Integer) binds.get("EXTENDED_NET_AMT");
                if (extNetAmt != null) {
                    binds.put("EXTENDED_NET_AMT", extNetAmt / 100);
                }
            }
            final String sqlName = sqlNameByType.get(reader.getRecordType());
            final SqlStatement sh = sqlStatements.getSqlStatement(sqlName);
            // if ("CD".equals(reader.getRecordType())) {
            // TreeMap info = new TreeMap();
            // for (Entry<String, Object> e : binds.entrySet()) {
            // info.put(e.getKey(), e.getValue());
            // }
            // String hash = TreeHash.hash(info);
            // binds.put("INFO_HASH", hash);
            // }
            sh.addBatch(binds);
        }
        reader.getLineNumber();
        for (SqlStatement ss : sqlStatements) {
            ss.executeBatch();
        }

        reader.close();
        dataStream.close();
        return fileId;
    }

    /**
     *
     * @param binds
     * @return
     * @throws SQLException
     */
    Long initialInsert(Binds binds) throws SQLException {
        SqlStatement sh = sqlStatements.getSqlStatement("org_sql").useColonBind();
        sh.setConnection(connection);
        final NameValue row = sh.getNameValue(binds, false);
        // logger.info(String.format("reporting org: %s", row));

        sh = sqlStatements.getSqlStatement("etl_file_initial_insert");
        sh.setConnection(connection);
        // logger.info(sh.toString());
        final Binds fileBinds = new Binds();
        // fileBinds.put("ORG_CD", row.get("ORG_CD"));
        fileBinds.put("ORG_ID", row.get("ORG_ID"));
        fileBinds.put("RPT_ORG_ID", row.get("ORG_ID"));
        final long etlFileId = sequenceHelper.getSequence("etl_file_id_seq");
        fileBinds.put("ETL_FILE_ID", etlFileId);
        // final SqlStatement orgss = new SqlStatement(connection,"select * from org");
        // final ListOfNameValue onv = orgss.getListOfNameValue(fileBinds,true);
        // logger.info("onv: " + onv);
        sh.executeUpdate(fileBinds);

//        final SqlStatement etlf = new SqlStatement(connection,
//                "select * from etl_file where etl_file_id = :etl_file_id");
//
//        binds.put("ETL_FILE_ID", etlFileId);
//        //final NameValue etlfnv = etlf.getNameValue(binds, true);
//        // logger.info("etlFileId: {}", etlfnv.toString());
//        // TODO check expected values
//        etlf.close();

        return etlFileId;

    }

    @Override
    public boolean accept(File dir, String fileName) {
        return fileName.endsWith(".cds");
    }
}
