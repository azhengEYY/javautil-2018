package org.javautil.dblogging.traceagent;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.sql.Clob;
import java.sql.SQLException;

public interface DbloggerApplication {

	void prepareConnection() throws SQLException;

	
	 public void setTraceStep(final String stepName, long jobStepId) throws SQLException;

	 public Clob getMyTraceFile() throws SQLException;

	void setAction(String actionName) throws SQLException;

	void setModule(String moduleName, String actionName) throws SQLException;

	String getTraceFileName() throws SQLException;

	void getMyTraceFile(File file) throws IOException, SQLException;

	void getMyTraceFile(Writer writer) throws SQLException, IOException;

	void dispose() throws SQLException;

	String openFile(String fileName) throws SQLException;
	

	
	void showConnectionInformation();

  //  long getUtProcessStatusId();
	
	long getNextJobId();

   


   // Clob createClob() throws SQLException;

    String setTracefileIdentifier(long jobId);
   // String tracefileName = applicationLogger.appJobStart(processName, moduleName, statusMsg, jobId, traceLevel);
    public String appJobStart(
            final String processName, 
            final String className,
            String moduleName, 
            String statusMsg,
            long jobLogId ,int traceLevel) throws SQLException;




  //  void endJob() throws SQLException;

    


    
  
    
//    /**
//     * Store the trace file in job_log on job_abort or job_end.
//     * 
//     * This burns some cycles on the instrumented application but ensures the file is not lost.
//     * @param persistTrace
//     */
//    public void setPersistTraceOnJobCompletion(boolean persistTrace);
//    
//    public void setPersistPlansOnJobCompletion(boolean persistPlans);



  
}