package org.javautil.sql;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;

import org.javautil.text.StringUtil;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SqlSplitterUtConditionTest {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Test
	public void testUtCondition() throws IOException, SqlSplitterException {
		String splitterResource = "condition_identification/ut_condition_tables.sr.sql";
		
		final SqlSplitter sr = new SqlSplitter(this,splitterResource).setVerbosity(0);
		logger.info("getSqlTexts");
		final ArrayList<String> sqls = sr.getSqlTexts();
		String[] firstLines = StringUtil.getLines(sqls.get(0));
		assertEquals("--@name create ut_condition_run_id_seq",firstLines[0]);

		assertEquals(7, sqls.size());
		logger.info("done");
	}

	

}
