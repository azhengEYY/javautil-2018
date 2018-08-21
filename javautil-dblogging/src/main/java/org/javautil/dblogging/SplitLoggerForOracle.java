package org.javautil.dblogging;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.sql.Connection;
import java.sql.SQLException;

import org.javautil.sql.SqlSplitterException;

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
        this.persistencelogger = persistenceLogger;
    }

    @Override
    public void prepareConnection() throws SQLException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public int beginJob(String processName, String className, String moduleName, String statusMsg, String threadName,
            String tracefileName) throws SQLException {
        return persistencelogger.beginJob(processName, className, moduleName, statusMsg, threadName, tracefileName);
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

}
