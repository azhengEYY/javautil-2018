package org.javautil.dblogging;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import org.javautil.dblogging.logger.Dblogger;
import org.javautil.sql.Binds;
import org.javautil.sql.ConnectionUtil;
import org.javautil.sql.SqlStatement;
import org.javautil.util.NameValue;

public class SampleUsage {

    private Dblogger   dblogger;
    private Connection connection;
    private String     processName;

    public SampleUsage(Connection connection, Dblogger dblogger, String processName) {
        this.connection = connection;
        this.dblogger = dblogger;
        this.processName = processName;
    }

    public long process() throws SQLException {
        dblogger.prepareConnection();

        long id =  dblogger.startJobLogging(processName,getClass().getName(), "SampleUsage","", 8);

        actionNoStep();
        stepNoAction();
        dblogger.endJob();
        return id;

    }

    public long processException() throws SQLException, FileNotFoundException, IOException {
       // id = dblogger.startJobLogging(processName, "ExampleLogging", null, 12);
        long logJobId = dblogger.startJobLogging(processName, getClass().getName(), "SamplueUsage", null,  8);
 
        try {
            int x  = 1 / 0;
        } catch (Exception e) {
            dblogger.abortJob(e);
        }
 
        return logJobId;
    }

    private void actionNoStep() throws SQLException {

        dblogger.setAction("Some work");
        ConnectionUtil.exhaustQuery(connection, "select * from user_tab_columns, user_tables");
    }

    private void stepNoAction() throws SQLException {

        dblogger.insertStep("Useless join", "full join", getClass().getName());
        ConnectionUtil.exhaustQuery(connection, "select * from user_tab_columns, user_tables");
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
