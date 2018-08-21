package org.javautil.dblogging;

import java.sql.Connection;
import java.sql.SQLException;

import org.javautil.sql.Binds;
import org.javautil.sql.ConnectionUtil;
import org.javautil.sql.SqlStatement;
import org.javautil.util.NameValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SampleUsage {
    
    private Dblogger dblogger;
    private Connection connection;
    private Logger logger = LoggerFactory.getLogger(getClass());
    private String processName;
    
    public SampleUsage(Connection connection, Dblogger dblogger, String processName) {
        this.connection = connection;
        this.dblogger = dblogger;
        this.processName = processName;
    }
    
    public long process() throws SQLException {
        dblogger.prepareConnection();
       
        
        final long id = dblogger.beginJob(processName, getClass().getCanonicalName(), "ExampleLogging", null,
                Thread.currentThread().getName(), null);
        actionNoStep();
        stepNoAction();
        dblogger.endJob();
        return id;
        
       

        
    }
    
   
    
    public void processException() {
        
    }
    
    private void actionNoStep() throws SQLException {
        
        dblogger.setAction("Some work");
        ConnectionUtil.exhaustQuery(connection, "select * from user_tab_columns, user_tables");       
    }
    
    private void stepNoAction() throws SQLException {
        
        dblogger.insertStep("Useless join", "full join", getClass().getName());
        ConnectionUtil.exhaustQuery(connection, "select * from user_tab_columns, user_tables");       
    }
    
    
  
    
    NameValue getUtProcessStatus(Connection connection, long id) throws SQLException {
        final String sql = "select * from ut_process_status "
                + "where ut_process_status_id = :ut_process_stat_id";
        final SqlStatement ss = new SqlStatement(connection, sql);
        Binds binds = new Binds();
        binds.put("ut_process_stat_id", id);
        final NameValue retval = ss.getNameValue();
        ss.close();
        return retval;
    }
//    public void sampleUsageTest() throws SQLException, SqlSplitterException, IOException {
//   
//
//
//    
//
//        dblogger.setAction("Another set of work");
//        ConnectionUtil.exhaustQuery(connection, "select count(*) from all_tab_columns");
//            // test it
//
//        // check ut_status_process_fields
//        final NameValue status = getUtProcessStatus(connection, id);
//        logger.debug(status.getSortedMultilineString());
//        assertEquals(processName, status.getString("PROCESS_NAME"));
//        assertEquals("C", status.getString("STATUS_ID"));
//        assertNotNull(status.getString("STATUS_TS"));
//        // assertNotNull(status.getString("TOTAL_ELAPSED"));
//        String tracefileName = status.getString("TRACEFILE_NAME");
//        logger.info("tracefileName {}", tracefileName);
//
//    }

}
