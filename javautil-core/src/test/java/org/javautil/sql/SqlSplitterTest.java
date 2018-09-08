package org.javautil.sql;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class SqlSplitterTest {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	public void testStripAnnotations() {
		String sql = "--@name wtf\ncreate sequence toad";
		String stripped = SqlSplitter.stripAnnotations(sql);
		assertEquals("create sequence toad",stripped);
	}

	// @Test
	public void testUtCondition() throws IOException, SqlSplitterException {
		final SqlSplitter sr = new SqlSplitter(this, "testsr/ut_condition_tables.sr.sql").setVerbosity(9);
		//System.out.println(sr.formatLines());
		logger.info("getSqlTexts");
		final ArrayList<String> sqls = sr.getSqlTexts();
		final String expected = "create sequence ut_condition_id_seq";
		final String actual = sqls.get(1);
//		logger.info("expected: {}", expected);
//		logger.info("actual    {}", actual);
		assertEquals(expected, actual);
		int stmtNbr = 1;
		if (logger.isDebugEnabled()) {
			for (final String sql1 : sqls) {
				logger.debug("sql {}:\n{}", stmtNbr++, sql1);
			}
		}

		assertEquals(7, sqls.size());

	}

	
	//@Test
	public void testNamed() throws IOException, SqlSplitterException {
		final SqlSplitter sr = new SqlSplitter(this, "condition_identification/UtConditionPersistenceDml.sr.sql").setVerbosity(0);
		// sr.formatLines();
		final ArrayList<String> sqls = sr.getSqlTexts();
		assertEquals(10, sqls.size());
		// TODO test the statements, line counts, first and last line
	}
	  
}
