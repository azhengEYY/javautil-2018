package org.javautil.dblogging.logger;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.SQLException;

import org.javautil.dblogging.traceagent.DbloggerApplication;
import org.javautil.dblogging.traceagent.DbloggerForOracleApplication;
import org.javautil.dblogging.tracepersistence.DbloggerPersistence;
import org.javautil.dblogging.tracepersistence.DbloggerPersistenceImpl;
import org.javautil.lang.ThreadUtil;
import org.javautil.oracle.OracleSessionInfo;
import org.javautil.sql.Binds;
import org.javautil.sql.Dialect;
import org.javautil.sql.SqlSplitterException;
import org.javautil.sql.SqlStatement;
import org.javautil.util.NameValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Persistence is performed in a different database
 * 
 * @author jjs
 *
 */
public class SplitLoggerForOracle implements Dblogger {

    private DbloggerPersistence persistenceLogger;
    
    private DbloggerApplication applicationLogger;
    
    private long jobId;
    
    private Logger logger = LoggerFactory.getLogger(getClass());
    
    private boolean throwExceptions = true;

    private Connection applicationConnection;
    

    public SplitLoggerForOracle(Connection connection, Connection loggerPersistenceConnection)
            throws IOException, SQLException, SqlSplitterException {
        //super(connection);
        Dialect persistenceDialect = Dialect.getDialect(loggerPersistenceConnection);
        switch (persistenceDialect) {
        case ORACLE:
            this.persistenceLogger = new DbloggerPersistenceImpl(loggerPersistenceConnection);
            break;
        case H2:
            this.persistenceLogger = new DbloggerPersistenceImpl(loggerPersistenceConnection);
            break;
        default:
            throw new IllegalArgumentException("Unsupported logger Dialect: " + persistenceDialect);
        }
        applicationConnection = connection;
        applicationLogger = new DbloggerForOracleApplication(connection);
        logger.debug("SplitLogger constructor: {}", OracleSessionInfo.getConnectionInfo(connection));

    }

    public SplitLoggerForOracle(Connection connection, DbloggerPersistence persistenceLogger)
            throws IOException, SQLException, SqlSplitterException {


        logger.debug("SplitLogger constructor: {}", OracleSessionInfo.getConnectionInfo(connection));
        this.persistenceLogger = persistenceLogger;
        applicationConnection = connection;
        applicationLogger = new DbloggerForOracleApplication(connection);
    }

    @Override
    public void prepareConnection() throws SQLException {
        // TODO Auto-generated method stub

    }

    @Override

    //  long startJobLogging(String processName, String moduleName, String statusMsg, int traceLevel) throws SQLExceptio
    public long startJobLogging(String processName,String className, String moduleName, String statusMsg,
           int traceLevel) throws SQLException {
     //   logger.debug("beginJob: " + OracleSessionInfo.getConnectionInfo(connection));
        logger.warn("tracefileName ignored");
        jobId = persistenceLogger.getNextJobLogId();
        SqlStatement ss = new SqlStatement("select user from dual");
        ss.setConnection(applicationConnection);
        NameValue userNv = ss.getNameValue();
        String schema = userNv.getString("user");
        
        String tracefileName = applicationLogger.appJobStart(processName,className, moduleName, statusMsg, jobId, traceLevel);
    //    applicationLogger.setTracefileIdentifier(jobId);
  //      String appTracefileName = applicationLogger.getTraceFileName();
    
        persistenceLogger.persistJob(processName,className, moduleName, statusMsg, schema,
                tracefileName, jobId);
  //      logger.info("********updating tracefile name to {}", tracefileName);
//        persistenceLogger.updateTraceFileName(appTracefileName);
//        persistenceLogger.setJobId(jobId);
        return jobId;
    }

    @Override
    public void abortJob(Exception e) {
        try {
            persistenceLogger.abortJob(e);
            updateJobWithTrace();
        } catch (SQLException e1) {
           logger.error("",e1);
        }

    }

    @Override
    public void endJob() throws SQLException {
        persistenceLogger.endJob();
        updateJobWithTrace();
    }

//    @Override
//    public void dispose() throws SQLException {
//  //      persistencelogger.dispose();
//        super.dispose();
//
//    }

