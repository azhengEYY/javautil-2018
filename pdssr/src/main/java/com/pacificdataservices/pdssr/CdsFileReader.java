package com.pacificdataservices.pdssr;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.javautil.sql.Binds;

class CdsFileReader {
    private String                           filename;

    private Map<String, List<Map<String, Object>>> record_types;
    static final String                            NUMERIC    = "NUMERIC";
    static final String                            INTEGER    = "INTEGER";
    static final String                            DECIMAL    = "DECIMAL";
    static final String                            DATE       = "DATE";
    static final String                            TEXT       = "TEXT";
    static final String                            LITERAL    = "LITERAL";
    static final String                            DIGITS     = "DIGITS";
    private Map<String, List<Map<String, Object>>> recordDefs;
    private BufferedReader                         reader;

    FixedRecordUtil                                fru        = new FixedRecordUtil();

    private String                                 inputLine;
    private String                                 recordType;
    private List<Map<String, Object>>              recordDefinition;

    private int                                    lineNumber = 0;
    // private final Logger logger = LoggerFactory.getLogger(getClass());
    
    CdsFileReader(InputStream is) {
        try {
            InputStreamReader isr = new InputStreamReader(is);
            reader = new BufferedReader(isr);
            loadRecordDefinitions();
        } catch (final FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    CdsFileReader(String filename) {
        this.filename = filename;

        FileReader fr;
        try {
            fr = new FileReader(this.filename);
            reader = new BufferedReader(fr);
            loadRecordDefinitions();
        } catch (final FileNotFoundException e) {
            throw new RuntimeException(e);
        }

    }

    void loadRecordDefinitions() throws FileNotFoundException {
        recordDefs = new CdsReportingFileMetadata().getRecordDefs();

        // recordDefs = CdsReportingFileMetadata.getRecordDefs();
        record_types = new HashMap<>();
        record_types.put("CD", recordDefs.get("customer"));
        record_types.put("CT", recordDefs.get("customer_total"));
        record_types.put("IR", recordDefs.get("inventory"));
        record_types.put("IT", recordDefs.get("inventory_total"));
        record_types.put("SA", recordDefs.get("sales"));
        record_types.put("AT", recordDefs.get("sales_total"));
    }

    String inputLine() throws IOException {
        do {
            inputLine = reader.readLine();
            lineNumber += 1;
        } while ((inputLine != null) && inputLine.startsWith("#"));
        if (inputLine != null) {
            recordType = inputLine.substring(168, 170); // line[168:170] TODO
            recordDefinition = record_types.get(recordType);
        }
        return inputLine;
    }

    List<Object> readLineAsObjectList() throws ParseException, IOException {
        inputLine = inputLine();
        List<Object> retval = null;
        if (inputLine != null) {
            retval = fru.getObjectList(recordDefinition, inputLine);
        }
        return retval;
    }

    Binds readLine() throws ParseException, IOException {
        inputLine = inputLine();
        Map<String, Object> retval = null;
        Binds binds = null;
        if (inputLine != null) {
            retval = fru.getBindMap(recordDefinition, inputLine);
            binds = new Binds(retval);
        }

        return binds;
    }

    void close() throws IOException {
        this.reader.close();
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public String getRecordType() {
        return recordType;
    }
}
