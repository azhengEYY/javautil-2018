package org.javautil.dblogging.misc;

import java.io.File;
import java.io.IOException;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.javautil.oracle.trace.formatter.TkprofSort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TkprofRunner {

    public static final String tkprof = "/common/oracle/product/12.2.0/dbhome_1/bin/tkprof"; // TODO must be
                                                                                             // configurable

    private TkprofSort         sortBy;

    private Logger             logger = LoggerFactory.getLogger(getClass());

    private Integer            topNStatements;

    private Boolean            aggregate;

    private Boolean            sys;
    private Boolean            waits;

    public int runTkprof(File inFile, String outFileName) throws ExecuteException, IOException {
        String command = String.format("%s %s %s", tkprof, inFile.getAbsoluteFile(), outFileName);
        CommandLine commandLine = CommandLine.parse(command);
        DefaultExecutor executor = new DefaultExecutor();
        int retval = executor.execute(commandLine);
        logger.warn("wrote " + outFileName);
        return retval; // TODO should check return code and throw exception
    }

    public TkprofSort getSortBy() {
        return sortBy;
    }

    public TkprofRunner setSortBy(TkprofSort sortBy) {
        this.sortBy = sortBy;
        return this;
    }

    public Integer getTopNStatements() {
        return topNStatements;
    }

    public TkprofRunner setTopNStatements(Integer topNStatements) {
        this.topNStatements = topNStatements;
        return this;
    }

    public Boolean getAggregate() {
        return aggregate;
    }

    public TkprofRunner setAggregate(Boolean aggregate) {
        this.aggregate = aggregate;
        return this;
    }

    public Boolean getWaits() {
        return waits;
    }

    public TkprofRunner setWaits(Boolean waits) {
        this.waits = waits;
        return this;
    }
}
