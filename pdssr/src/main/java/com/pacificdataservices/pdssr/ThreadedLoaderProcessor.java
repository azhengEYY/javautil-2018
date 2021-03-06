package com.pacificdataservices.pdssr;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.sql.DataSource;


import org.javautil.dblogging.logger.SplitLoggerForOracle;
import org.javautil.dblogging.tracepersistence.DbloggerPersistence;
import org.javautil.dblogging.tracepersistence.DbloggerPersistenceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThreadedLoaderProcessor implements Runnable {
    Logger                            logger = LoggerFactory.getLogger(getClass());
    private DataSource                appDataSource;
    private DataSource                dbloggerDataSource;
    private int                       threadCount;
    private LoadFileInputStreamSource fileSource;
    private boolean batch;
    private boolean persistToAppConnection;

    
    public ThreadedLoaderProcessor(DataSource appDataSource, DataSource dbloggerDataSource, int threadCount, boolean batch, boolean persistToAppConnection) {
        this.appDataSource = appDataSource;
        this.dbloggerDataSource = dbloggerDataSource;
        this.threadCount = threadCount;
        fileSource = new LoadFileInputStreamSource();
        this.batch = batch;
        this.persistToAppConnection = persistToAppConnection;
    }


   
    public Runnable getLoadProcessor() {
        Runnable processor = null;
        try {
            Connection appConnection = appDataSource.getConnection();
            Connection persistenceConnection = persistToAppConnection ? appDataSource.getConnection() : dbloggerDataSource.getConnection();
            DbloggerPersistence persistenceLogger = new DbloggerPersistenceImpl(persistenceConnection);
            final SplitLoggerForOracle dblogger = new SplitLoggerForOracle(appConnection, persistenceLogger);
            processor = new LoadProcessor(appConnection, dblogger, fileSource, batch);
        
           
        } catch (Exception e) {
            // TODO this mus be blowing up it is sometimes returning null
            logger.error("====" + e.getMessage());
        }
        
        return processor;
    }

    public void run() {
        // TODO check to see if any of them had errors
   
        final ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        final ExecutorCompletionService<String> completionService = new ExecutorCompletionService<>(executorService);

        
        for (int i = 0 ; i <  threadCount; i++) {
           executorService.submit(getLoadProcessor());
        }
        logger.info("shutting down");
        executorService.shutdown();
        try {
            executorService.awaitTermination(10, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        
    }

}
