package org.javautil.io;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResourceHelperTest {

	private final Logger log = LoggerFactory.getLogger(ResourceHelperTest.class);


	@Test
	public void testGetResourceAsFile2() throws IOException {
		final File f = ResourceHelper.getResourceAsFile(this, "application.properties");
		assertTrue(f.exists());
		log.debug("f is: " + f.getCanonicalPath());
	}

	@Test
	public void testSalesTables() throws IOException {
		final File f = ResourceHelper.getResourceAsFile(this, "h2/sales_reporting_ddl.sr.sql");
		assertTrue(f.exists());
		assertTrue(f.getTotalSpace() > 0l);
		log.debug("f is: " + f.getCanonicalPath());
	}
}
