Quick Start
===========

Add the dependency to your pom
------------------------------

code-block:: 

    <dependency>
         <groupId>org.javautil</groupId>
         <artifactId>javautil-core</artifactId>
         <version>2018.0.5-SNAPSHOT</version>
    </dependency>
    <dependency>
        <groupId>org.javautil</groupId>
        <artifactId>javautil-dblogging</artifactId>
        <version>2018.0.5-SNAPSHOT</version>
    </dependency>

Configure dblogger.properties
-----------------------------

code-block:: 

    dblogger.datasource.driver-class-name=org.h2.Driver
    dblogger.datasource.url=jdbc:h2:/tmp/dbloggerh2;AUTO_SERVER=TRUE;COMPRESS=TRUE
    dblogger.datasource.username=sr
    dblogger.datasource.password=tutorial

Add calls
---------

code-block:: 

    dblogger.beginJob(jobName, this.getClass().getCanonicalName(), "LoadProcessor", 
                getClass().getName(),
    Thread.currentThread().getName(), traceFileName);
    try {
        logger.info("tracing to" + traceFileName);
        dblogger.setAction("loadFile");
        etlFileId = loadFile(datafile, dblogger);
        dblogger.setAction("runConditions");
        runConditions(etlFileId);
        dblogger.setAction("prepost");
        prepost(etlFileId);
        dblogger.endJob();
        connection.commit(); // TODO without a commit there is nothing here. We should just leave on autocommit
    catch (final Exception e) {
        dblogger.abortJob(); // TODO needs stacktrace
        throw e;
    }

Instrument Called Routines
**************************

code-block:: 

    @Autoinject
    private DatabaseInstrumentation dblogger; 

    ...
    long stepId = dblogger.insertStep("ConditionIdentification",rule.getRuleName(),getClass().getName());
