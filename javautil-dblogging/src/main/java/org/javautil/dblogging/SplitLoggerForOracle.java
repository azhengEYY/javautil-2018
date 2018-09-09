package org.javautil.dblogging;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import org.javautil.lang.ThreadUtil;
import org.javautil.oracle.OracleSessionInfo;
import org.javautil.sql.Binds;
import org.javautil.sql.Dialect;
import org.javautil.sql.SqlSplitterException;
import org.javautil.sql.SqlStatement;
import org.javautil.util.NameValue;

/**
 * Persistence is performed in a different database
 * 
 * @author jjs
 *
 */
public class SplitLoggerForOracle extends DbloggerForOracle implements Dblogger {

    private Dblogger persistencelogger;

    public SplitLoggerForOracle(Connection connection, Connection loggerPersistenceConnection)
            throws IOException, SQLException, SqlSplitterException {
        super(connection);
        Dialect persistenceDialect = Dialect.getDialect(loggerPersistenceConnection);
        switch (persistenceDialect) {
        case ORACLE:
            this.persistencelogger = new DbloggerForOracle(loggerPersistenceConnection);
            break;
        case H2:
            this.persistencelogger = new DbloggerH2(loggerPersistenceConnection);
            break;
        default:
            throw new IllegalArgumentException("Unsupported logger Dialect: " + persistenceDialect);
        }
        logger.debug("SplitLogger constructor: {}", OracleSessionInfo.getConnectionInfo(connection));

    }

    public SplitLoggerForOracle(Connection connection, Dblogger persistenceLogger)
            throws IOException, SQLException, SqlSplitterException {
        super(connection);

        logger.debug("SplitLogger constructor: {}", OracleSessionInfo.getConnectionInfo(connection));
        this.persistencelogger = persistenceLogger;
    }

    @Override
    public void prepareConnection() throws SQLException {
        // TODO Auto-generated method stub

    }

    @Override
    public long beginJob(String processName, String className, String moduleName, String statusMsg, String threadName,
            String tracefileName) throws SQLException {
        logger.debug("beginJob: " + OracleSessionInfo.getConnectionInfo(connection));
        logger.warn("tracefileName ignored");
    
        long id = persistencelogger.beginJob(processName, className, moduleName, statusMsg, threadName, null);
        setTracefileIdentifier(id);
        String appTracefileName = getTraceFileName();
        logger.info("********updating tracefile name to {}", appTracefileName);
        persistencelogger.updateTraceFileName(appTracefileName);
        return id;
    }

    @Override
    public void abortJob(Exception e) throws SQLException {
        persistencelogger.abortJob(e);
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
        String stack = ThreadUtil.getStackTrace();
        if (stack.length() > 4000) {
            stack = stack.substring(0,4000);
        }
        return persistencelogger.insertStep(stepName, stepInfo, className, stack);
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
        SqlStatement ss = new SqlStatement(conn,
                "select * from job_log where job_log_id = :job_log_id");
        Binds binds = new Binds();
        binds.put("job_log_id", id);
        final NameValue status = ss.getNameValue(binds, false);

        String tracefileName = status.getString("TRACEFILE_NAME");
        return tracefileName;
    }

}
