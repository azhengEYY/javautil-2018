package com.pacificdataservices.pdssr;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.javautil.io.ResourceHelper;
import org.yaml.snakeyaml.Yaml;

public class CdsReportingFileMetadata {

    // private final Logger logger = Logger.getLogger(getClass());

    public Map<String, List<Map<String, Object>>> getRecordDefs() throws FileNotFoundException {
        final Yaml yaml = new Yaml();
        final String resourceName = "pdssr/cds_reporting_metadata.yaml";
        InputStream recordDefsStream = null;

        recordDefsStream = ResourceHelper.getResourceAsInputStream(this, resourceName);

        @SuppressWarnings("unchecked")
        final Map<String, List<Map<String, Object>>> recordDefs = (Map<String, List<Map<String, Object>>>) yaml
                .load(recordDefsStream);
        return recordDefs;
    }
}
