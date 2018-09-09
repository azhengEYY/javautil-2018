package com.pacificdataservices.pdssr;

import static org.junit.Assert.assertNotNull;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;

import org.junit.Test;

public class CdsReportingFileFormatTest {

    @Test
    public void test() throws FileNotFoundException {
        final CdsReportingFileMetadata format = new CdsReportingFileMetadata();
        final Map<String, List<Map<String, Object>>> defs = format.getRecordDefs();
        assertNotNull(defs);
    }
}
