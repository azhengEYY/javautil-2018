package org.javautil.dblogging;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

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

        final long id = dblogger.beginJob(processName, getClass().getCanonicalName(), "ExampleLogging", null,
                Thread.currentThread().getName(), null);
        actionNoStep();
        stepNoAction();
        dblogger.endJob();
        return id;

    }

    public long processException() throws SQLException, FileNotFoundException, IOException {
        final long id = dblogger.beginJob(processName, getClass().getCanonicalName(), "ExampleLogging", null,
                Thread.currentThread().getName(), null);
        try {
            int x  = 1 / 0;
        } catch (Exception e) {
            dblogger.abortJob(e);
        }
 
        return id;
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
