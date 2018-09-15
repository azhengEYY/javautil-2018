package org.javautil.oracle.trace.record;

import java.util.regex.Pattern;

public abstract class DbTime extends AbstractRecord {

    protected static final Pattern cpuPattern = Pattern.compile("c=(\\d*)");
    protected static final Pattern elapsedMicrosecondsPattern = Pattern.compile(",e=(\\d*)");
    /**
     * Total cpu consumption for call. Represented in trace file as c=%d c CPU
     * time (100th's of a second in Oracle7 ,8 and 9).
     */
    private int cpu;
    /**
     * Elapsed curation of timed event;
     */
    private long elapsedMicroSeconds;

    public DbTime(int lineNumber, String stmt) {
        super(lineNumber, stmt);
        setCpu(getInt(stmt, cpuPattern));
        setElapsedMicroSeconds(getLong(stmt, elapsedMicrosecondsPattern));
    }

    /**
     * @return
     */
    public int getCpu() {
        return cpu;
    }

    /**
     * @return Returns the e.
     */
    public long getElapsedMicroSeconds() {
        return elapsedMicroSeconds;
    }

    /**
     * @param c
     *            The c to set.
     */
    protected void setCpu(final int c) {
        this.cpu = c;
    }

    /**
     * @param ela
     *            The ela to set.
     */
    protected void setElapsedMicroSeconds(final long ela) {
        this.elapsedMicroSeconds = ela;
    }

}