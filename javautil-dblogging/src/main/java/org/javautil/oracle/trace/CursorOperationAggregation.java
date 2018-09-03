package org.javautil.oracle.trace;

import org.javautil.io.Tracer;
import org.javautil.oracle.trace.record.CursorOperation;
import org.javautil.oracle.trace.record.RecordType;
import org.javautil.util.EventHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CursorOperationAggregation {
    private transient static final Logger  logger                         = LoggerFactory
            .getLogger(CursorOperationAggregation.class);

    private transient static EventHelper   events                         = new EventHelper();

    private transient static final String  newline                        = System.getProperty("line.separator");
    private transient static final Integer LOG_AGGREGATE_CURSOR_OPERATION = Integer.valueOf(1);

    private transient static final Integer LOG_CURSOR_OPERATION_AGGREGATE = Integer.valueOf(2);

    private RecordType                     recordType                     = null;

    private long                           minimumTime;

    private long                           maximumTime;

    private int                            eventCount;

    /**
     * Total cpu consumption for call. Represented in trace file as c=%d c CPU
     * time (100th's of a second in Oracle7 ,8 and 9).
     */
    private int                            cpu;
    /**
     * Elapsed duration of the call. In trace file as "e".
     */
    /**
     * Elapsed curation of timed event;
     */
    private long                           elapsedMicroSeconds;
    // private long completionTime;
    /**
     * Number of database blocks obtained by O.S. read calls.
     * 
     * In trace file as "p".
     */
    private int                            physicalBlocksRead;
    /**
     * Number of blocks read from cache in consistent mode.
     * 
     * In trace file as "cr".
     */
    private int                            consistentReadBlocks;
    /**
     * Number of blocks retrieved from cache in current mode. In trace file as
     * "cu".
     */
    private int                            currentModeBlocks;
    /**
     * Number of misses on the library cache.
     */
    private int                            libraryCacheMissCount;
    /**
     * Number of rows returned. In trace file as "r".
     */
    private int                            rowCount;
    /**
     * Recursive depth. In trace file as "dep".
     */
    static {
        events.addEvent(LOG_AGGREGATE_CURSOR_OPERATION);
        events.addEvent(LOG_CURSOR_OPERATION_AGGREGATE);
    }

    private transient Tracer tracer;

    public CursorOperationAggregation() {

    }

    public CursorOperationAggregation(Tracer tracer) {
        this.tracer = tracer;
    }

    /**
     * Total cpu consumption for call. Represented in trace file as c=%d c CPU
     * time (100th's of a second in Oracle7 ,8 and 9).
     */
    public void aggregate(final CursorOperation o) {
        StringBuffer sb = null;
        if (recordType == null) {
            recordType = o.getRecordType();
        } else {
            if (!recordType.equals(o.getRecordType())) {
                throw new IllegalStateException("mismatch on record type");
            }
        }
        if (events.exists(LOG_AGGREGATE_CURSOR_OPERATION)) {
            sb = new StringBuffer();
            sb.append("aggregate CursorOperation before " + newline + toString());
            sb.append("operation: " + o.toString());
        }
        tracer.traceln("input to aggregation: " + o.toString());
        tracer.traceln("before aggregation " + toString());
        eventCount++;
        cpu += o.getCpu();
        elapsedMicroSeconds += o.getElapsedMicroSeconds();
        physicalBlocksRead += o.getPhysicalBlocksRead();
        consistentReadBlocks += o.getConsistentReadBlocks();
        currentModeBlocks += o.getCurrentModeBlocks();
        libraryCacheMissCount += o.getLibraryCacheMissCount();
        setMinimumTime(o.getTime());
        setMaximumTime(o.getTime());
        rowCount += o.getRowCount();
        if (events.exists(LOG_AGGREGATE_CURSOR_OPERATION)) {
            sb.append("after: " + toString());
            logger.debug(sb.toString());
        }
        tracer.traceln("after aggregation " + toString());
    }

    /**
     * CursorOperationAggregation o may be null in which case this silently
     * ignores the input.
     * 
     * @param o
     */
    public void aggregate(final CursorOperationAggregation o) {
        if (o != null) {
            tracer.traceln("CursorOperationAggegation " + o.toString());
            tracer.traceln("before aggregation " + toString());
            if (!(o == null)) {
                eventCount++;
                cpu += o.getCpu();
                elapsedMicroSeconds += o.getElapsedMicroSeconds();
                physicalBlocksRead += o.getPhysicalBlocksRead();
                consistentReadBlocks += o.getConsistentReadBlocks();
                currentModeBlocks += o.getCurrentModeBlocks();
                libraryCacheMissCount += o.getLibraryCacheMissCount();
                setMinimumTime(o.getMinimumTime()); // TODO must see if less
                setMaximumTime(o.getMaximumTime());
                rowCount += o.getRowCount();
            }

            tracer.traceln("after aggregation:  " + toString());
        }
    }

    public int getConsistentReadBlocks() {
        return consistentReadBlocks;
    }

    public int getCpu() {
        return cpu;
    }

    public int getCurrentModeBlocks() {
        return currentModeBlocks;
    }

    public long getElapsedMicroSeconds() {
        return elapsedMicroSeconds;
    }

    public int getEventCount() {
        return eventCount;
    }

    public int getLibraryCacheMissCount() {
        return libraryCacheMissCount;
    }

    public long getMaximumTime() {
        return maximumTime;
    }

    public long getMinimumTime() {
        return minimumTime;
    }

    public int getPhysicalBlocksRead() {
        return physicalBlocksRead;
    }

    public int getRowCount() {
        return rowCount;
    }

    private void setMaximumTime(final long time) {
        if (time > maximumTime) {
            maximumTime = time;
        }
    }

    private void setMinimumTime(final long time) {
        if (minimumTime == 0 || time < minimumTime) {
            minimumTime = time;
        }
    }

    public void setTracer(Tracer tracer) {
        this.tracer = tracer;

    }
}
