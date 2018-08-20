package org.javautil.dblogging;

import java.io.IOException;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.javautil.oracle.trace.formatter.LoggerModels;

public class LoggerModelsTest {

    // @Test Need populate with test data H2LoggerDataSource
    public void test() throws IOException, SQLException {
        DataSource ds = new H2LoggerDataSource().getPopulatedH2FromDbLoggerProperties();
        LoggerModels models = new LoggerModels(ds);
        String model = models.getJobStepInfoJson(1l);
        // System.out.println(model);
    }
}
