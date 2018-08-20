package org.javautil.dblogging;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BasicPersistentenceDblogger extends DbloggerForOracle {

    protected Dblogger persistencelogger;
    private static Logger logger = LoggerFactory.getLogger(SplitLoggerForOracle.class);

    public BasicPersistentenceDblogger(Connection connection) {
        super(connection);
    }

    @Override
    protected CallableStatement prepareCall(String sql) throws SQLException {
        final CallableStatement retval = connection.prepareCall(sql);
        callableStatements.add(retval);
        return retval;
    }

    @Override
    public void prepareConnection() throws SQLException {
        final String sql = "begin logger.prepare_connection; end;";
        if (prepareConnectionStatement == null) {
            prepareConnectionStatement = prepareCall(sql);
        }
        prepareConnectionStatement.execute();
        prepareConnectionStatement.close();
    }

    @Override
    public int beginJob(final String processName, String className, String moduleName, String statusMsg, String threadName, String tracefileName)
            throws SQLException {
                long id = persistencelogger.getUtProcessStatusId();
                setTracefileIdentifier(id);
                String traceFilename = getTraceFileName();
                return persistencelogger.beginJob(processName, className, moduleName, statusMsg, threadName, traceFilename);
            
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
        for (final CallableStatement cs : callableStatements) {
            cs.close();
        }
        callableStatements = new ArrayList<>();
    }

    @Override
    public long insertStep(final String stepName, String stepInfo, String className) {
    
        String myStepName;
        if (stepName.length() > 64) {
            myStepName = stepName.substring(0, 64);
            logger.warn("stepname: '{}' truncated to '{}'", stepName, myStepName);
        } else {
            myStepName = stepName;
        }
        try {
            setTraceStep(myStepName);
        } catch (SQLException e) {
            e.printStackTrace();
            logger.error(e.getMessage());
        }
        return persistencelogger.insertStep(myStepName, stepInfo, className);
    }

    @Override
    public void finishStep() throws SQLException {
        persistencelogger.finishStep();
    
    }

}