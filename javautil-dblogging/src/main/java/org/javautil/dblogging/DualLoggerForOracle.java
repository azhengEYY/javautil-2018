package org.javautil.dblogging;

import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.sql.DataSource;

import org.javautil.sql.SqlSplitterException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DualLoggerForOracle extends DbloggerForOracle implements Dblogger {

    private DbloggerH2    h2logger;

    private static Logger logger = LoggerFactory.getLogger(DualLoggerForOracle.class);

    public DualLoggerForOracle(Connection connection, DataSource h2loggerDataSource)
            throws IOException, SQLException, SqlSplitterException {
        super(connection);
        h2logger = new DbloggerH2(h2loggerDataSource.getConnection());
    }

    @Override
    CallableStatement prepareCall(String sql) throws SQLException {
        if (connection == null) {
            throw new IllegalStateException("connection is null");
        }
        final CallableStatement retval = connection.prepareCall(sql);
        callableStatements.add(retval);
        return retval;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.javautil.dblogging.DatabaseInstrumentation#prepareConnection()
     */
    @Override
    public void prepareConnection() throws SQLException {
        final String sql = "begin logger.prepare_connection; end;";
        if (prepareConnectionStatement == null) {
            prepareConnectionStatement = prepareCall(sql);
        }
        prepareConnectionStatement.execute();
        prepareConnectionStatement.close();
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.javautil.dblogging.DatabaseInstrumentation#beginJob(java.lang.String,
     * java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public int beginJob(final String processName, String className, String moduleName, String statusMsg,
            String threadName, String tracefileName) throws SQLException {
        long id = h2logger.getUtProcessStatusId();
        setTracefileIdentifier(id);
        String traceFilename = getTraceFileName();
        return h2logger.beginJob(processName, className, moduleName, statusMsg, threadName, traceFilename);

    }

    /*
     * (non-Javadoc)
     *
     * @see org.javautil.dblogging.DatabaseInstrumentation#abortJob()
     */
    @Override
    public void abortJob() throws SQLException {
        h2logger.abortJob();

    }

    /*
     * (non-Javadoc)
     *
     * @see org.javautil.dblogging.DatabaseInstrumentation#endJob()
     */
    @Override
    public void endJob() throws SQLException {
        h2logger.endJob();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.javautil.dblogging.DatabaseInstrumentation#dispose()
     */
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
        return h2logger.insertStep(myStepName, stepInfo, className);
    }

    @Override
    public void finishStep() throws SQLException {
        h2logger.finishStep();

    }

}
