package org.javautil.text;

import static org.junit.Assert.*;

import java.io.IOException;

import org.javautil.util.ListOfNameValue;
import org.javautil.util.NameValue;
import org.junit.Test;

public class CSVReaderTest {

    @Test
    public void mfr() throws IOException {
        CsvReader csvr = new CsvReader("src/test/resources/csvdata/manufacturers.csv");
          csvr.setHasHeader(true);
        ListOfNameValue lnv = csvr.readLinesAsListOfNameStringValue();
        assertNotNull(lnv);
        assertEquals(16,lnv.size());
        System.out.println(lnv);
    }
    
    @Test
    public void mfr2() throws IOException {
        ListOfNameValue lnv = new CsvReader("src/test/resources/csvdata/manufacturers.csv").setHasHeader(true).readLinesAsListOfNameStringValue();
        
        assertNotNull(lnv);
        assertEquals(16,lnv.size());
        NameValue nv = lnv.get(0);
        assertEquals("0000000020",nv.get("mfr_id"));
        assertEquals("F-L",nv.get("mfr_cd"));
        assertEquals("Frito-Lay",nv.get("mfr_name"));

     
    }
}
