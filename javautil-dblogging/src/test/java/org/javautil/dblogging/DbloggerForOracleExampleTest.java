package org.javautil.dblogging;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

import javax.sql.DataSource;

import org.javautil.io.FileUtil;
import org.javautil.sql.ApplicationPropertiesDataSource;
import org.javautil.sql.Binds;
import org.javautil.sql.SqlStatement;
import org.javautil.util.ListOfNameValue;
import org.javautil.util.NameValue;
import org.javautil.util.PropertiesFactory;
import org.junit.BeforeClass;
import org.junit.Test;

public class DbloggerForOracleExampleTest {
    
    
    static DataSource applicationDataSource;
    static Connection applicationConnection;
    static DataSource loggerDataSource;
    static Connection loggerConnection;
    static Dblogger dblogger;
    
    @BeforeClass
    public static void beforeClass() throws IOException, SQLException {
        String propertyPath = "src/test/resources/logger_and_application.properties";
        FileInputStream fis = new FileInputStream(propertyPath);
        Properties properties = new Properties();
        properties.load(fis);
   
        applicationDataSource = ApplicationPropertiesDataSource.getDataSource(properties);
        applicationConnection = applicationDataSource.getConnection();
        loggerDataSource = DbloggerPropertiesDataSource.getDataSource(properties);
        loggerConnection = loggerDataSource.getConnection();
        dblogger = new SplitLoggerForOracle(applicationConnection, loggerConnection);
         
    }
    
    
    @Test
    public void test1() throws SQLException, IOException {
       DbloggerForOracleExample example = new DbloggerForOracleExample(applicationConnection, dblogger, "example", false);
       long jobId = example.process();
       SqlStatement ss = new SqlStatement("select * from job_log where job_log_id = :job_log_id");
       ss.setConnection(loggerConnection);
       Binds binds = new Binds();
       binds.put("job_log_id", jobId);
       NameValue jobNv = ss.getNameValue(binds,true);
       assertEquals("SR",jobNv.get("schema_name"));
       assertEquals("main",jobNv.get("thread_name"));
       assertNotNull(jobNv.get("process_run_nbr"));
       assertEquals("DONE",jobNv.get("status_msg"));
       assertEquals("C",jobNv.get("status_id"));
       assertNotNull(jobNv.get("status_ts"));
       assertEquals("N",jobNv.get("ignore_flg"));
       assertEquals("ExampleLogging", jobNv.get("module_name"));
       assertEquals("org.javautil.dblogging.DbloggerForOracleExample",jobNv.get("classname"));
       String tracefileName = jobNv.getString("tracefile_name");
       int jobInd = tracefileName.indexOf("job");
       assertTrue(jobInd >= 0);
       //
       SqlStatement stepSs = new SqlStatement("select * from job_step where job_log_id = :job_log_id order by job_step_id ");
       stepSs.setConnection(loggerConnection);
       ListOfNameValue nvSteps = stepSs.getListOfNameValue(binds,true);
       assertEquals(2,nvSteps.size());
       System.out.println(nvSteps);
       NameValue step1 = nvSteps.get(0);
       assertEquals(step1.get("step_name"),"Useless join");
       assertEquals(step1.get("classname"),"org.javautil.dblogging.DbloggerForOracleExample");
       assertEquals(step1.get("step_info"),"full join");
       assertNotNull(step1.get("start_ts"));
       assertNotNull(step1.get("end_ts"));

       System.out.println(jobNv);
    }

}
