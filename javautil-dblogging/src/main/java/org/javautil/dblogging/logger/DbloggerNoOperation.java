package org.javautil.dblogging.logger;

import java.sql.SQLException;

public class DbloggerNoOperation implements Dblogger {

    @Override
    public void prepareConnection() throws SQLException {
    }



    @Override
    public void abortJob(Exception e)  {
    }

    @Override
    public void endJob()  {
    }

    @Override
    public void setAction(String actionName) {
    }

    @Override
    public void setModule(String moduleName, String actionName) throws SQLException {
    }


    @Override
    public void finishStep() {
    }


    @Override
    public long insertStep(String stepName, String stepInfo, String className) {
        return -1;
    }



    @Override
    public long insertStep(String stepName, String stepInfo, String className, String stack) {
        // TODO Auto-generated method stub
        return -1;
    }


    @Override
    public void setPersistTraceOnJobCompletion(boolean persistTrace) {
    }

    @Override
    public void setPersistPlansOnJobCompletion(boolean persistPlans) {
        
    }

    

    @Override
    public long startJobLogging(String processName, String className, String moduleName, String statusMsg,
            int traceLevel) throws SQLException {

        return Integer.MIN_VALUE;
    }

//    @Override
//    public long beginJob(String processName, String processInfo, int traceLevel) {
//        // TODO Auto-generated method stub
//        return 0;
//    }


}
