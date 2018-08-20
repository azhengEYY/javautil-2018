package com.pacificdataservices.pdssr;

import java.io.FileNotFoundException;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.javautil.conditionidentification.ConditionIdentification;
import org.javautil.conditionidentification.ConditionIdentificationPersistence;
import org.javautil.dblogging.Dblogger;
import org.javautil.io.ResourceHelper;
import org.javautil.sql.Binds;
import org.javautil.sql.DataNotFoundException;
import org.javautil.sql.SqlSplitterException;
import org.javautil.sql.SqlStatement;
import org.javautil.util.ListOfNameValue;
import org.javautil.util.NameValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

public class InstrumentedLoadConditionIdentification {

    private final Logger              logger           = LoggerFactory.getLogger(this.getClass());

    private ConditionIdentification   condi;

    private Connection                connection;

    private List<Map<String, Object>> conditionRules;

    private static final String       ruleYamlFileName = "pdssr/CdsDataloadConditions.yaml";

    private Dblogger   dblogger;

    public InstrumentedLoadConditionIdentification(Connection conn, Dblogger dblogger)
            throws SQLException, IOException, SqlSplitterException {
        if (conn == null) {
            throw new IllegalArgumentException("connection is null");
        }
        if (dblogger == null) {
            throw new IllegalArgumentException("dblogger is null");
        }
        this.connection = conn;
        connection.setAutoCommit(false);
        loadRules();
        condi = new ConditionIdentification(conn, conditionRules, false, 5);
        condi.setDblogger(dblogger);
        this.dblogger = dblogger;
        dblogger.setModule("LoadConditionIdentification", "initialize");
        logger.warn("dblogger is  " + dblogger);

    }

    @SuppressWarnings("unchecked")
    private void loadRules() throws FileNotFoundException {
        Yaml yaml = new Yaml();
        InputStream ios = ResourceHelper.getResourceAsInputStream(this, ruleYamlFileName);
        conditionRules = (List<Map<String, Object>>) yaml.load(ios);
    }

    public void process(long etlFileId, int verbosity) throws SQLException, IOException, SqlSplitterException {
        logger.info("processing for etl_file_id " + etlFileId);
        Binds binds = new Binds();
        binds.put("ETL_FILE_ID", etlFileId);
        Long runId;
        try {
            runId = getUtConditionRunId(etlFileId);
            logger.info("deleting run " + runId);
            deleteRun(runId);
        } catch (DataNotFoundException e) {
            logger.info("no run to delete " + etlFileId);
        }
        condi.process(binds, verbosity);
    }

    public Long getUtConditionRunId(long etlFileId) throws SQLException {
        SqlStatement sel = new SqlStatement(connection, //
                "select /* getUtConditionRunId */ ut_condition_run_id \n " + //
                        "from ut_condition_run_parm \n" + //
                        "where parm_nm = 'ETL_FILE_ID' and parm_value_str = :ETL_FILE_ID");
        logger.info("getting for " + etlFileId);
        Binds binds = new Binds();
        binds.put("ETL_FILE_ID", etlFileId);

        Long retval = null;
        try {
            NameValue row = sel.getNameValue(binds, true);
            retval = row.getLong("ut_condition_run_id");
            logger.info("About to close cursor");
        } finally {
            sel.close();
        } // TODO find out why this is not closing or why I am getting duplicate cursors
        logger.info("closed cursor");
        return retval;

    }

    public void deleteRun(long utConditionRunId) throws SQLException, IOException, SqlSplitterException {
        Binds binds = new Binds();

        dblogger.setAction("deleteRun");
        ConditionIdentificationPersistence cip = new ConditionIdentificationPersistence(connection, 0);
        cip.purgeRun(utConditionRunId);
    }

    public ArrayList<Long> processAll(int verbosity) throws SQLException, IOException, SqlSplitterException {
        SqlStatement loads = new SqlStatement(connection, "select etl_file_id from etl_file order by etl_file_id");
        ListOfNameValue rows = loads.getListOfNameValue(new Binds(), true);

        int loadCount = 0;
        ArrayList<Long> retval = new ArrayList<Long>();
        for (NameValue binds : rows) {
            long etl_file_id = binds.getLong("etl_file_id");
            logger.info("processing " + etl_file_id);
            process(etl_file_id, verbosity);
            retval.add(etl_file_id);
            loadCount++;
        }
        logger.info("load count " + loadCount);
        return retval;
    }
}