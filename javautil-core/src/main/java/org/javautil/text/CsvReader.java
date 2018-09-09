package org.javautil.text;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.javautil.dataset.DataType;
import org.javautil.util.ListOfNameValue;
import org.javautil.util.NameValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CsvReader implements Closeable {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private final BufferedReader           reader;

    private final CSVTokenizer             tokenizer = new CSVTokenizer();

    private List<DataType>                 datatypes;

    private boolean                        hasHeader;

    private LinkedHashMap<String, Integer> headerColumns;
    private List<String>                   headerColumnNames;

    public CsvReader(final String filename) throws FileNotFoundException {
        if (filename == null) {
            throw new IllegalArgumentException("filename is null");
        }
        final FileInputStream fis = new FileInputStream(filename);
        final InputStreamReader sr = new InputStreamReader(fis);
        reader = new BufferedReader(sr);
    }

    public CsvReader(final InputStream is) {
        if (is == null) {
            throw new IllegalArgumentException("is is null");
        }
        final InputStreamReader sr = new InputStreamReader(is);
        reader = new BufferedReader(sr);
    }

    public void createHeader() throws IOException {
        final String line = reader.readLine();
        List<String> retval = null;
        if (line != null) {
            headerColumnNames = tokenizer.parse(line);
            int colIndex = 0;
            headerColumns = new LinkedHashMap<String, Integer>();
            for (String columnName : headerColumnNames) {
                Integer oldIndex = headerColumns.put(columnName, colIndex);
                if (oldIndex != null) {
                    String message = String.format("column %d and %d are both named %s", colIndex, oldIndex,
                            columnName);
                    throw new IllegalArgumentException(message);
                }
                colIndex++;
            }
        }
    }

    public List<String> readLine() throws IOException {
        if (hasHeader && headerColumns == null) {
            createHeader();
        }
        final String line = reader.readLine();
        List<String> retval = null;
        if (line != null) {
            retval = tokenizer.parse(line);
        }
        return retval;
    }

    public ListOfNameValue readLinesAsListOfNameStringValue() throws IOException {
        ListOfNameValue retval = new ListOfNameValue();
        NameValue nv;
        while ((nv = readLineAsNameStringValue()) != null) {
            retval.add(nv);
        }
        reader.close();
        return retval;

    }

    public NameValue readLineAsNameStringValue() throws IOException {
        if (!hasHeader) {
            throw new IllegalArgumentException("no header");
        }
        final List<String> stringValues = readLine();
        if (logger.isDebugEnabled()) {
        logger.debug("stringValues: " + stringValues);
        }
        NameValue nv = null;
        if (stringValues != null) {
            nv = new NameValue();
            int colIndex = 0;
            for (String value : stringValues) {
                nv.put(headerColumnNames.get(colIndex), value);
                colIndex++;
            }
        }

        return nv;
    }

    // TODO document
    public List<Object> readLineAsObjects() throws IOException {
        if (datatypes == null) {
            throw new IllegalStateException("must be preceded by call to setDatatypes");
        }
        final List<String> stringValues = readLine();
        List<Object> values = null;
        if (stringValues != null) {
            values = new ArrayList<Object>(stringValues.size());
            for (int i = 0; i < stringValues.size(); i++) {
                final String s = stringValues.get(i);
                values.add(datatypes.get(i).coerceString(s));
            }
        } else {
            reader.close();
        }
        return values;
    }

    /**
     * @return the datatypes
     */
    public List<DataType> getDatatypes() {
        return datatypes;
    }

    public CsvReader setHasHeader(boolean hasHeader) {
        this.hasHeader = hasHeader;
        return this;
    }

    /**
     * @param datatypes
     *            the datatypes to set
     */
    public void setDatatypes(final List<DataType> datatypes) {
        this.datatypes = datatypes;
    }

    public void close() throws IOException {
        reader.close();
    }
}
