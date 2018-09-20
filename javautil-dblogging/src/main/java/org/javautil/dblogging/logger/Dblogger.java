package org.javautil.dblogging.logger;

import java.sql.SQLException;

public interface Dblogger {

    long insertStep(String string, String ruleName, String name);

    void setAction(String string);

    void endJob() throws SQLException;

    void prepareConnection() throws SQLException;


    void setModule(String moduleName, String actionName) throws SQLException;

    void abortJob(Exception e) throws SQLException;


    long insertStep(String stepName, String stepInfo, String className, String stack);

    void finishStep();


    void setPersistTraceOnJobCompletion(boolean persistTrace);

    void setPersistPlansOnJobCompletion(boolean persistPlans);


    long startJobLogging(String processName, String className, String moduleName, String statusMsg, int traceLevel) throws SQLException;

  
}