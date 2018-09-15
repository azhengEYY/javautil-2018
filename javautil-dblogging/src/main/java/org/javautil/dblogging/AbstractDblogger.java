package org.javautil.dblogging;

import java.io.IOException;
import java.io.Reader;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.javautil.io.FileUtil;
import org.javautil.lang.ThreadUtil;
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

    protected final Connection   connection;

    protected NamedSqlStatements statements;

    Logger                       logger            = LoggerFactory.getLogger(getClass());
    private long                 jobStartMilliseconds;

    private String               moduleName;

    private String               actionName;

    private SequenceHelper       sequenceHelper;

    private long                 utProcessStatusId = -1;

    long                         utProcessStepId;

    private boolean persistTrace;

    private boolean persistPlans;

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

        setUtProcessStatusId(sequenceHelper.getSequence("job_log_id_seq"));
        SqlStatement ss = statements.getSqlStatement("job_log_insert");
        ss.setConnection(connection);
        Binds binds = new Binds();
        binds.put("job_log_id", utProcessStatusId);
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

        // String appTracefileName = getTraceFileName();
        // logger.debug("*************updating tracefile name to {}", appTracefileName);
        // updateTraceFileName(appTracefileName);
        return (int) utProcessStatusId;
    }

    protected void setUtProcessStatusId(long id) {
        logger.info("setUtProcessStatusId: {} after {}", utProcessStatusId, id);
        this.utProcessStatusId = id;

    }
    

    public long insertStep(String stepName, String stepInfo, String className) throws SQLException {
        String stackTrace  = ThreadUtil.getStackTrace(2);
        return insertStep(stepName,stepInfo,className,stackTrace);
    }

    @Override
    public long insertStep(String stepName, String stepInfo, String className, String stacktrace) {
        long retval = -1;
        try {
            if (sequenceHelper == null) {
                throw new IllegalStateException("sequencehelper is null");
            }
            this.utProcessStepId = sequenceHelper.getSequence("job_step_id_seq");
            Binds binds = new Binds();
            binds.put("job_step_id", utProcessStepId);
            binds.put("job_log_id", utProcessStatusId);
            binds.put("step_name", stepName);
            binds.put("step_info", stepInfo);
            binds.put("classname", className);
            binds.put("start_ts", new java.sql.Timestamp(System.currentTimeMillis()));
            binds.put("stacktrace", stacktrace);
            if (statements == null) {
                throw new IllegalStateException("statements is null");
            }
            SqlStatement ss = statements.get("job_step_insert");
            ss.setConnection(connection);
            ss.executeUpdate(binds);
            connection.commit();
            logger.debug("insertStep {} with binds {} " + stepName, binds);
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
        binds.put("job_step_id", utProcessStepId);
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
        binds.put("job_log_id", utProcessStatusId);
        binds.put("end_ts", new java.sql.Timestamp(System.currentTimeMillis()));
        int rowcount = ss.executeUpdate(binds);
        // connection.commit(); // TODO is this a hack?
        if (rowcount != 1) {
            logger.warn("job_log not updated for {}", utProcessStatusId);
        } else {
            logger.warn("finishJob: {}", utProcessStatusId);
        }
        
            updateJob(utProcessStatusId);
        
        connection.commit();
        logger.info("job " + utProcessStatusId + " finished =====");
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
        String sql = "select * from job_log order by job_log_id";
        SqlStatement ss = new SqlStatement(connection, sql);
        return ss.getListOfNameValue(new Binds());

    }

    public void showUtProcessStep() throws SQLException {
        String sql = "select * from job_step order by job_step_id";
        SqlStatement ss = new SqlStatement(connection, sql);
        for (NameValue nv : ss.getListOfNameValue(new Binds())) {
            System.out.println(nv);
        }

    }

    public void updateJob(long jobId) throws SQLException {
        String ups = "select tracefile_name from job_log "
                + "where job_log_id = :job_log_id";

        String upd = "update job_log "
                + "set tracefile_data =  ?, "
                + "    tracefile_json =  ? "
                + "where job_log_id = ?";

        logger.warn("updating job {}", jobId);
        SqlStatement upsStatement = new SqlStatement(connection, ups);
        Binds binds = new Binds();
        binds.put("job_log_id", jobId);
        NameValue upsRow = upsStatement.getNameValue(binds, true);
        logger.warn("upsRow {}", upsRow);

        //
        String traceFileName = upsRow.getString("tracefile_name");
        if (traceFileName == null) {
            throw new IllegalStateException("traceFileName is null");
        }
        Clob clob = connection.createClob();
        String tracefileData = null;
        try {
            tracefileData = FileUtil.getAsString(traceFileName);
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
        clob.setString(1, tracefileData);
        //
        Clob jsonClob = connection.createClob();
        OracleTraceProcessor tfr;
        try {
            tfr = new OracleTraceProcessor(traceFileName);
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
                throw new IllegalArgumentException("unable to update job_log_id " + jobId);
            }
            logger.warn("updated {}", jobId);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            logger.error(e.getMessage());
        }
       
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

    public void updateTraceFileName(String appTracefileName) throws SQLException {
        logger.info("*** updating trace to {}", appTracefileName);
        SqlStatement ss = new SqlStatement(connection,
                "update job_log set tracefile_name = :tracefile_name "
                        + "where job_log_id = :job_log_id");
        Binds binds = new Binds();
        binds.put("tracefile_name", appTracefileName);
        binds.put("job_log_id", utProcessStatusId);
        int rowCount = ss.executeUpdate(binds);
        connection.commit();
        if (rowCount != 1) {
            logger.error("Unable to update job_log for {}", utProcessStatusId);
        } else {
            logger.info("updated job_log {}", appTracefileName);
        }

    }
    
    public void persistenceUpdateTrace(long jobId, Clob traceClob) throws SQLException {
        if (traceClob == null) {
            throw new IllegalArgumentException("null traceClob");
        }
        Clob clob = connection.createClob();
        String upd = "update job_log "
                + "set tracefile_data =  ? "
                + "where job_log_id = ?";
       
    
        Reader traceReader = traceClob.getCharacterStream();
        

     
        
        PreparedStatement updateTraceFile = connection.prepareStatement(upd);

        updateTraceFile.setCharacterStream(1, traceReader);
    //    updateTraceFile.setClob(1, clob);
        updateTraceFile.setLong(2, jobId);
      
        int rowcount = updateTraceFile.executeUpdate();

        if (rowcount != 1) {
            throw new IllegalArgumentException("unable to update job_log_id " + jobId);
        }
    
    }
    
   

    public Clob createClob() throws SQLException {
        // TODO Auto-generated method stub
        return connection.createClob();
    }



    @Override
    public String openFile(String fileName) throws SQLException {
       throw new UnsupportedOperationException();
    }

    @Override
    public void setPersistTraceOnJobCompletion(boolean persistTrace) {
      this.persistTrace = persistTrace;
        
    }

    @Override
    public void setPersistPlansOnJobCompletion(boolean persistPlans) {
        this.persistPlans = persistPlans;
        
    }

}