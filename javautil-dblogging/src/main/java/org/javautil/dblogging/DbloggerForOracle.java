package org.javautil.dblogging;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.javautil.oracle.OracleConnectionHelper;
import org.javautil.sql.Binds;
import org.javautil.sql.SequenceHelper;
import org.javautil.sql.SqlSplitterException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DbloggerForOracle extends AbstractDblogger implements Dblogger {

    private Logger                    logger             = LoggerFactory.getLogger(getClass());
    
    private String name;

    // TODO see if clear context is necessary

    // TODO move all to an .sr.sql

    // TODO start a loggerThread? How to communicate with it?

   // protected final Connection        connection;

    protected List<CallableStatement> callableStatements = new ArrayList<>();

    private CallableStatement         beginJobStatement;

    private CallableStatement         abortJobStatement;

    private CallableStatement         endJobStatement;

    protected CallableStatement       prepareConnectionStatement;

    protected CallableStatement       setActionStatement;

    private CallableStatement         setModuleStatement;

    private CallableStatement         getTraceFileStatement;

    private CallableStatement         getMyTraceFileStatement;

    private CallableStatement         openFileStatement;

    private CallableStatement         pushStepStatement;

    private CallableStatement         setTraceStepStatement;

    private CallableStatement         setTracefileIdentifierStatement;

    SequenceHelper                    sh;

    // private static Logger logger = LoggerFactory.getLogger(Dblogger.class);

    public DbloggerForOracle(Connection connection) throws SQLException, SqlSplitterException, IOException {
        super(connection); 
    }

    CallableStatement prepareCall(String sql) throws SQLException {
   
        final CallableStatement retval = getConnection().prepareCall(sql);
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
        final String sql = "       begin\n" + "         :ut_process_status_id := logger.begin_java_job (\n"
                + "           p_process_name => :p_process_name,  -- VARCHAR2,\n"
                + "           p_classname   => :p_classname,    -- varchar2,\n"
                + "           p_module_name  => :p_module_name,   -- varchar2,\n"
                + "           p_status_msg   => :p_status_msg,    -- varchar2,\n"
                + "           p_thread_name  => :p_thread_name   -- varchar2\n" + "          );\n" + "       end;\n"
                + "";
        if (beginJobStatement == null) {
            beginJobStatement = prepareCall(sql);
            beginJobStatement.registerOutParameter("ut_process_status_id", java.sql.Types.INTEGER);
        }
        final CallableStatement cs = beginJobStatement;
        cs.setString("p_process_name", processName);
        cs.setString("p_classname", className);
        cs.setString("p_module_name", moduleName);
        cs.setString("p_status_msg", statusMsg);
        cs.setString("p_thread_name", threadName);
        cs.execute();
        final int retval = cs.getInt("ut_process_status_id");
        setUtProcessStatusId(retval);
      //  cs.close();
        logger.info("started job {} " + retval);
        return retval;

    }

    /*
     * (non-Javadoc)
     *
     * @see org.javautil.dblogging.DatabaseInstrumentation#abortJob()
     */
    @Override
    public void abortJob() throws SQLException {
        if (abortJobStatement == null) {
            abortJobStatement = prepareCall("begin logger.abort_job(); end;");
        }
        abortJobStatement.execute();

    }

    /*
     * (non-Javadoc)
     *
     * @see org.javautil.dblogging.DatabaseInstrumentation#endJob()
     */
    @Override
    public void endJob() throws SQLException {
        if (endJobStatement == null) {
            endJobStatement = prepareCall("begin logger.end_job(); end;");
        }
        endJobStatement.execute();
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.javautil.dblogging.DatabaseInstrumentation#setAction(java.lang.String)
     */
    @Override
    public void setAction(final String actionName) throws SQLException {
        final String sql = "begin logger.set_action(p_action => :p_action_name); end;";
        if (setActionStatement == null) {
            setActionStatement = prepareCall(sql);
        }
        setActionStatement.setString("p_action_name", actionName);
        setActionStatement.execute();

    }

    public void setTraceStep(final String stepName) throws SQLException {
        final String sql = "begin logger.trace_step(p_step_name => :p_step_name); end;";
        if (setTraceStepStatement == null) {
            setTraceStepStatement = prepareCall(sql);
        }
        setTraceStepStatement.setString("p_step_name", stepName);
        setTraceStepStatement.execute();

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
        final String sql = "begin \n" + "   logger.set_module(p_module_name => :p_module_name, \n"
                + "                        p_action_name => :p_action_name); \n" + "end;";

        if (moduleName == null) {
            throw new IllegalArgumentException("moduleName is null");
        }
        final String actionNameUsed = actionName == null ? " " : actionName;
        if (setModuleStatement == null) {
            setModuleStatement = prepareCall(sql);
        }
        setModuleStatement.setString("p_module_name", moduleName);
        setModuleStatement.setString("p_action_name", actionNameUsed);
        setModuleStatement.execute();
    }

    public void pushStep(final String stepName) throws SQLException {
        final String sql = "begin logger.push_step(p_step_name => :p_step_name); end;";

        if (stepName == null) {
            throw new IllegalArgumentException("stepName is null");
        }

        if (pushStepStatement == null) {
            pushStepStatement = prepareCall(sql);
        }
        pushStepStatement.setString("p_step_name", stepName);

        setModuleStatement.execute();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.javautil.dblogging.DatabaseInstrumentation#getTraceFileName()
     */
    @Override
    public String getTraceFileName() throws SQLException {
        if (getTraceFileStatement == null) {
            getTraceFileStatement = prepareCall("begin :trace_file_name := logger.get_my_tracefile_name(); end;");
            getTraceFileStatement.registerOutParameter("trace_file_name", java.sql.Types.VARCHAR);
        }
        getTraceFileStatement.execute();

        final String retval = getTraceFileStatement.getString("trace_file_name");
        // getTraceFileStatement.close();
        return retval;
    }

    public String setTracefileIdentifier(long identifier) throws SQLException {
        if (setTracefileIdentifierStatement == null) {
            setTracefileIdentifierStatement = prepareCall(
                    "begin :trace_file_name := logger.set_tracefile_identifier(:p_identifier); end;");
            setTracefileIdentifierStatement.registerOutParameter("trace_file_name", java.sql.Types.VARCHAR);
        }

        setTracefileIdentifierStatement.setLong("p_identifier", identifier);
        setTracefileIdentifierStatement.execute();

        final String retval = setTracefileIdentifierStatement.getString("trace_file_name");

        return retval;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.javautil.dblogging.DatabaseInstrumentation#getMyTraceFile(java.io.File)
     */
    @Override
    public void getMyTraceFile(File file) throws IOException, SQLException {
        final FileWriter fw = new FileWriter(file);
        getMyTraceFile(fw);
        fw.close();
    }

    @Override
    public String openFile(String fileName) throws SQLException {
        if (openFileStatement == null) {
            openFileStatement = prepareCall(
                    "begin :log_file_name := logger.open_log_file(p_file_name => :p_file_name); end;");
            openFileStatement.registerOutParameter("log_file_name", java.sql.Types.VARCHAR);
        }
        final Binds binds = new Binds();
        binds.put("p_file_name", null);
        openFileStatement.setString("p_file_name", null);
        openFileStatement.execute();
        final String retval = openFileStatement.getString("LOG_FILE_NAME");
        return retval;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.javautil.dblogging.DatabaseInstrumentation#getMyTraceFile(java.io.Writer)
     */
    @Override
    public void getMyTraceFile(Writer writer) throws SQLException, IOException {
        if (getMyTraceFileStatement == null) {
            getMyTraceFileStatement = prepareCall("begin :my_tracefile_data := logger.get_my_tracefile(); end;");
            getMyTraceFileStatement.registerOutParameter(1, java.sql.Types.CLOB);
        }
        getMyTraceFileStatement.execute();

        final Clob clob = getMyTraceFileStatement.getClob(1);
        OracleConnectionHelper.clobWrite(clob, writer);
    }

    // // TODO should be in jdbcHelper
    // /*
    // * (non-Javadoc)
    // *
    // * @see
    // org.javautil.dblogging.DatabaseInstrumentation#clobWrite(java.sql.Clob,
    // * java.io.Writer)
    // */
    // @Override
    // public void clobWrite(Clob clob, Writer writer) throws SQLException,
    // IOException {
    // int length = 1024 * 1024;
    // Reader reader = clob.getCharacterStream();
    // char[] buffer = new char[length];
    // int count;
    // while ((count = reader.read(buffer)) != -1) {
    // writer.write(buffer, 0, count);
    // }
    // clob.free();
    // }

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
    public void showConnectionInformation() {
        // TODO Auto-generated method stub

    }

//    public long getUtProcessStatusId() {
//        return utProcessStatusId;
//    }

 //   public long getUtProcessStatusId() {
//        long utProcessStatusId = -999;
//        try {
//            if (sh == null) {
//                sh = new SequenceHelper(getConnection());
//            }
//            utProcessStatusId = sh.getSequence("ut_process_status_id_seq");
//        } catch (SQLException sqe) {
//            logger.error(sqe.getMessage());
//        }
//        return utProcessStatusId;
//    }

}
