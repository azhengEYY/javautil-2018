// package com.pacificdataservices.pdssr;
//
// import java.io.File;
// import java.io.FileNotFoundException;
// import java.sql.Connection;
//
// import org.javautil.io.ResourceHelper;
// import org.javautil.sql.SqlStatements;
// import org.junit.Test;
//
// public class TestYamlFiles {
//
// @Test
// public void testPostDml() {
// getSqlStatements("pdssr/oracle/post_dml.yaml", false);
//
// }
//
// @Test
// public void testEtlPostingQueries() {
//
// getSqlStatements("pdssr/oracle/etl_posting_queries.yaml", true);
//
// }
//
// SqlStatements getSqlStatements(String path, boolean isMap) {
// Connection connection = null;
// if (path == null) {
// throw new IllegalArgumentException("path is null");
// }
// SqlStatements retval = null;
//
// File file = ResourceHelper.getResourceAsFile(this, path);
// // logger.info(file.getAbsolutePath());
// try {
// retval = new SqlStatements(file.getAbsolutePath(), connection, isMap);
// } catch (FileNotFoundException e) {
// throw new RuntimeException(e);
// }
// return retval;
// }
// }
