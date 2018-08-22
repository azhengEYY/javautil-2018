package org.javautil.sql;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NamedSqlStatementTest {

	Logger logger = LoggerFactory.getLogger(getClass());

	@Test
	public void testAnnotatedName() throws IOException, SqlSplitterException {
		final SqlSplitter sr = new SqlSplitter(this,"testsr/etl_persistence.named.sr.sql");
		sr.setTraceState(0);
		sr.analyze();
		final SqlStatements ss= sr.getSqlStatements();

		final NamedSqlStatements named = new NamedSqlStatements(ss);
		//System.out.println(named.toString());
		final SqlStatement custInsert = named.getSqlStatement("etl_customer_tot_insert");
		assertNotNull(custInsert);
		final String custInsertSql = custInsert.getSql();
		final int index = custInsertSql.indexOf(":ETL_FILE_ID,  :LINE_NUMBER, :CUSTOMER_COUNT");
		assertTrue(index > -1);

	}


	@Test
	public void testAnnotatedNameShort() throws IOException, SqlSplitterException {
		String resourceName = "testsr/etl_persistence.named.sr.sql";
		final NamedSqlStatements named = NamedSqlStatements.getNameSqlStatementsFromSqlSplitterResource(this,resourceName);
		final SqlStatement custTotInsert = named.getSqlStatement("etl_customer_tot_insert");
		assertNotNull(custTotInsert);
		final String custTotInsertSql = custTotInsert.getSql();
		final int index = custTotInsertSql.indexOf(":ETL_FILE_ID,  :LINE_NUMBER, :CUSTOMER_COUNT");
		assertTrue(index > -1);
		assertEquals(9,named.size());

	}
}
