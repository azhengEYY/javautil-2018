package org.javautil.dblogging;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

public class H2LoggerDataSourceTest {

    final static String count = "select count(*) cnt from ut_process_status";

    /**
     * Autocommit should be on
     * 
     * @throws IOException
     * @throws SQLException
     */
    // @Test
    // public void testNewDatabaseAutocommit() throws IOException, SQLException {
    // DataSource ds = new
    // H2LoggerDataSource().getPopulatedH2FromDbLoggerProperties();
    // testDataSource(ds);
    // }

    // @Test
    public void testDataSource(DataSource ds) throws IOException, SQLException {
        Connection conn1 = ds.getConnection();

        Statement statement1 = conn1.createStatement();
        ResultSet rset1 = statement1.executeQuery(count);
        rset1.next();
        long cnt1 = rset1.getLong("cnt");
        assertEquals(0, cnt1);

        Connection conn2 = ds.getConnection();
        Statement statement2 = conn2.createStatement();
        ResultSet rset2 = statement2.executeQuery(count);
        rset2.next();
        long cnt2 = rset2.getLong("cnt");
        assertEquals(0, cnt2);

        // is autocommit
        String insert = "insert into ut_process_status(ut_process_status_id) values(-2)";
        statement1.executeUpdate(insert);

        statement2 = conn2.createStatement();
        rset2 = statement2.executeQuery(count);
        rset2.next();
        cnt2 = rset2.getLong("cnt");
        assertEquals(1, cnt2);

        conn1.commit(); // for single connection variant

        rset2 = statement2.executeQuery(count);
        rset2.next();
        cnt2 = rset2.getLong("cnt");
        assertEquals(1, cnt2);

    }

}
