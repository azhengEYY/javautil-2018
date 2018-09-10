package org.javautil.dblogging.misc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.javautil.io.FileUtil;
import org.javautil.sql.Binds;
import org.javautil.sql.SqlSplitterException;
import org.javautil.sql.SqlStatement;
import org.javautil.util.NameValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JobLogOracleStatUpdater implements JobLogTraceUpdater {

    private Connection connection;

    Logger             logger = LoggerFactory.getLogger(getClass());

    JobLogOracleStatUpdater(Connection connection) throws SqlSplitterException, IOException, SQLException {
        this.connection = connection;
    }

    public int runTkprof(File inFile, String outFileName) throws ExecuteException, IOException {
        String tkprof = "/common/orainst/bin/tkprof";
        // String tkprof = "/common/oracle/product/12.2.0/dbhome_1/bin/tkprof"; // TODO
        // must be configurable
        String command = String.format("%s %s %s", tkprof, inFile.getAbsoluteFile(), outFileName);
        CommandLine commandLine = CommandLine.parse(command);
        DefaultExecutor executor = new DefaultExecutor();
        int retval = executor.execute(commandLine);
        logger.info("wrote " + outFileName);
        return retval; // TODO should check return code and throw exception
    }
    // https://docs.oracle.com/javase/tutorial/jdbc/basics/blob.html
    // TODO put in javautil

    // private void copyFileToWriter(File inputFile, Writer writerArg)
    // throws FileNotFoundException, IOException {
    //
    // BufferedReader br = new BufferedReader(new FileReader(inputFile));
    // String nextLine = "";
    // while ((nextLine = br.readLine()) != null) {
    // System.out.println("Writing: " + nextLine);
    // writerArg.write(nextLine);
    // }
    //
    // }

    public void updateJobs() throws SQLException, FileNotFoundException, IOException {
        String noTraceSql = "select job_stat_id from job_stat where tracefile_data is null";
        SqlStatement ss = new SqlStatement(connection, noTraceSql);
        for (NameValue nv : ss.getListOfNameValue(new Binds())) {
            updateJob(nv.getLong("job_stat_id"));
        }
    }
    
    

    /* (non-Javadoc)
     * @see org.javautil.dblogging.misc.JobLogTraceUpdater#updateJob(long)
     */
    @Override
    public void updateJob(long jobId) throws SQLException, FileNotFoundException, IOException {
        String ups = "select tracefile_name from job_log "
                + "where job_log_id = :job_log_id";

        String upd = "update job_log "
                + "set tracefile_data =  ? "
                + "where job_log_id = ?";

        SqlStatement upsStatement = new SqlStatement(connection, ups);
        Binds binds = new Binds();
        binds.put("job_log_id", jobId);
        NameValue upsRow = upsStatement.getNameValue(binds, true);
        String traceFileName = upsRow.getString("tracefile_name");
        Clob clob = connection.createClob();
        String tracefileData = FileUtil.getAsString(traceFileName);
        clob.setString(1, tracefileData);

        PreparedStatement updateTraceFile = connection.prepareStatement(upd);

        updateTraceFile.setLong(2, jobId);
        updateTraceFile.setClob(1, clob);
        int count = updateTraceFile.executeUpdate();
        binds.put("tracefile_data", clob);
        if (count != 1) {
            throw new IllegalArgumentException("unable to update job_log_id " + jobId);
        }
        logger.warn("updated {}", jobId);
    }

    public void tkprofJob(long jobId) throws SQLException, IOException {
        String getTraceData = "select tracefile_data from job_log "
                + "where job_log_id = ?";
        logger.warn("tkprof job #" + jobId);
        PreparedStatement traceDataStatement = connection.prepareStatement(getTraceData);
        traceDataStatement.setLong(1, jobId);
        ResultSet rset = traceDataStatement.executeQuery();
        rset.next();
        String traceData = rset.getString(1);
        String tracedataFilename = "/tmp/job_" + jobId + ".trc";
        FileWriter fw = new FileWriter(tracedataFilename); // todo temp file and cleanup
        fw.write(traceData);
        fw.close();
        String tkprofFilename = "/tmp/job_" + jobId + ".prf";
        runTkprof(new File(tracedataFilename), tkprofFilename);
        logger.warn("wrote {}", tkprofFilename);
    }
}
