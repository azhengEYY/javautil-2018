package org.javautil.dblogging.formatter;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import org.javautil.oracle.trace.CursorsStats;
import org.javautil.oracle.trace.OracleTraceProcessor;
import org.javautil.oracle.trace.formatter.SqlMarshaller;
import org.javautil.sql.Binds;
import org.javautil.sql.H2InMemory;
import org.javautil.sql.SqlRunner;
import org.javautil.sql.SqlSplitterException;
import org.javautil.sql.SqlStatement;
import org.javautil.util.ListOfNameValue;
import org.javautil.util.NameValue;
import org.junit.Before;
import org.junit.Test;



public class SqlMarshallerTest {
    
    private boolean initted = false;
    private Connection connection;
    
    @Before
    public void before () throws ClassNotFoundException, SQLException, SqlSplitterException, IOException {
        if (initted) {
            return;
        }
        connection = H2InMemory.getConnection();
        SqlRunner sr =  new SqlRunner(this,"cursor_stat.sql");
        sr.setConnection(connection);
        sr.setPrintSql(true);
        sr.process();
        
        initted = true;
    }
    
    @Test
    public void test() throws IOException, SqlSplitterException, SQLException {
        OracleTraceProcessor otp = new OracleTraceProcessor("src/test/resources/oratrace/dev12c_ora_10581_job_6.trc");
        otp.process();
        CursorsStats cursors = otp.getCursors();
        SqlMarshaller dillon = new SqlMarshaller(connection);
        long runId = dillon.persist(cursors);
        connection.commit();
        SqlStatement ssRun = new SqlStatement(connection,"select * from cursor_info_run");
       
        SqlStatement ssCursors = new SqlStatement(connection,"select * from cursor_stat");
        SqlStatement ssText = new SqlStatement(connection,"select * from cursor_text");
        NameValue runNv = ssRun.getNameValue();
        ListOfNameValue cursorsNv = ssCursors.getListOfNameValue(new Binds(),true);
        System.out.println("cursorsNv" + cursorsNv);
        assertTrue(cursorsNv.size() > 0);
        ListOfNameValue textNv = ssText.getListOfNameValue(new Binds(), true);
        System.out.println("text:\n" + textNv );
        assertTrue(textNv.size() > 0);
        assertNotNull(runNv != null);
        
        
    }

}
