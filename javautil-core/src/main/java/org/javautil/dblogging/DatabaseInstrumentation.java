package org.javautil.dblogging;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.sql.SQLException;

public interface DatabaseInstrumentation {

	void prepareConnection() throws SQLException;

	int beginJob(String processName, String className, String moduleName, String statusMsg, String threadName, String tracefileName)
			throws SQLException;

	void abortJob() throws SQLException;

	void endJob() throws SQLException;

	void setAction(String actionName) throws SQLException;

	void setModule(String moduleName, String actionName) throws SQLException;

	String getTraceFileName() throws SQLException;

	void getMyTraceFile(File file) throws IOException, SQLException;

	void getMyTraceFile(Writer writer) throws SQLException, IOException;

	void dispose() throws SQLException;

	String openFile(String fileName) throws SQLException;
	
	 long insertStep(String stepName, String stepInfo, String className);


	void finishStep() throws SQLException;
	
	void showConnectionInformation();
}