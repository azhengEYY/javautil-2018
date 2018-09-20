package org.javautil.dblogging.tracepersistence;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.sql.Clob;
import java.sql.SQLException;

public interface DbloggerPersistence {


//	long beginJob(String processName, String className, String moduleName, String statusMsg, String threadName, String tracefileName)
//			throws SQLException;
//	
//	   long beginJob(String processName, String className, String moduleName, String statusMsg, String threadName, String tracefileName, int traceLevel)
//	            throws SQLException;
//	
	  void abortJob(Exception e) throws SQLException;

	//void abortJob() throws SQLException;

	void endJob() throws SQLException;
	
   // long insertStep(String stepName, String stepInfo, String className) throws SQLException;

    long insertStep(String stepName, String stepInfo, String className, String stack);
    
    void finishStep() throws SQLException;


	void showConnectionInformation();

  //  long getUtProcessStatusId();

    void updateTraceFileName(String appTracefileName) throws SQLException;



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

  //  void setJobId(long jobId);

    long getJobLogId();


    long getNextJobLogId();

    long persistJob(String processName, String className, String moduleName, String statusMsg, String tracefileName, String schema,
            long jobLogId) throws SQLException;

  
}