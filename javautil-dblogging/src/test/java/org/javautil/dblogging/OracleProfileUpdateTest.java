package org.javautil.dblogging;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.javautil.sql.SqlSplitterException;
import org.junit.BeforeClass;
import org.junit.Test;

public class OracleProfileUpdateTest {
    static DataSource   dataSource;
    OracleProfileUpdate opu;
    Connection          connection;

    @BeforeClass
    public static void beforeClass() throws IOException, SQLException {
        dataSource = new H2LoggerDataSource().getPopulatedH2FromDbLoggerProperties();

    }

    @Test
    public void test() throws SQLException, SqlSplitterException, IOException {
        connection = dataSource.getConnection();
        opu = new OracleProfileUpdate(connection);
        String outFileName = "/tmp/dev12c_ora_8973_job_1.prf";
        File outFile = new File(outFileName);
        if (outFile.exists()) {
            outFile.delete();
        }
        assertFalse(outFile.exists());
        opu.runTkprof(new File("src/test/resources/oratrace/dev12c_ora_8973_job_1.trc"), outFileName);
        assertTrue(outFile.exists());
        connection.close();
    }

    // @Test todo prepoluate with data with a simple db copy
    public void updateTraceTest() throws SQLException, FileNotFoundException, IOException {
        connection = dataSource.getConnection();
        opu = new OracleProfileUpdate(connection);
        opu.updateJob(2);
        connection.commit();
        opu.tkprofJob(2);
    }
}
