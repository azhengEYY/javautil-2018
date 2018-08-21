package org.javautil.sql;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;

import org.javautil.text.StringUtil;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO ensure works with no databases up
public class SqlSplitterUtConditionTest {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Test
	public void testUtCondition() throws IOException, SqlSplitterException {
		String splitterResource = "condition_identification/ut_condition_tables.sr.sql";
		
		final SqlSplitter sr = new SqlSplitter(this,splitterResource).setVerbosity(0);
		//System.out.println(sr.formatLines());
		logger.info("getSqlTexts");
		final ArrayList<String> sqls = sr.getSqlTexts();
		String[] firstLines = StringUtil.getLines(sqls.get(0));
		assertEquals("--@name create ut_condition_run_id_seq",firstLines[0]);
		final String actual = sqls.get(0);

		assertEquals(7, sqls.size());
		logger.info("done");

	}

	@Test (expected = SqlSplitterException.class)
	public void testUtConditionBad() throws IOException, SqlSplitterException {
		String splitterResource = "testsr/ut_condition_tables_error_no_statement_end.sr.sql";
	
		new SqlSplitter(this,splitterResource).setVerbosity(0);
	}
	

}
