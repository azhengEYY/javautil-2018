package org.javautil.dblogging;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.sql.Connection;
import java.sql.SQLException;

import org.javautil.sql.NamedSqlStatements;
import org.javautil.sql.SequenceHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO these should all throw Dblogger exception, don't want
//  to blow up a job because of an error in the logger
public class DbloggerH2 extends AbstractDblogger implements Dblogger {

    static Logger  logger            = LoggerFactory.getLogger(DbloggerForOracle.class);

    private long   jobStartMilliseconds;

    private String moduleName;

    private String actionName;

    SequenceHelper sequenceHelper;

    long           utProcessStatusId = -1;

    long           utProcessStepId;

    public DbloggerH2(Connection connection) throws IOException, SQLException {
        super(connection);
        statements = NamedSqlStatements.getNameSqlStatementsFromSqlSplitterResource(this, "ddl/h2/dblogger_dml.ss.sql");
        sequenceHelper = new SequenceHelper(connection);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.javautil.dblogging.DatabaseInstrumentation#prepareConnection()
     */
    @Override
    public void prepareConnection() throws SQLException {
    }

    @Override
    public long getUtProcessStatusId() {
        long utProcessStatusId = -1;
        try {
            utProcessStatusId = sequenceHelper.getSequence("ut_process_status_id_seq");
        } catch (SQLException sqe) {
            logger.error(sqe.getMessage());
        }
        return utProcessStatusId;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.javautil.dblogging.DatabaseInstrumentation#setAction(java.lang.String)
     */
    @Override
    public void setAction(final String actionName) throws SQLException {
        this.actionName = actionName;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.javautil.dblogging.DatabaseInstrumentation#setModule(java.lang.String,
     * java.lang.String)
     */
    @Override
    public void setModule(final String moduleName, final String actionName) throws SQLException {
        this.moduleName = moduleName;
        this.actionName = actionName;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.javautil.dblogging.DatabaseInstrumentation#getTraceFileName()
     */
    @Override
    public String getTraceFileName() throws SQLException {
        throw new UnsupportedOperationException();
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.javautil.dblogging.DatabaseInstrumentation#getMyTraceFile(java.io.File)
     */
    @Override
    public void getMyTraceFile(File file) throws IOException, SQLException {

    }

    @Override
    public String openFile(String fileName) throws SQLException {
        return null;

    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.javautil.dblogging.DatabaseInstrumentation#getMyTraceFile(java.io.Writer)
     */
    @Override
    public void getMyTraceFile(Writer writer) throws SQLException, IOException {
        logger.info("getMyTraceFile unimplemented");
    }

    /*
     * (non-Javadoc)
     *
     * @see org.javautil.dblogging.DatabaseInstrumentation#dispose()
     */
    @Override
    public void dispose() throws SQLException {
        statements.close();
    }


    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public String getActionName() {
        return actionName;
    }

    public void setActionName(String actionName) {
        this.actionName = actionName;
    }

    public long getUtProcessStepId() {
        return utProcessStepId;
    }

}
