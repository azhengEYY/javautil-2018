package com.pacificdataservices.pdssr;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.sql.DataSource;

import org.javautil.dblogging.Dblogger;
import org.javautil.dblogging.DbloggerForOracle;
import org.javautil.dblogging.SplitLoggerForOracle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThreadedLoaderProcessor implements Runnable {
    Logger                            logger = LoggerFactory.getLogger(getClass());
    private DataSource                appDataSource;
    private DataSource                dbloggerDataSource;
    private int                       threadCount;
    private LoadFileInputStreamSource fileSource;

    public ThreadedLoaderProcessor(DataSource appDataSource, DataSource dbloggerDataSource, int threadCount) {
        this.appDataSource = appDataSource;
        this.dbloggerDataSource = dbloggerDataSource;
        this.threadCount = threadCount;
        fileSource = new LoadFileInputStreamSource();
    }

    public void run2() {

        try {
            for (int i = 0; i < threadCount; i++) {
                Connection appConnection = appDataSource.getConnection();
                Dblogger persistenceLogger = new DbloggerForOracle(dbloggerDataSource.getConnection());
                final SplitLoggerForOracle dblogger = new SplitLoggerForOracle(appConnection, persistenceLogger);
                LoadProcessor loadProcessor = new LoadProcessor(appConnection, dblogger, fileSource);
                logger.info("Starting loadProcessor " + i);
                Thread t = new Thread(loadProcessor);
                t.start();
                t.join();

            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.getMessage());
        }
    }

    public Runnable getLoadProcessor() {
        Runnable processor = null;
        try {
            Connection appConnection = appDataSource.getConnection();
            Dblogger persistenceLogger = new DbloggerForOracle(dbloggerDataSource.getConnection());
            final SplitLoggerForOracle dblogger = new SplitLoggerForOracle(appConnection, persistenceLogger);
            processor = new LoadProcessor(appConnection, dblogger, fileSource);
        
           
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return processor;
    }

    public void run() {

   
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
