package org.javautil.sql;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.javautil.util.ListOfNameValue;
import org.javautil.util.NameValue;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SqlStatementTest {

	static final Logger logger = LoggerFactory.getLogger(SqlStatementTest.class);
	static boolean setup = false;
	DataSource dataSource = DataSourceFactory.getInMemoryDataSource();
	Connection connection;


	@Before
	public void before() throws IOException, SQLException, SqlSplitterException {
		dataSource = DataSourceFactory.getInMemoryDataSource();
		connection = dataSource.getConnection();
		createSeededDatabase();
		logger.debug("**********Tables exist? " + doTablesExist());
	}

	boolean doTablesExist() throws SQLException {
		boolean tableExists = false;
		final SqlStatement ss = new SqlStatement(connection, "select count(*) from org");
		try {
			ss.getListOfLists(new Binds());
			tableExists = true;
			logger.warn("***** table exists");
		} catch (final SQLException sqe) {
		}
		return tableExists;
	}

	public void createSeededDatabase() throws IOException, SQLException, SqlSplitterException {
		if (!doTablesExist()) { // TODO I should not have to do this, this should be refreshed
			logger.debug("****creating database");
			runner("h2/ut_condition_tables.sr.sql");
			runner("h2/sales_reporting_ddl.sr.sql");
			seedDatabase(connection);
		}
	}

	@Test
	public void toQuestionTest() {
		final String sql = "insert into org (org_cd, org_nm)  values (%(ORG_CD)s, %(ORG_NM)s)";
		final String expected = "insert into org (org_cd, org_nm)  values (?, ?)";
		final List<String> binds = SqlStatement.findPythonBinds(sql);
		assertNotNull(binds);
		assertEquals(2, binds.size());
		assertTrue(binds.contains("ORG_CD"));
		assertTrue(binds.contains("ORG_NM"));
		SqlStatement ss = new SqlStatement(sql);
		final String result = ss.toQuestionBinds(sql);
		assertEquals(expected, result);
		logger.debug("result is: " + result);
	}

	@Test
	public void toQuestionTestInsert() throws SQLException {
		final SqlStatement createTable = new SqlStatement(connection,
				"create table xxx_org (\n" + "    org_id bigint auto_increment primary key not null,\n"
						+ "    org_cd varchar(16),\n" + "    org_nm varchar(32)\n" + ")");
		createTable.execute(connection);
		assertNotNull(connection);
		final String sql = "insert into xxx_org (org_cd, org_nm)  values (%(ORG_CD)s, %(ORG_NM)s)";
		// String sql = "select org_cd, org_nm from org where org_cd = $(ORG_CD)s";
		final SqlStatement stmt = new SqlStatement(sql);
		stmt.setConnection(connection);
		SqlStatement ss = new SqlStatement();
		final String questionSql = ss.toQuestionBinds(sql);
		logger.debug("questionSql: " + questionSql);
		final String expected = "insert into xxx_org (org_cd, org_nm)  values (?, ?)";
		assertEquals(expected, questionSql);
		final Binds binds = new Binds();
		binds.put("ORG_CD", "F-L");
		binds.put("ORG_NM", "Frito-Lay");
		stmt.bind(binds);
		stmt.executeUpdate(binds);
		//
		final SqlStatement selectStatement = new SqlStatement(connection, "select * from xxx_org");
		final ListOfNameValue list = selectStatement.getListOfNameValue(new Binds(), true);
		assertEquals(1, list.size());
		final NameValue nv = list.get(0);
		assertEquals(binds.get("ORG_CD"), nv.get("org_cd"));
	}


	void runner(String ddlResourceName) throws SQLException, IOException, SqlSplitterException {
		// InputStream ddlFile = ResourceHelper.getResourceAsInputStream(this, ddlResourceName);
		logger.debug("about to create sales tables " + ddlResourceName);
		if (connection == null) {
			throw new IllegalStateException("connection is null");
		}
		final SqlRunner runner = new SqlRunner(this,ddlResourceName).setCommit(false).setConnection(connection);
		runner.process();
	}

	public void seedDatabase(Connection connection) throws SQLException, IOException, SqlSplitterException {
		final SeedSalesReportingDatabase seeder = new SeedSalesReportingDatabase(connection);
		seeder.process(true);
	}

	@Test
	public void test() throws SQLException, IOException, SqlSplitterException {
		createSeededDatabase();
		final SqlStatement orgStatement = new SqlStatement("select * from org order by org_cd");
		orgStatement.setConnection(connection);
		final ListOfNameValue rows = orgStatement.getListOfNameValue(new Binds());
		assertEquals(18, rows.size());
		final NameValue row = rows.get(0);
		logger.debug("row: " + row);
		assertEquals("CADBURY", row.getString("org_cd"));
	}
	
	@Test
	public void toQuestionBindsTest() {
		String input = "--@name wtf\ncreate sequence woop";
		SqlStatement ss = new SqlStatement(input);
		ss.setTrace(true);
		String output = ss.toQuestionBinds(input);
		assertEquals(input,output);
		
	}
}
