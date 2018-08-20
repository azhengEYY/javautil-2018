package com.pacificdataservices.pdssr;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Arrays;

import javax.sql.DataSource;

import org.javautil.dblogging.Dblogger;
import org.javautil.dblogging.H2LoggerDataSourceCheck;
import org.javautil.dblogging.H2LoggerForOracle;
import org.javautil.sql.SqlSplitterException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoadProcessor implements FilenameFilter {
    private CdsDataLoader                           loader;

    private InstrumentedLoadConditionIdentification loadConditions;

    private Prepost                                 prepost;

    private Post                                    post;

    private final Logger                            logger = LoggerFactory.getLogger(getClass());

    private final Connection                        connection;

    // DataSource ds = new
    // H2LoggerDataSource().getPopulatedH2FromDbLoggerProperties();
    H2LoggerForOracle                               dblogger;

    private DataSource                              h2loggerDataSource;

    public LoadProcessor(Connection connection, DataSource h2loggerDataSource) throws SqlSplitterException, Exception {
        this.h2loggerDataSource = h2loggerDataSource;
        this.connection = connection;
        this.dblogger = new H2LoggerForOracle(connection, h2loggerDataSource);
        logger.info("dblogger : " + dblogger);
        logger.info("Database Instrumentation: " + dblogger.getClass().getCanonicalName());
        initializeSubProcesses();
    }

    private void initializeSubProcesses() throws SQLException, IOException, SqlSplitterException {
        loader = new CdsDataLoader(connection, dblogger);
        loadConditions = new InstrumentedLoadConditionIdentification(connection, dblogger);
        prepost = new Prepost(connection, 5, dblogger);
        post = new Post(connection, 5);
    }

    void processFiles(File[] files) throws Exception, SqlSplitterException {
        H2LoggerDataSourceCheck checker = new H2LoggerDataSourceCheck();
        checker.testDataSource(h2loggerDataSource);
        for (final File f : files) {
            process(f);
        }
    }

    public File[] getFiles() {
        final String loadFileDir = "src/test/resources/dataloads";
        final File loadDirFile = new File(loadFileDir);
        final File[] files = loadDirFile.listFiles(this);
        Arrays.sort(files);
        return files;
    }

    public long process(File datafile) throws Exception, SqlSplitterException {
        long etlFileId;

        final String jobName = "LoadProcessor " + datafile.getAbsolutePath();
        final String traceFileName = dblogger.getTraceFileName();

        dblogger.beginJob(jobName, this.getClass().getCanonicalName(), "LoadProcessor", getClass().getName(),
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
            connection.commit(); // TODO without a commit there is nothing here. We should just leave on
                                 // autocommit
        } catch (final Exception e) {
            e.printStackTrace();
            dblogger.abortJob(); // TODO needs stacktrace
            throw e;
        }
        return etlFileId;
    }

    public long loadFile(File loadFile, Dblogger dblogger)
            throws SQLException, ParseException, IOException {
        return loader.process(loadFile.getAbsolutePath(), "EXOTICTX", false);
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
}