package org.javautil.oracle.trace.formatter;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.javautil.sql.Binds;
import org.javautil.sql.SqlStatement;
import org.javautil.util.ListOfNameValue;
import org.javautil.util.NameValue;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class LoggerModels {

    private Connection connection;

    public LoggerModels(DataSource dataSource) throws IOException, SQLException {
        this.connection = dataSource.getConnection();
    }

    public NameValue getJobInfo(long jobId) throws SQLException {
        String statusVwSql = "select ut_process_status_id, process_name, thread_name, "
                + "status_msg, status_id, status_ts, tracefile_name, classname "
                + "from ut_process_status where ut_process_status_id = :ut_process_status_id";
        SqlStatement statusVwSs = new SqlStatement(connection, statusVwSql);
        Binds binds = new Binds();
        binds.put("ut_process_status_id", jobId);
        NameValue statusVwNameValue = statusVwSs.getNameValue(binds, true);
        statusVwSs.close();
        return statusVwNameValue;
    }

    public ListOfNameValue getStepInfo(long jobId) throws SQLException {
        String stepVwSql = "select * from ut_process_step_vw where ut_process_status_id = :ut_process_status_id order by ut_process_step_id";
        SqlStatement stepVwSs = new SqlStatement(connection, stepVwSql);
        Binds binds = new Binds();
        binds.put("ut_process_status_id", jobId);
        ListOfNameValue stepVwNameValue = stepVwSs.getListOfNameValue(binds);
        stepVwSs.close();
        return stepVwNameValue;
    }

    public NameValue getJobStepInfo(long jobId) throws SQLException {
        NameValue lhm = new NameValue();
        lhm.put("job", getJobInfo(jobId));
        lhm.put("step", getStepInfo(jobId));
        return lhm;
    }

    public String getJobStepInfoJson(long jobId) throws SQLException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        NameValue jobStepInfoNv = getJobStepInfo(jobId);
        String json = gson.toJson(jobStepInfoNv);
        return json;
    }

}
