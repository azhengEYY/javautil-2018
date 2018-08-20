package org.javautil.dblogging;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

public class H2LoggerDataSourceCheck {

    final static String count = "select count(*) cnt from ut_process_status";

    // @Test
    public long testDataSource(DataSource ds) throws SQLException {
        Connection conn1 = ds.getConnection();

        Statement statement1 = conn1.createStatement();
        ResultSet rset1 = statement1.executeQuery(count);
        rset1.next();
        long cnt1 = rset1.getLong("cnt");
        return cnt1;

    }

}
