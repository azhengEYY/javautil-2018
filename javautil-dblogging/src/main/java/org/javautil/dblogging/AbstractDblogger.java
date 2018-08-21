package org.javautil.dblogging;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.javautil.io.FileUtil;
import org.javautil.oracle.trace.CursorsStats;
import org.javautil.oracle.trace.OracleTraceProcessor;
import org.javautil.sql.Binds;
import org.javautil.sql.NamedSqlStatements;
import org.javautil.sql.SequenceHelper;
import org.javautil.sql.SqlSplitterException;
import org.javautil.sql.SqlStatement;
import org.javautil.util.ListOfNameValue;
import org.javautil.util.NameValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractDblogger implements Dblogger {

    protected final Connection           connection;

    protected NamedSqlStatements statements;

    Logger                       logger            = LoggerFactory.getLogger(getClass());
    private long                 jobStartMilliseconds;

    private String               moduleName;

    private String               actionName;

    private SequenceHelper       sequenceHelper;

    private long                 utProcessStatusId = -1;

    long                         utProcessStepId;

    public AbstractDblogger(Connection connection) throws SQLException, SqlSplitterException, IOException {
        super();
        if (connection == null) {
            throw new IllegalArgumentException("null connection");
        }
        this.connection = connection;
        sequenceHelper = new SequenceHelper(connection);
        statements = NamedSqlStatements.getNameSqlStatementsFromSqlSplitterResource(this, "ddl/h2/dblogger_dml.ss.sql");
    }

    @Override
    public long beginJob(final String processName, String className, String moduleName, String statusMsg,
            String threadName, String tracefileName)
            throws SQLException {

        setUtProcessStatusId(sequenceHelper.getSequence("ut_process_status_id_seq"));
        SqlStatement ss = statements.getSqlStatement("ut_process_status_insert");
        ss.setConnection(connection);
        Binds binds = new Binds();
        binds.put("ut_process_status_id", utProcessStatusId);
        binds.put("process_name", processName);
        binds.put("classname", className);
        binds.put("module_name", moduleName);
        binds.put("status_msg", statusMsg);
        binds.put("thread_name", threadName);
        binds.put("schema_name", null);
        binds.put("tracefile_name", tracefileName);
        binds.put("status_ts", new java.sql.Timestamp(System.currentTimeMillis()));
        System.out.println("about to startJob with " + binds.toString());
        ss.executeUpdate(binds);
        connection.commit();
        logger.warn("started job {} ", utProcessStatusId);
        return (int) utProcessStatusId;
    }

    protected void setUtProcessStatusId(long id) {
        logger.info("setUtProcessStatusId: {} after {}", utProcessStatusId, id);
        this.utProcessStatusId = id;

    }

    @Override
    public long insertStep(String stepName, String stepInfo, String className) {
        long retval = -1;
        try {
            if (sequenceHelper == null) {
                throw new IllegalStateException("sequencehelper is null");
            }
            this.utProcessStepId = sequenceHelper.getSequence("ut_process_step_id_seq");
            Binds binds = new Binds();
            binds.put("ut_process_step_id", utProcessStepId);
            binds.put("ut_process_status_id", utProcessStatusId);
            binds.put("step_name", stepName);
            binds.put("step_info", stepInfo);
            binds.put("classname", className);
            binds.put("start_ts", new java.sql.Timestamp(System.currentTimeMillis()));
            if (statements == null) {
                throw new IllegalStateException("statements is null");
            }
            SqlStatement ss = statements.get("ut_process_step_insert");
            ss.setConnection(connection);
            ss.executeUpdate(binds);
            connection.commit();
            logger.info("insertStep {} with binds {} " + stepName, binds);
            retval = utProcessStepId;
        } catch (SQLException sqe) {
            sqe.printStackTrace();
            logger.error(sqe.getMessage());
        }
        return retval;
    }

    @Override
    public void finishStep() throws SQLException {
        Binds binds = new Binds();
        binds.put("ut_process_step_id", utProcessStepId);
        binds.put("end_ts", new java.sql.Timestamp(System.currentTimeMillis()));
        SqlStatement ss = statements.get("end_step");
        ss.setConnection(connection);
        int rowCount = ss.executeUpdate(binds);
        if (rowCount != 1) {
            // should have option to abort
            logger.error(String.format("finishStep stepId %d updated %d rows", utProcessStepId, rowCount));
            // now what? rollback TODO
        }

        connection.commit();
    }

    private void finishJob(SqlStatement ss) throws SQLException {
        logger.warn("finishing {} ", utProcessStatusId);
        ss.setConnection(connection);
        Binds binds = new Binds();
        binds.put("ut_process_status_id", utProcessStatusId);
        binds.put("end_ts", new java.sql.Timestamp(System.currentTimeMillis()));
        int rowcount = ss.executeUpdate(binds);
        // connection.commit(); // TODO is this a hack?
        if (rowcount != 1) {
            logger.warn("ut_process_status not updated for {}", utProcessStatusId);
        } else {
            logger.warn("finishJob: {}", utProcessStatusId);
        }
        try {
            updateJob(utProcessStatusId);
        } catch (FileNotFoundException e) { // TODO need a separate Logger
            logger.error(e.getMessage());
        } catch (IOException e) {
            logger.error(e.getMessage());

        }
        connection.commit();
        System.out.println("job " + utProcessStatusId + " finished =====");
    }

    @Override
    public void abortJob(Exception e) throws SQLException {
        finishJob(statements.getSqlStatement("abort_job"));
    }

    @Override
    public void endJob() throws SQLException {
        finishJob(statements.getSqlStatement("end_job"));
    }

    protected ListOfNameValue getUtProcessStatus(long jobNbr) throws SQLException {
        String sql = "select * from ut_process_status order by ut_process_status_id";
        SqlStatement ss = new SqlStatement(connection, sql);
        return ss.getListOfNameValue(new Binds());

    }

    public void showUtProcessStep() throws SQLException {
        String sql = "select * from ut_process_step order by ut_process_step_id";
        SqlStatement ss = new SqlStatement(connection, sql);
        for (NameValue nv : ss.getListOfNameValue(new Binds())) {
            System.out.println(nv);
        }

    }

    public void updateJob(long jobId) throws SQLException, FileNotFoundException, IOException {
        String ups = "select tracefile_name from ut_process_status "
                + "where ut_process_status_id = :ut_process_status_id";

        String upd = "update ut_process_status "
                + "set tracefile_data =  ?, "
                + "    tracefile_json =  ? "
                + "where ut_process_status_id = ?";

        logger.warn("updating job {}", jobId);
        SqlStatement upsStatement = new SqlStatement(connection, ups);
        Binds binds = new Binds();
        binds.put("ut_process_status_id", jobId);
        NameValue upsRow = upsStatement.getNameValue(binds, true);
        //
        String traceFileName = upsRow.getString("tracefile_name");
        Clob clob = connection.createClob();
        String tracefileData = FileUtil.getAsString(traceFileName);
        clob.setString(1, tracefileData);
        //
        Clob jsonClob = connection.createClob();
        OracleTraceProcessor tfr = new OracleTraceProcessor(traceFileName);
        tfr.process();
        CursorsStats cursorStats = tfr.getCursors();
        String jsonString = cursorStats.toString();
        jsonClob.setString(1, jsonString);

        PreparedStatement updateTraceFile = connection.prepareStatement(upd);

        updateTraceFile.setClob(1, clob);
        updateTraceFile.setClob(2, jsonClob);
        updateTraceFile.setLong(3, jobId);
        int count = updateTraceFile.executeUpdate();

        binds.put("tracefile_data", clob);
        if (count != 1) {
            throw new IllegalArgumentException("unable to update ut_process_status_id " + jobId);
        }
        logger.warn("updated {}", jobId);
    }



    public Connection getConnection() {
        return connection;
    }

    @Override
    public void showConnectionInformation() {
        System.out.println(connection);

    }

    @Override
    public long getUtProcessStatusId() {
        return utProcessStatusId;
    }


}