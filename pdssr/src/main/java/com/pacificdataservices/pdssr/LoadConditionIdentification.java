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
import org.javautil.dblogging.logger.Dblogger;
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

public class LoadConditionIdentification {

    private final Logger              logger           = LoggerFactory.getLogger(this.getClass());

    private ConditionIdentification   condi;

    private Connection                connection;

    private List<Map<String, Object>> conditionRules;

    private static final String       ruleYamlFileName = "pdssr/CdsDataloadConditions.yaml";
    // // TODO externalize
    // private SqlStatement deleteRow = new SqlStatement("deleteRow","delete from
    // ut_condition_row_msg "
    // + "where ut_condition_run_step_id in (" + "select ut_condition_run_step_id "
    // + "from ut_condition_run_step "
    // + "where ut_condition_run_id = :ut_condition_run_id)");
    //
    // private SqlStatement deleteStep = new SqlStatement(
    // "deleteStep","delete from ut_condition_run_step where ut_condition_run_id =
    // :ut_condition_run_id");
    //
    // private SqlStatement deleteParm = new SqlStatement("deleteParm",
    // "delete from ut_condition_run_parm where ut_condition_run_id =
    // :ut_condition_run_id");
    //
    // private SqlStatement deleteRun = new SqlStatement("deleteRun",
    // "delete from ut_condition_run where ut_condition_run_id =
    // :ut_condition_run_id");
    //
    //// private SqlStatement selectRow = new SqlStatement("selectRow",
    // "select ut_condition_run_step_id, count(*) row_count from
    // ut_condition_row_msg "
    // + "group by ut_condition_run_step_id " + "order by
    // ut_condition_run_step_id");
    //
    // private SqlStatement selectStep = new SqlStatement("selectStep",
    // "select ut_condition_run_id, count(*) row_count from ut_condition_run_step "
    // + "group by ut_condition_run_id " + "order by ut_condition_run_id");
    //
    // private SqlStatement selectParm = new SqlStatement(connection,
    // "select to_number(parm_value_str,'99999') etl_file_id, count(*) "
    // + "from ut_condition_run_parm where parm_nm = 'ETL_FILE_ID' "
    // + "group by to_number(parm_value_str,'99999') " + "order by
    // to_number(parm_value_str,'99999') ");
    //
    // private SqlStatement selectRun = new SqlStatement(connection,
    // "select ut_condition_run_id, count(*) from ut_condition_run " + " group by
    // ut_condition_run_id "
    // + "order by ut_condition_run_id");

    private Dblogger   dblogger;

    public LoadConditionIdentification(Connection conn, Dblogger dblogger)
            throws SQLException, IOException, SqlSplitterException {
        if (conn == null) {
            throw new IllegalArgumentException("connection is null");
        }
        this.connection = conn;
        connection.setAutoCommit(false);
        loadRules();
        condi = new ConditionIdentification(conn, conditionRules, false, 5);
        this.dblogger = dblogger;
        dblogger.setModule("LoadConditionIdentification", "initialize");

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
        binds.put("ut_condition_run_id", utConditionRunId);
        dblogger.setAction("deleteRun");
        ConditionIdentificationPersistence cip = new ConditionIdentificationPersistence(connection, 0);
        cip.purgeRun(utConditionRunId);
        // deleteRow.executeUpdate(connection, binds);
        // deleteStep.executeUpdate(connection, binds);
        // deleteParm.executeUpdate(connection, binds);
        // deleteRun.executeUpdate(connection, binds);
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