    @Override
    public long insertStep(String stepName, String stepInfo, String className)  {
        String stack = ThreadUtil.getStackTrace();
        if (stack.length() > 4000) {
            stack = stack.substring(0,4000);
        }
        if (jobId != persistenceLogger.getJobLogId()) {
            String message = String.format("this jobId %d  persistenceJobId %d", this.jobId, persistenceLogger.getJobLogId());
            throw new IllegalStateException(message);
        }
        long stepId =  persistenceLogger.insertStep(stepName, stepInfo, className, stack);
      
        try {
            applicationLogger.setTraceStep(stepName,stepId);
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            logger.error(e.getMessage());
        }
        return stepId;
    }

    @Override
    public void finishStep(){
        try {
            persistenceLogger.finishStep();
        } catch (SQLException e) {
            logger.error(e.getMessage());
            if (throwExceptions) {
                throw new RuntimeException(e);
            }
        }

    }
//
//    @Override
//    public long getUtProcessStatusId() {
//        return persistenceLogger.getUtProcessStatusId();
//    }

    public String getTraceFileName(Connection conn, long id) throws SQLException {
        SqlStatement ss = new SqlStatement(conn,
                "select * from job_log where job_log_id = :job_log_id");
        Binds binds = new Binds();
        binds.put("job_log_id", id);
        final NameValue status = ss.getNameValue(binds, false);

        String tracefileName = status.getString("TRACEFILE_NAME");
        return tracefileName;
    }
    
    public void updateJobWithTrace() throws SQLException {
        StringWriter traceFileWriter = new StringWriter();
       
        Clob inputClob = null;
            try {
            inputClob = applicationLogger.getMyTraceFile();
           
            } catch (Exception e) {
                String message = "Does directory 'UDUMP_DIR' exist? Is this where the trace files are? Can this oracle reader read the diretory?";
                logger.error(message + " " + e.getMessage());
                throw new RuntimeException(e);
            }
            persistenceLogger.persistenceUpdateTrace(jobId, inputClob);
           
            



//            SqlStatement upsStatement = new SqlStatement(connection, ups);

//            NameValue upsRow = upsStatement.getNameValue(binds, true);
//            String traceFileName = upsRow.getString("tracefile_name");
//            Clob clob = connection.createClob();
//            String tracefileData = FileUtil.getAsString(traceFileName);
//            clob.setString(1, tracefileData);

            
       
        logger.warn("updated {}", jobId);
    }

//    @Override
//    public long beginJob(String processName, String className, String moduleName, String statusMsg, String threadName,
//            String tracefileName, int traceLevel) {
//        // TODO Auto-generated method stub
//        return 0;
//    }

//    @Override
//    public long insertStep(String stepName, String stepInfo, String className, String stack) {
//        // TODO Auto-generated method stub
//        return 0;
//    }
////
//    @Override
//    public void showConnectionInformation() {
//        // TODO Auto-generated method stub
//        
//    }

//    @Override
//    public void updateTraceFileName(String appTracefileName) throws SQLException {
//        // TODO Auto-generated method stub
//        
//    }
//
//    @Override
//    public Clob createClob() throws SQLException {
//        // TODO Auto-generated method stub
//        return null;
//    }
//
//    @Override
//    public void persistenceUpdateTrace(long jobId, Clob traceData) throws SQLException {
//        // TODO Auto-generated method stub
//        
//    }

    @Override
    public void setPersistTraceOnJobCompletion(boolean persistTrace) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setPersistPlansOnJobCompletion(boolean persistPlans) {
        // TODO Auto-generated method stub
        
    }

//    @Override
//    public void setJobId(long jobId) {
//        // TODO Auto-generated method stub
//        
//    }
//
//    @Override
//    public long getJobId() {
//        // TODO Auto-generated method stub
//        return 0;
//    }

    @Override
    public void setAction(String actionName){
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setModule(String moduleName, String actionName) throws SQLException {
        // TODO Auto-generated method stub
        
    }

//    @Override
//    public long beginJob(String processName, String processInfo, int traceLevel) {
//        // TODO Auto-generated method stub
//        return 0;
//    }

    @Override
    public long insertStep(String stepName, String stepInfo, String className, String stack) {
        return persistenceLogger.insertStep(stepName, stepInfo, className, null);
    }

//    @Override
//    public String getTraceFileName() throws SQLException {
//        // TODO Auto-generated method stub
//        return null;
//    }
//
//    @Override
//    public void getMyTraceFile(File file) throws IOException, SQLException {
//        // TODO Auto-generated method stub
//        
//    }
//
//    @Override
//    public void getMyTraceFile(Writer writer) throws SQLException, IOException {
//        // TODO Auto-generated method stub
//        
//    }
//
//    @Override
//    public String openFile(String fileName) throws SQLException {
//        // TODO Auto-generated method stub
//        return null;
//    }


    
  
}
