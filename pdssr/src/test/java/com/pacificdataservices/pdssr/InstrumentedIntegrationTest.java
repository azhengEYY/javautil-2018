// package com.pacificdataservices.pdssr;
//
// import java.beans.PropertyVetoException;
// import java.io.File;
// import java.io.FilenameFilter;
// import java.io.IOException;
// import java.sql.Connection;
// import java.sql.SQLException;
// import java.text.ParseException;
// import java.util.ArrayList;
// import java.util.Arrays;
//
// import javax.sql.DataSource;
//
// import org.javautil.dblogging.OracleInstall;
// import org.javautil.misc.Timer;
// import org.javautil.sql.ApplicationPropertiesDataSource;
// import org.javautil.sql.Binds;
// import org.javautil.sql.Dialect;
// import org.javautil.sql.SqlStatement;
// import org.javautil.util.ListOfNameValue;
// import org.javautil.util.NameValue;
// import org.junit.Test;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
//
//
// public class InstrumentedIntegrationTest implements FilenameFilter {
// private final Logger logger = LoggerFactory.getLogger(this.getClass());
//
// private Connection conn;
//
// private DataSource datasource;
//
// private int loadCount = 1;
//
// private Dialect dialect;
//
// public InstrumentedIntegrationTest() throws PropertyVetoException,
// SQLException, IOException {
// datasource = new ApplicationPropertiesDataSource().getDataSource();
// this.conn = datasource.getConnection();
// }
//
// @Test
// public void oracleDialectTest()
// throws Exception {
// // dialect = Dialect.ORACLE;
// fullTest();
// }
// // @Test
// //
// // public void fullDialectTest() throws SQLException, IOException,
// // ParseException, PropertyVetoException {
// //// dialect = Dialect.H2;
// //// dbTest = new DbTest(dialect);
// //// this.conn = getConnection(dialect);
// //// fullTest(dialect);
// //
// // dialect = Dialect.POSTGRES;
// // dbTest = new DbTest(Dialect.POSTGRES);
// // this.conn = getConnection(Dialect.POSTGRES);
// //
// // fullTest(Dialect.POSTGRES);
// //
// // }
//
// // @Test
// public void fullTest()
// throws Exception {
//
// Timer t = new Timer("fullTest " + dialect);
// logger.info("fullTest");
// // if (dialect.equals(Dialect.POSTGRES)) {
// // dropSchema();
// // }
// createSchema();
// seedDatabase();
// conn.commit();
//
// loadFiles(null);
// runConditionsAll(conn);
// prepostAll();
//
// postAll();
// // unloadAll();
// //
// t.logElapsed();
//
// conn.close();
//
// }
//
//
//
// private void createSchema() throws Exception {
// Timer t = new Timer("createSchema");
// OracleInstall installer = new OracleInstall(conn,true);
// logger.info("about to process install");
// installer.process();
// logger.info("about to create schema conn: " + conn + " dialect: " + dialect);
// CreateSchema cs = new CreateSchema(conn,true);
// cs.process();
// conn.commit();
// t.logElapsed();
// logger.info("schema created");
// }
//
// private void seedDatabase() throws SQLException, IOException {
// logger.info("seedDatabase");
// Timer t = new Timer("seedDatabase");
// SeedSalesReportingDatabase seeder = new SeedSalesReportingDatabase(conn);
// seeder.process(true);
// t.logElapsed();
// }
//
// // TODO hash and see if loaded
// public void loadFiles(String dir) throws SQLException, ParseException,
// IOException {
// logger.info("loadFiles");
// Timer t = new Timer("loadFiles");
// String loadFileDir = "src/test/resources/dataloads";
// if (dir != null) {
// loadFileDir = dir;
// }
// File loadDirFile = new File(loadFileDir);
// FilenameFilter filter = this;
// CdsDataLoader loader = new CdsDataLoader(conn);
// File[] files = loadDirFile.listFiles(filter);
// logger.info("files:\n" + files);
// Arrays.sort(files);
//
// int fileCount = 0;
// for (File f : files) {
// if (++fileCount > loadCount) {
// break;
// }
// loader.process(f.getAbsolutePath(), "EXOTICTX", false);
// logger.info("fileCount " + fileCount);
// }
//
//
// t.logElapsed();
// }
//
// ArrayList<Long> getAllLoadNumbers() throws SQLException {
// SqlStatement loads = new SqlStatement(conn, "select etl_file_id from
// etl_file");
// ListOfNameValue rows = loads.getListOfNameValue(new Binds(),true);
// ArrayList<Long> loadNumbers = new ArrayList<Long>();
// for (NameValue row : rows) {
// Long loadNumber = row.getLong("etl_file_id");
// if (loadNumber == null) {
// throw new IllegalStateException("no etl_file_id in map " + row.toString());
// }
// loadNumbers.add(row.getLong("etl_file_id"));
// }
// return loadNumbers;
// }
//
// private void runConditionsAll(Connection conn) throws SQLException,
// IOException {
// if (conn == null) {
// throw new IllegalArgumentException("connection is null");
// }
// Timer t = new Timer("runConditionsAll");
// logger.info("runConditionsAll");
// LoadConditionIdentification lci = new LoadConditionIdentification(conn);
// ArrayList<Long> loadNumbers = getAllLoadNumbers();
// logger.info("lci is " + lci.toString());
// for (Long loadNumber : loadNumbers) {
// logger.info("Load Number: " + 3);
// lci.process(loadNumber, 3);
// }
//
// t.logElapsed();
// }
//
// // TODO dedup from postall
// private void prepostAll() throws SQLException, InvalidLoadFileException {
// Timer t = new Timer("prepostAll");
// logger.info("prepostAll");
// ArrayList<Long> loadNumbers = getAllLoadNumbers();
// Prepost prepost = new Prepost(conn, 5);
// for (Long loadNumber : loadNumbers) {
// prepost.process(loadNumber);
// }
//
// }
//
// private void postAll() throws SQLException, InvalidLoadFileException {
// Timer t = new Timer("postAll");
// logger.info("postAll");
// Post post = new Post(conn, 5, dialect);
// ArrayList<Long> loadNumbers = getAllLoadNumbers();
// for (Long loadNumber : loadNumbers) {
// post.process(loadNumber);
// }
//
// t.logElapsed();
// }
//
// private void unloadAll() throws SQLException, IOException {
// Timer t = new Timer("unloadAll");
// SqlStatement loads = new SqlStatement("select etl_file_id from etl_file");
// String destDir = "/tmp/";
// CdsUnload unloader = new CdsUnload(conn, false);
// int fileCount = 0;
//
// ArrayList<Long> loadNumbers = getAllLoadNumbers();
//
// for (Long etlFileId : loadNumbers) {
// String fileName = destDir + etlFileId + ".cds";
// unloader.process(etlFileId, fileName, false);
// }
//
// ListOfNameValue rows = loads.getListOfNameValue(conn, true);
// for (NameValue row : rows) {
// long etlFileId = row.getLong("ETL_FILE_ID");
// String fileName = destDir + etlFileId + ".cds";
// unloader.process(etlFileId, fileName, false);
//
// }
//
// t.logElapsed();
// }
//
// @Override
// public boolean accept(File dir, String fileName) {
// return fileName.endsWith(".cds");
// }
//
// }
