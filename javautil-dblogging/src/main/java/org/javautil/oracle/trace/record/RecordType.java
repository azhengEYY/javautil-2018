package org.javautil.oracle.trace.record;

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO document

public enum RecordType {
    PARSING, PARSE, BIND, EXEC, WAIT, FETCH, STAT, END_OF_STATEMENT, IGNORED, TIMESTAMP, STARS, ERROR, SEPARATOR, XCTEND, APP_NAME, PARSE_ERROR, UNMAP, CLOSE, ACTION, MODULE, UNKNOWN, LOBREAD, LOBPGSIZE;

    @SuppressWarnings("unused")
    private static final Logger                logger  = LoggerFactory.getLogger(RecordType.class.getName());

    private static HashMap<String, RecordType> textMap = new HashMap<String, RecordType>();

    static {
        textMap.put("*** 20", TIMESTAMP);
        textMap.put("BINDS", BIND);
        textMap.put("CLOSE", CLOSE);
        textMap.put("ERROR", ERROR);
        textMap.put("EXEC", EXEC);
        // textMap.put("EXE", EXE);
        textMap.put("FETCH", FETCH);
        // textMap.put("PARSE", PARSE);
        textMap.put("PARSING", PARSING);
        textMap.put("STAT", STAT);
        textMap.put("WAIT", WAIT);
        textMap.put("XCTEND", XCTEND);
        textMap.put("LOBREAD:", LOBREAD);
        textMap.put("LOBPGSIZE:", LOBPGSIZE);

    }

    public static RecordType getRecordType(final String record) {
        RecordType returnValue = null;
        int firstSpace = record.indexOf(" ");
        if (firstSpace > -1) {
            String firstWord = record.substring(0, firstSpace);
            returnValue = textMap.get(firstWord);
        }
  //      logger.info(String.format("%s -- %s", returnValue, record));
        if (returnValue == null) {
            // if (record.startsWith("XCTEND")) {
            // returnValue = XCTEND;
            if (record.startsWith("PARSE ERROR")) {
                returnValue = PARSE_ERROR;
            } else if (record.startsWith("PARSE")) {
                returnValue = PARSE;
                // } else if (record.startsWith("PARSING")) {
                // returnValue = PARSING;
                // } else if (record.startsWith("EXEC")) {
                // returnValue = EXEC;
                // } else if (record.startsWith("FETCH")) {
                // returnValue = FETCH;
            } else if (record.startsWith("*** ACTION NAME:")) {
                returnValue = ACTION;
            } else if (record.startsWith("*** MODULE NAME:(")) {
                returnValue = MODULE;
                // } else if (record.startsWith("CLOSE")) {
                // returnValue = CLOSE;
                // } else if (record.startsWith("ERROR")) {
                // returnValue = ERROR;
                // } else if (record.startsWith("STAT")) {
                // returnValue = STAT;
            } else if (record.startsWith("***")) {
                if (record.startsWith("*** 20")) {
                    returnValue = TIMESTAMP;
                } else {
                    returnValue = STARS;
                }
            } else if (record.startsWith("===")) {
                returnValue = SEPARATOR;
            } else if (record.trim().length() > 0) {
                returnValue = UNKNOWN;
            }
        }
        if (returnValue == null) {
            returnValue = IGNORED;
    //        logger.warn(String.format("%s -- %s", returnValue, record));
        }
        return returnValue;
    }
}
