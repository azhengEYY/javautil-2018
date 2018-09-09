package com.pacificdataservices.pdssr;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

import org.javautil.sql.Binds;
import org.javautil.sql.Dialect;
import org.javautil.sql.SqlRunner;
import org.javautil.sql.SqlSplitterException;
import org.javautil.sql.SqlStatement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SeedSalesReportingDatabase {
    private final Connection connection;

    private final Logger     logger = LoggerFactory.getLogger(getClass());

    private SqlStatement     orgStmt;                                     // = new SqlStatement("insert into org
                                                                          // (org_cd, org_nm) values (%(ORG_CD)s,
    // %(ORG_NM)s)");

    private SqlStatement     mfrStmt;

    private SqlStatement     orgDistrib;

    private SqlStatement     orgDatafeed;

    ArrayList<String[]>      mfrs   = new ArrayList<>();

    public SeedSalesReportingDatabase(Connection connection) throws SQLException {
        this.connection = connection;

        mfrs.add(new String[] { "0000000020", "F-L", "Frito-Lay" });
        mfrs.add(new String[] { "0000000030", "GM", "General Mills" });
        mfrs.add(new String[] { "0000000040", "HVEND", "Hershey Vending" });
        mfrs.add(new String[] { "0000000050", "HFUND", "Hershey Fund Raising" });
        mfrs.add(new String[] { "0000000055", "HCONC", "Hershey Concession" });
        mfrs.add(new String[] { "0000000060", "SNYDERS", "Snyder's of Hanover" });
        mfrs.add(new String[] { "0000000080", "KELLOGG", "Kellogg, Keebler" });
        mfrs.add(new String[] { "0000000115", "KARS", "Kar Nut Product (Kar's)" });
        mfrs.add(new String[] { "0000000135", "MARS", "Mars Chocolate " });
        mfrs.add(new String[] { "0000000145", "POORE", "Inventure Group (Poore Brothers)" });
        mfrs.add(new String[] { "0000000150", "WOW", "WOW Foods" });
        mfrs.add(new String[] { "0000000160", "CADBURY", "Cadbury Adam USA, LLC" });
        mfrs.add(new String[] { "0000000170", "MONOGRAM", "Monogram Food" });
        mfrs.add(new String[] { "0000000185", "JUSTBORN", "Just Born" });
        mfrs.add(new String[] { "0000000190", "HOSTESS", "Hostess, Dolly Madison" });
        mfrs.add(new String[] { "0000000210", "SARALEE", "Sara Lee" });
        mfrs.add(new String[] { "0000000005", "ExoticTx", "Exotic Texas" });
        // TODO externalize and locate based on Dialect
        switch (Dialect.getDialect(connection)) {
        case ORACLE:
            setSqlStatementsOracle(connection);
            break;
        case H2:
            setSqlStatementsRational(connection);
            break;
        default:
            throw new IllegalArgumentException("unsupported Dialect");
        }
    }

    void seedManufacturers() throws SQLException, IOException {

        for (final String[] mfr : mfrs) {
            // TODO write a zip
            final Binds binds = new Binds();
            binds.put("CDS_MFR_ID", mfr[0]);
            binds.put("ORG_CD", mfr[1]);
            binds.put("ORG_NM", mfr[2]);
            orgStmt.useColonBind().executeUpdate(binds);
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

    // InputStream getResource(Class callingClass, String resourceName) {
    // logger.info("resourceName: " + resourceName);
    // URL url = callingClass.getResource(resourceName);
    // logger.info("resourceName " + resourceName + " url " + url);
    // InputStream is = this.getClass().getResourceAsStream(resourceName);
    // if (is == null) {
    // throw new IllegalStateException("resource " + resourceName + " failed to
    // load");
    //
    // }
    // return is;
    // }

    public void seedProduct() throws SQLException, IOException, SqlSplitterException {
        logger.info("about to populate product table");
        final SqlRunner runner = new SqlRunner(this, "pdssr/pop_product.sql").setConnection(connection);
        runner.process();
    }

    public void process(boolean commit) throws SQLException, IOException, SqlSplitterException {
        seedManufacturers();
        seedDistributor();
        seedProduct();
    }

    void setSqlStatementsRational(Connection connection) throws SQLException {

        orgStmt = new SqlStatement(connection, "insert into org (org_cd, org_nm) values (%(ORG_CD)s, %(ORG_NM)s)");

        mfrStmt = new SqlStatement(connection,
                "insert into org_mfr( org_id,cds_mfr_id) " + "select org_id, %(CDS_MFR_ID)s "
                        + "from   org " + "where org_cd = %(ORG_CD)s"); // TODO externalize

        orgDistrib = new SqlStatement(connection,
                "insert into org_distrib(org_id,distrib_id) " + "select org_id, %(DISTRIB_ID)s "
                        + "from org " + "where org_cd = %(ORG_CD)s");

        orgDatafeed = new SqlStatement(connection,
                "insert into org_datafeed(org_id) " + "select org_id " + "from org " + "where org_cd = %(ORG_CD)s");
    }

    // TODO this is copy paste and most are the same just get the sequence and
    // dispose of the differences
    void setSqlStatementsOracle(Connection connection) throws SQLException {

        orgStmt = new SqlStatement(
                "insert into org (org_id, org_cd, org_nm) " + " values (org_id_seq.nextval,:ORG_CD, :ORG_NM)",
                connection);

        mfrStmt = new SqlStatement("insert into org_mfr( org_id,cds_mfr_id) " + "select org_id, %(CDS_MFR_ID)s "
                + "from   org " + "where org_cd = %(ORG_CD)s", connection); // TODO externalize
        mfrStmt.useColonBind();

        orgDistrib = new SqlStatement("insert into org_distrib(org_id,distrib_id) " + "select org_id, %(DISTRIB_ID)s "
                + "from org " + "where org_cd = %(ORG_CD)s", connection).useColonBind();

        orgDatafeed = new SqlStatement(
                "insert into org_datafeed(org_id) " + "select org_id " + "from org " + "where org_cd = %(ORG_CD)s",
                connection);
        orgDatafeed.useColonBind();
    }

}