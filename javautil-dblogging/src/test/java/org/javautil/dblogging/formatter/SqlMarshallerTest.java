package org.javautil.dblogging.formatter;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import org.javautil.oracle.trace.CursorsStats;
import org.javautil.oracle.trace.OracleTraceProcessor;
import org.javautil.oracle.trace.formatter.SqlMarshaller;
import org.javautil.sql.Binds;
import org.javautil.sql.H2FileDatabase;
import org.javautil.sql.SqlRunner;
import org.javautil.sql.SqlSplitterException;
import org.javautil.sql.SqlStatement;
import org.javautil.text.SimpleDateFormatFactory;
import org.javautil.util.ListOfNameValue;
import org.javautil.util.NameValue;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class SqlMarshallerTest {
    
    private static final Logger logger = LoggerFactory.getLogger(SqlMarshallerTest.class);
    private boolean initted = false;
    private static Connection connection;
    private static String dbFileName;
    
    @BeforeClass
    public static void beforeClass () throws ClassNotFoundException, SQLException, SqlSplitterException, IOException {

        String timestamp = SimpleDateFormatFactory.getTimestamp();
        File f = new File("/tmp/" + timestamp);
        dbFileName = f.getAbsolutePath();
        connection = H2FileDatabase.getConnection(f, "", "");
        
        SqlRunner sr =  new SqlRunner(new SqlMarshallerTest(),"ddl/oracle/dblogger_install_tables.sr.sql");
       // SqlRunner sr =  new SqlRunner(new SqlMarshallerTest(),"cursor_stat.sql");
        sr.setConnection(connection);
        sr.setPrintSql(true);
        sr.process();
    }
    
    @AfterClass
    public static void afterClass() {
        logger.info("created " + dbFileName);
    }
    
    @Test
    public void test() throws IOException, SqlSplitterException, SQLException {
        OracleTraceProcessor otp = new OracleTraceProcessor("src/test/resources/oratrace/dev18b_ora_813.trc");
        otp.process();
        CursorsStats cursors = otp.getCursors();
        SqlMarshaller dillon = new SqlMarshaller(connection);
        long runId = dillon.saveAll(cursors);
        connection.commit();
        SqlStatement ssRun = new SqlStatement(connection,"select * from cursor_info_run");
       
        SqlStatement ssCursors = new SqlStatement(connection,"select * from cursor_stat");
        SqlStatement ssText = new SqlStatement(connection,"select * from cursor_sql_text");
        NameValue runNv = ssRun.getNameValue();
        ListOfNameValue cursorsNv = ssCursors.getListOfNameValue(new Binds(),true);
       logger.debug("cursorsNv" + cursorsNv);
        assertTrue(cursorsNv.size() > 0);
        ListOfNameValue textNv = ssText.getListOfNameValue(new Binds(), true);
        logger.debug("text:\n" + textNv );
        assertTrue(textNv.size() > 0);
        assertNotNull(runNv != null);
        // TODO read the tables and check row counts and values for sample records
        
    }

}
