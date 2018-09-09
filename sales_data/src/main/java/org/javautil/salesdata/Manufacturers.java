package org.javautil.salesdata;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.javautil.sql.Binds;
import org.javautil.util.ListOfNameValue;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvParser;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;

// https://github.com/FasterXML/jackson-dataformats-text/tree/master/csv
public class Manufacturers {
    private static final String fileName= "src/main/resources/pdssr/manufacturers.csv";

    ListOfNameValue getManufacturers() throws IOException {
        ListOfNameValue lnv = new ListOfNameValue();
        File csvFile = new File(fileName);
        CsvMapper mapper = new CsvMapper();
        
        CsvSchema schema = CsvSchema.emptySchema().withHeader(); // use first row as header; otherwise defaults are fine
        MappingIterator<Map<String,String>> it = mapper.readerFor(Map.class)
           .with(schema)
           .readValues(csvFile);
        while (it.hasNext()) {
          Map<String,String> rowAsMap = it.next();
          System.out.println(rowAsMap);
        }

        return lnv;
        
    }

}
