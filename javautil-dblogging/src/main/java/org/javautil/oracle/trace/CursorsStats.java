package org.javautil.oracle.trace;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;

import org.javautil.io.Tracer;
import org.javautil.oracle.trace.record.Close;
import org.javautil.oracle.trace.record.CursorOperation;
import org.javautil.oracle.trace.record.Parsing;
import org.javautil.oracle.trace.record.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Container for CursorInfo by cursor number and sqlId
 * 
 * @author jjs
 *
 */
public class CursorsStats {
    @SuppressWarnings("unused")
    private transient Logger                    logger              = LoggerFactory.getLogger(getClass());
    /**
     * Each parse produces a cursorNumber which may not be unique in a trace file.
     */
    private transient HashMap<Long, CursorInfo> cursorStatsByNumber = new HashMap<>();
    /**
     * The sqlId will be the same for all cursors that use the same sql
     * Therefore there will be at least as many cursorStatsByNumber, one for
     * each parse but only one by sqlID for the same sql text.
     * 
     * By keeping in a LinkedHashMap we can present in the order of occurrence.
     */
    private LinkedHashMap<String, CursorInfo>   cursorStatsById     = new LinkedHashMap<>();

    private transient HashMap<Long, String>     sqlIdByCursorNumber = new HashMap<>();

    private transient Tracer                    tracer;

    public HashMap<Long, CursorInfo> getCursorStatsByCursorStatsNumber() {
        return cursorStatsByNumber;
    }

    public Collection<CursorInfo> getAllCursorInfo() {
        return cursorStatsById.values();
    }

    public ArrayList<CursorInfo> getNonRecursiveCursorInfo() {
        ArrayList<CursorInfo> retval = new ArrayList<>();
        for (CursorInfo cs : getAllCursorInfo()) {
            Parsing parsing = cs.getParsing();
            if (parsing == null) {
                logger.warn("no parsing for " + cs.toString());
            } else {
                if (cs.getParsing().getRecursionDepth() == 0) {
                    retval.add(cs);
                }
            }
        }
        return retval;
    }

    public ArrayList<CursorInfo> getRecursiveCursorInfo() {
        ArrayList<CursorInfo> retval = new ArrayList<>();
        for (CursorInfo cs : getAllCursorInfo()) {
            Parsing parsing = cs.getParsing();
            if (parsing == null) {
                logger.warn("no parsing for " + cs.toString());
            } else {
                if (cs.getParsing().getUid() == 0) {
                    retval.add(cs);
                }
            }
        }
        return retval;
    }

    public ArrayList<CursorInfo> getCursorInfos(boolean includeSys) {
        ArrayList<CursorInfo> retval = new ArrayList<>();
        for (CursorInfo cs : getAllCursorInfo()) {
            Parsing parsing = cs.getParsing();
            if (parsing != null)
                if (includeSys || cs.getParsing().getUid() != 0) {
                    retval.add(cs);
                }
        }
        return retval;
    }

    public CursorInfo getCursorInfoByCursorNumber(Long id) {
        return cursorStatsByNumber.get(id);
    }

    public CursorInfo getCursorInfoByCursorId(String id) {
        return cursorStatsById.get(id);
    }

    public void handle(CursorOperation record) {
        String sqlId = sqlIdByCursorNumber.get(record.getCursorNumber());
        CursorInfo cursorStats = cursorStatsByNumber.get(record.getCursorNumber());
        if (cursorStats == null) {
            cursorStats = new CursorInfo(record);
            cursorStatsByNumber.put(record.getCursorNumber(), cursorStats);
        }
        // cursorStats.aggregate(record);
        //
        cursorStats = cursorStatsById.get(sqlId);
        if (cursorStats == null) {
            cursorStats = new CursorInfo(record);
            cursorStatsById.put(sqlId, cursorStats);
        }
        cursorStats.aggregate(record);
    }

    public void handle(Parsing record) {
        CursorInfo cursorStats = cursorStatsByNumber.get(record.getCursorNumber());
        cursorStats = new CursorInfo(record);
        cursorStats.setTracer(tracer);
        cursorStatsByNumber.put(record.getCursorNumber(), cursorStats);
        CursorInfo cursorStatsForSqlId = cursorStatsById.get(record.getSqlid());
        if (cursorStatsForSqlId == null) {
            cursorStatsById.put(record.getSqlid(), cursorStats);
            cursorStatsForSqlId = cursorStats;
        }
        sqlIdByCursorNumber.put(record.getCursorNumber(), record.getSqlid());
    }

    public void handle(Stat record) {
        CursorInfo cursorStats = cursorStatsByNumber.get(record.getCursorNumber());
        if (cursorStats == null) {
            cursorStats = new CursorInfo(record);
            cursorStatsByNumber.put(record.getCursorNumber(), cursorStats);
        }
        cursorStats.addStat(record);
    }

    /**
     * Returns this as a json String
     */
    @Override
    public String toString() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(this);
    }

    public void setTracer(Tracer tracer) {
        this.tracer = tracer;

    }

    public void handle(Close record) {
        CursorInfo cursorStats = cursorStatsByNumber.get(record.getCursorNumber());
        if (cursorStats == null) {
            cursorStats = new CursorInfo(record);
            cursorStatsByNumber.put(record.getCursorNumber(), cursorStats);
        }
        // cursorStats.aggregate(record);
        //
//        cursorStats = cursorStatsById.get(sqlId);
//        if (cursorStats == null) {
//            cursorStats = new CursorInfo(record);
//            cursorStatsById.put(sqlId, cursorStats);
//        }
//        cursorStats.aggregate(record);
        
    }

}
