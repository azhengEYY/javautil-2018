package org.javautil.dblogging;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.sql.SQLException;

public class DatabaseLoggerNoOperation implements Dblogger {

	@Override
	public void prepareConnection() throws SQLException {
	}

	@Override
	public long beginJob(String processName, String className, String moduleName, String statusMsg, String threadName, String tracefileName)
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
	}

	@Override
	public long insertStep(String stepName, String stepInfo, String className) {
		return -1;
	}

    @Override
    public long getUtProcessStatusId() {
        return -1;
    }

    @Override
    public void updateTraceFileName(String appTracefileName) throws SQLException {
        // TODO Auto-generated method stub
        
    }

}
