package com.pacificdataservices.pdssr;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Arrays;

import javax.sql.DataSource;

import org.javautil.dblogging.Dblogger;
import org.javautil.sql.SqlSplitterException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoadProcessor implements FilenameFilter, Runnable {
    private CdsDataLoader                           loader;
    private CdsBatchDataLoader                      batchLoader;

    private InstrumentedLoadConditionIdentification loadConditions;

    private Prepost                                 prepost;

    private Post                                    post;

    private final Logger                            logger = LoggerFactory.getLogger(getClass());

    private final Connection                        connection;

    Dblogger                              dblogger;

    private DataSource                              h2loggerDataSource;
    
    private LoadFileInputStreamSource loadSource;
    private boolean batch;

    public LoadProcessor(Connection connection, Dblogger dblogger, LoadFileInputStreamSource loadSource, boolean batch) throws SqlSplitterException, Exception {
        this.dblogger = dblogger;
        this.connection = connection;
        logger.info("dblogger : " + dblogger);
        logger.info("Database Instrumentation: " + dblogger.getClass().getCanonicalName());
        this.loadSource = loadSource;
        initializeSubProcesses();
        this.batch = batch;
    }

    private void initializeSubProcesses() throws SQLException, IOException, SqlSplitterException {
        // TODO create an interface
        if (batch) {
            batchLoader = new CdsBatchDataLoader(connection,dblogger);
        } else {
        loader = new CdsDataLoader(connection, dblogger);
        }
        batchLoader = new CdsBatchDataLoader(connection,dblogger);
        loadConditions = new InstrumentedLoadConditionIdentification(connection, dblogger);
        prepost = new Prepost(connection, 5, dblogger);
        post = new Post(connection, 5);
    }

//    void processFiles(File[] files) throws Exception, SqlSplitterException {
////        H2LoggerDataSourceCheck checker = new H2LoggerDataSourceCheck();
////        checker.testDataSource(h2loggerDataSource);
//        for (final File f : files) {
//            process(f);
//        }
//    }

//    public File[] getFiles() {
//        final String loadFileDir = "src/test/resources/dataloads";
//        final File loadDirFile = new File(loadFileDir);
//        final File[] files = loadDirFile.listFiles(this);
//        Arrays.sort(files);
//        return files;
//    }

    public long process(InputStream datafile, String dataFileDescr) throws Exception, SqlSplitterException {
        logger.info("process: {}",dataFileDescr);
        long etlFileId;

        final String jobName = "=====LoadProcessor " + dataFileDescr;
        logger.info("about to getTraceFileName");
        final String traceFileName = dblogger.getTraceFileName();
        logger.info("====== tracing to {}",traceFileName);
        logger.info("about to beginJob");
        long jobNbr = dblogger.beginJob(jobName, this.getClass().getCanonicalName(), "LoadProcessor", getClass().getName(),
                Thread.currentThread().getName(), traceFileName);
        logger.info("starting job {} utProcessStatusId {} {}", jobNbr,  dblogger.getUtProcessStatusId());
        try {
            logger.info("tracing to" + traceFileName);
            dblogger.setAction("loadFile");
            logger.info("about to load file: {}",dataFileDescr);
            etlFileId = loadFile(datafile, dataFileDescr, dblogger);
            dblogger.setAction("runConditions");
            runConditions(etlFileId);
            dblogger.setAction("prepost");
            prepost(etlFileId);
            dblogger.endJob();
            connection.commit(); // TODO without a commit there is nothing here. We should just leave on
                                 // autocommit
        } catch (final Exception e) {
            e.printStackTrace();
            dblogger.abortJob(e); // TODO needs stacktrace
            throw e;
        }
        return etlFileId;
    }

    public long loadFile(InputStream loadFile, String inputDescr, Dblogger dblogger)
            throws SQLException, ParseException, IOException {
        if (batch) {
            return loader.process(loadFile, inputDescr, "EXOTICTX", false);
        } else {
            return loader.process(loadFile, inputDescr, "EXOTICTX", false);
        }
    }

    public void runConditions(long etlFileId) throws SQLException, IOException, SqlSplitterException {
        loadConditions.process(etlFileId, 3);
    }

    public void prepost(long etlFileId) throws SQLException, InvalidLoadFileException {
        prepost.process(etlFileId);
    }

    @Override
    public boolean accept(File dir, String fileName) {
        return fileName.endsWith(".cds");
    }

    @Override
    public void run() {
        logger.info("starting ");
        InputStream is;
        int count = 0;
        try {
            while (( is = loadSource.next()) != null) {
                count++;
                File f = loadSource.getFile(is);
                logger.info("about to process {}",f.getAbsolutePath());
                process(is,f.getAbsolutePath());
            }
            // TODO clean this crap up
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SqlSplitterException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            try {
                connection.close();
            } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        logger.info("processed {}",count);
        
    }

    public LoadFileInputStreamSource getLoadSource() {
        return loadSource;
    }

    public void setLoadSource(LoadFileInputStreamSource loadSource) {
        this.loadSource = loadSource;
    }
}