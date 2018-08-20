package org.javautil.dblogging;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.sql.SQLException;

public class DatabaseLoggerNoOperation implements DatabaseInstrumentation {

    @Override
    public void prepareConnection() throws SQLException {
    }

    @Override
    public int beginJob(String processName, String className, String moduleName, String statusMsg, String threadName,
            String tracefileName)
            throws SQLException {
        return -1;
    }

    @Override
    public void abortJob() throws SQLException {
    }

    @Override
    public void endJob() throws SQLException {
    }

    @Override
    public void setAction(String actionName) throws SQLException {
    }

    @Override
    public void setModule(String moduleName, String actionName) throws SQLException {
    }

    @Override
    public String getTraceFileName() throws SQLException {
        return null;
    }

    @Override
    public void getMyTraceFile(File file) throws IOException, SQLException {
    }

    @Override
    public void getMyTraceFile(Writer writer) throws SQLException, IOException {
    }

    @Override
    public void dispose() throws SQLException {

    }

    @Override
    public String openFile(String fileName) throws SQLException {
        return null;
    }

    @Override
    public void finishStep() throws SQLException {

    }

    @Override
    public void showConnectionInformation() {
        // TODO Auto-generated method stub

    }

    @Override
    public long insertStep(String arg0, String arg1, String arg2) {
        // TODO Auto-generated method stub
        return -1;
    }

}