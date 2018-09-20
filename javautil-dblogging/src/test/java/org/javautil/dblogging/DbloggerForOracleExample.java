package org.javautil.dblogging;

import java.sql.Connection;
import java.sql.SQLException;

import org.javautil.dblogging.logger.Dblogger;
import org.javautil.sql.Binds;
import org.javautil.sql.ConnectionUtil;
import org.javautil.sql.SqlStatement;
import org.javautil.util.NameValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DbloggerForOracleExample {


    private Dblogger   dblogger;
    private Connection connection;
    private String     processName;
    private boolean testAbort = false;
    private int  traceLevel;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public DbloggerForOracleExample(Connection connection, Dblogger dblogger, String processName, boolean testAbort, int traceLevel) {
        this.connection = connection;
        this.dblogger = dblogger;
        this.processName = processName;
        this.testAbort = testAbort;
        this.traceLevel = traceLevel;
    }

    public long process() throws SQLException {
        dblogger.prepareConnection();
        long id = 0;

        try {
         //   long startJobLogging(String processName, String moduleName, String statusMsg, int traceLevel) throws SQLException;

         id = dblogger.startJobLogging(processName, getClass().getName(), "ExampleLogging", null, 12);
         logger.debug("============================jobId: {}", id);
        actionNoStep();
        stepNoAction();
        stepTwo();
        if (testAbort) {
            int x = 1 / 0;
        }
        logger.debug("calling endJob");
        dblogger.endJob();
        } catch (Exception e) {
            logger.error(e.getMessage());
            e.printStackTrace();
            dblogger.abortJob(e);
            throw e;
        }
     
        return id;

    }

   

    private void actionNoStep() throws SQLException {
        logger.debug("actionNoStep =============");
        dblogger.setAction("Some work");
        ConnectionUtil.exhaustQuery(connection, "select * from user_tab_columns, user_tables where rownum < 200");
        logger.debug("actionNoStep complete =============");
    }

    private void stepNoAction() throws SQLException {
        logger.debug("stepNoAction =============");
        dblogger.insertStep("Useless join", "full join", getClass().getName());
        ConnectionUtil.exhaustQuery(connection, "select * from user_tab_columns, user_tables");
        dblogger.finishStep();
        logger.debug("stepNoAction complete =============");
    }
    
    private void stepTwo() throws SQLException {
        dblogger.insertStep("count full", "full join", getClass().getName());
        ConnectionUtil.exhaustQuery(connection, "select count(*) dracula from user_tables");
        dblogger.finishStep();
    }

    NameValue getUtProcessStatus(Connection connection, long id) throws SQLException {
        final String sql = "select * from job_log "
                + "where job_log_id = :job_stat_id";
        final SqlStatement ss = new SqlStatement(connection, sql);
        Binds binds = new Binds();
        binds.put("job_stat_id", id);
        final NameValue retval = ss.getNameValue();
        ss.close();
        return retval;
    }

}
