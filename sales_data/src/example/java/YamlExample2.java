package com.pacificdataservices.pdssr;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.List;

import org.yaml.snakeyaml.Yaml;

class YamlExample2 {

	public static void main(String[] args) throws FileNotFoundException {
		// The path of your YAML file.
		final String fileName = "src/test/resources/cds_reporting_metadata.yaml";
		ArrayList<String> key = new ArrayList<String>();
		ArrayList<String> value = new ArrayList<String>();
		Map<String, List<Map<String, Object>>> recordDefs = null;
		Yaml yaml = new Yaml();

		InputStream ios = new FileInputStream(new File(fileName));

		recordDefs = (Map<String, List<Map<String, Object>>>) yaml.load(ios);

		for (Entry recordDef : recordDefs.entrySet()) {
			System.out.println(recordDef.getKey());
			List<Map<String, Object>> fieldsDef = (List<Map<String, Object>>) recordDef.getValue();
			for (Map<String, Object> fieldDef : fieldsDef) {
				for (Entry fieldAttr : fieldDef.entrySet())
					System.out.println(fieldAttr.getKey() + " " + fieldAttr.getValue());
			}
		}
	}
}