package org.javautil.dblogging;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.sql.Clob;
import java.sql.SQLException;

public interface Dblogger {

	void prepareConnection() throws SQLException;

	long beginJob(String processName, String className, String moduleName, String statusMsg, String threadName, String tracefileName)
			throws SQLException;
	
	  void abortJob(Exception e) throws SQLException;

	//void abortJob() throws SQLException;

	void endJob() throws SQLException;
	
    long insertStep(String stepName, String stepInfo, String className) throws SQLException;


    void finishStep() throws SQLException;

	void setAction(String actionName) throws SQLException;

	void setModule(String moduleName, String actionName) throws SQLException;

	String getTraceFileName() throws SQLException;

	void getMyTraceFile(File file) throws IOException, SQLException;

	void getMyTraceFile(Writer writer) throws SQLException, IOException;

	void dispose() throws SQLException;

	String openFile(String fileName) throws SQLException;
	

	
	void showConnectionInformation();

    long getUtProcessStatusId();

    void updateTraceFileName(String appTracefileName) throws SQLException;

    long insertStep(String stepName, String stepInfo, String className, String stack);

    Clob createClob() throws SQLException;


    
    public void persistenceUpdateTrace(long jobId, Clob traceData) throws SQLException;
    
    /**
     * Store the trace file in job_log on job_abort or job_end.
     * 
     * This burns some cycles on the instrumented application but ensures the file is not lost.
     * @param persistTrace
     */
    public void setPersistTraceOnJobCompletion(boolean persistTrace);
    
    public void setPersistPlansOnJobCompletion(boolean persistPlans);

  
}