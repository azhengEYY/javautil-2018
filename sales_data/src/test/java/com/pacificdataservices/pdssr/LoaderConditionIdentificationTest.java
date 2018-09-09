// package com.pacificdataservices.pdssr;
//
// import static org.junit.Assert.assertEquals;
// import static org.junit.Assert.assertTrue;
//
// import java.io.IOException;
// import java.sql.Connection;
// import java.sql.SQLException;
// import java.text.ParseException;
//
// import javax.sql.DataSource;
//
// import org.javautil.oracle.OracleConnectionUtil;
// import org.javautil.sql.ApplicationPropertiesDataSource;
// import org.javautil.sql.DataNotFoundException;
// import org.junit.BeforeClass;
// import org.junit.Test;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
//
// public class LoaderConditionIdentificationTest extends CreateSchemaTest {
// private Logger logger = LoggerFactory.getLogger(getClass());
//
// @Test
// public void test() throws SQLException, ParseException, IOException,
// InvalidLoadFileException {
// Connection connection = datasource.getConnection();
// for (int i = 0 ; i < 100; i++) {
// LoadConditionIdentification lci = new
// LoadConditionIdentification(connection);
// try {
// lci.getUtConditionRunId(1);
// } catch (DataNotFoundException noData) {
//
// }
// }
//// String dupeCursors = OracleConnectionUtil.getDuplicateCursors(connection);
//// logger.info("dupe cursors\n" + dupeCursors);
//// assertEquals(0,dupeCursors.length());
// }
//
// }
