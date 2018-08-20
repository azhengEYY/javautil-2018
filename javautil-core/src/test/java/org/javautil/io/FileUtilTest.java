package org.javautil.io;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class FileUtilTest {

	@Test
	public void test1() {

		assertEquals("input.trc", FileUtil.basename("/tmp/input.trc"));
		assertEquals("input.trc", FileUtil.basename("input.trc"));
		assertEquals("input", FileUtil.basename("/tmp/input.trc", ".trc"));
		assertEquals("input", FileUtil.basename("input.trc", ".trc"));
	}
}
