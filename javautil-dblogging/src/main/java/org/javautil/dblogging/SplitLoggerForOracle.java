package org.javautil.dblogging;

import java.io.File;
import org.javautil.oracle.OracleSessionInfo;
import java.io.IOException;
import java.io.Writer;
import java.sql.Connection;
import java.sql.SQLException;

import org.javautil.sql.Binds;
import org.javautil.sql.SqlSplitterException;
import org.javautil.sql.SqlStatement;
import org.javautil.util.NameValue;

/**
 * Persistence is performed in a different database
 * @author jjs
 *
 */
public class SplitLoggerForOracle extends DbloggerForOracle implements Dblogger {
    
    

    private Dblogger persistencelogger;

    public SplitLoggerForOracle(Connection connection, Dblogger persistenceLogger)
            throws IOException, SQLException, SqlSplitterException {
        super(connection);
       
        logger.info("SplitLogger constructor: {}", OracleSessionInfo.getConnectionInfo(connection));
        this.persistencelogger = persistenceLogger;
    }

    @Override
    public void prepareConnection() throws SQLException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public long beginJob(String processName, String className, String moduleName, String statusMsg, String threadName,
            String tracefileName) throws SQLException {
        logger.info("beginJob: " + OracleSessionInfo.getConnectionInfo(connection));
        logger.warn("tracefileName ignored");
        long id =  persistencelogger.beginJob(processName, className, moduleName, statusMsg, threadName, null);
        String appTracefileName = getTraceFileName();
        logger.info("*******************updating tracefile name to {}",appTracefileName);
        persistencelogger.updateTraceFileName(appTracefileName);
       String persistenceTracefileName = persistencelogger.getTraceFileName();
        return id;
    }

    @Override
    public void abortJob() throws SQLException {
        persistencelogger.abortJob();
        
    }

    @Override
    public void endJob() throws SQLException {
        persistencelogger.endJob();
        
    }

    @Override
    public void dispose() throws SQLException {
        persistencelogger.dispose();
        super.dispose();
        
    }


    @Override
    public long insertStep(String stepName, String stepInfo, String className) {
        return persistencelogger.insertStep(stepName, stepInfo, className);
    }

    @Override
    public void finishStep() throws SQLException {
        persistencelogger.finishStep();
        
    }

   

    @Override
    public long getUtProcessStatusId() {
        return persistencelogger.getUtProcessStatusId();
    }
    
    public String getTraceFileName(Connection conn, long id) throws SQLException {
        SqlStatement ss = new SqlStatement(conn,"select * from ut_process_status where ut_process_status_id = :ut_process_status_id");
        Binds binds = new Binds();
        binds.put("ut_process_status_id",id);
        final NameValue status = ss.getNameValue(binds,false);
 
        String tracefileName = status.getString("TRACEFILE_NAME");
       return tracefileName;
    }

}
