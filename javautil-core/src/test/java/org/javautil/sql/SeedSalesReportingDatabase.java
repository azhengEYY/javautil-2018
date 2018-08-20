package org.javautil.sql;


import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SeedSalesReportingDatabase {
	ArrayList<String[]> mfrs = new ArrayList<>();
	private final Connection connection;
	private final Logger logger = LoggerFactory.getLogger(getClass());
	// %(ORG_NM)s)");
	private SqlStatement orgStmt; // = new SqlStatement("insert into org (org_cd, org_nm) values (%(ORG_CD)s,
	private SqlStatement mfrStmt;
	private SqlStatement orgDistrib;
	private SqlStatement orgDatafeed;

	public SeedSalesReportingDatabase(Connection connection) throws SQLException {
		this.connection = connection;

		mfrs.add(new String[]{"0000000020", "F-L", "Frito-Lay"});
		mfrs.add(new String[]{"0000000030", "GM", "General Mills"});
		mfrs.add(new String[]{"0000000040", "HVEND", "Hershey Vending"});
		mfrs.add(new String[]{"0000000050", "HFUND", "Hershey Fund Raising"});
		mfrs.add(new String[]{"0000000055", "HCONC", "Hershey Concession"});
		mfrs.add(new String[]{"0000000060", "SNYDERS", "Snyder's of Hanover"});
		mfrs.add(new String[]{"0000000080", "KELLOGG", "Kellogg, Keebler"});
		mfrs.add(new String[]{"0000000115", "KARS", "Kar Nut Product (Kar's)"});
		mfrs.add(new String[]{"0000000135", "MARS", "Mars Chocolate "});
		mfrs.add(new String[]{"0000000145", "POORE", "Inventure Group (Poore Brothers)"});
		mfrs.add(new String[]{"0000000150", "WOW", "WOW Foods"});
		mfrs.add(new String[]{"0000000160", "CADBURY", "Cadbury Adam USA, LLC"});
		mfrs.add(new String[]{"0000000170", "MONOGRAM", "Monogram Food"});
		mfrs.add(new String[]{"0000000185", "JUSTBORN", "Just Born"});
		mfrs.add(new String[]{"0000000190", "HOSTESS", "Hostess, Dolly Madison"});
		mfrs.add(new String[]{"0000000210", "SARALEE", "Sara Lee"});
		mfrs.add(new String[]{"0000000005", "ExoticTx", "Exotic Texas"});
		setSqlStatementsOracle(connection);
	}

	void seedManufacturers() throws SQLException {

		for (final String[] mfr : mfrs) {
			final Binds binds = new Binds();
			binds.put("CDS_MFR_ID", mfr[0]);
			binds.put("ORG_CD", mfr[1]);
			binds.put("ORG_NM", mfr[2]);
			orgStmt.executeUpdate(binds);
			// orgStmt.executeUpdate(connection, binds);
			// mfrStmt.executeUpdate(connection, binds);
			mfrStmt.useColonBind().executeUpdate(binds);
		}
	}

	void seedDistributor() throws SQLException {
		final Binds binds = new Binds();
		binds.put("ORG_CD", "EXOTICTX");
		binds.put("ORG_NM", "Texas Exotic Foods");
		binds.put("DISTRIB_ID", "0000000001");
		orgStmt.executeUpdate(connection, binds);
		orgDistrib.executeUpdate(connection, binds);
		orgDatafeed.executeUpdate(connection, binds);
	}

	InputStream getResource(Class<?> callingClass, String resourceName) {
		logger.info("resourceName: " + resourceName);
		final URL url = callingClass.getResource(resourceName);
		logger.info("resourceName " + resourceName + " url " + url);
		final InputStream is = this.getClass().getResourceAsStream(resourceName);
		if (is == null) {
			throw new IllegalStateException("resource " + resourceName + " failed to load");

		}
		return is;
	}

	public void seedProduct() throws SQLException, IOException, SqlSplitterException {
		SqlRunner runner;

		logger.info("about to populate product table");
		runner = new SqlRunner(this, "h2/pop_product.sql").setConnection(connection).setPrintSql(false).setCommit(false).setContinueOnError(false);
		runner.process();

	}

	public void process(boolean commit) throws SQLException, IOException, SqlSplitterException {
		seedManufacturers();
		if (commit) {
			connection.commit(); // TOOD hack while debugging
		}
		seedDistributor();
		if (commit) {
			connection.commit(); // TOOD hack while debugging
		}
		seedProduct();
		if (commit) {
			connection.commit(); // TOOD hack while debugging
		}
	}


	void setSqlStatementsOracle(Connection connection) throws SQLException {

		orgStmt = new SqlStatement(
				"insert into org (org_cd, org_nm) " + " values (%(ORG_CD)s, %(ORG_NM)s)",
				connection);

		mfrStmt = new SqlStatement("insert into org_mfr( org_id,cds_mfr_id) " + "select org_id, %(CDS_MFR_ID)s "
				+ "from   org " + "where org_cd = %(ORG_CD)s", connection); // TODO externalize
		mfrStmt.useColonBind();

		orgDistrib = new SqlStatement("insert into org_distrib(org_id) " + "select org_id"
				+ " from org " + "where org_cd = %(ORG_CD)s", connection).useColonBind();

		orgDatafeed = new SqlStatement(
				"insert into org_datafeed(org_id) " + "select org_id " + "from org " + "where org_cd = %(ORG_CD)s",
				connection);
		orgDatafeed.useColonBind();
	}

}
