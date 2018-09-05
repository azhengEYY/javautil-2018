package org.javautil.sql;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;

import org.javautil.text.SimpleDateFormatFactory;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class H2FileDatabaseTest {
    private static final Logger logger = LoggerFactory.getLogger(H2FileDatabaseTest.class);

    @Test
    public void test() throws ClassNotFoundException, SQLException {
        // Date d = new Date();
        // SimpleDateFormat df = SimpleDateFormatFactory.getDateTimeForFileName();
        // String fname= df.format(d);
        // String fpath = "/tmp/" + fname;
        String timestamp = SimpleDateFormatFactory.getTimestamp();
        File f = new File("/tmp/" + timestamp);
        logger.info("h2 file " + f.getAbsolutePath());

        Connection conn = H2FileDatabase.getConnection(f, "", "");
        assertNotNull(conn);
        conn.close();
        File f2 = new File(f.getAbsolutePath() + ".mv.db");
        logger.info("checking for file: " + f2.getAbsolutePath());
        logger.info("file size " + f2.length());
        assertTrue(f2.exists());
    }

}
