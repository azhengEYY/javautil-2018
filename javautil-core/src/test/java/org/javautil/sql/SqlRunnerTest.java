package org.javautil.sql;


import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.javautil.sql.SqlRunner;
import org.javautil.sql.SqlSplitter;
import org.javautil.sql.SqlSplitterException;
import org.javautil.sql.SqlStatement;
import org.javautil.sql.SqlStatements;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SqlRunnerTest {

    
    private final Logger logger = LoggerFactory.getLogger(getClass());

    
    @Test
    public void testFile() throws SqlSplitterException, SQLException, IOException {
        final DataSource dataSource = new ApplicationPropertiesDataSource().getDataSource(this, "xe.application.properties");
        String ddlFile ="src/main/resources/cursor_stat.sql";
        String ddlResource = "cursor_stat.sql";
       
        Connection xeConnection = dataSource.getConnection();
        
        new SqlRunner(this, "cursor_stat_drop.sql").setConnection(xeConnection).setTrace(true)
        .setPrintSql(true).setContinueOnError(true).setShowError(true).process();

        
        new SqlRunner(this, "cursor_stat.sql").setConnection(xeConnection).setTrace(true)
        .setPrintSql(true).setContinueOnError(true).setShowError(true).process();
        
//        new SqlRunner(this, "ddl/oracle/dblogger_uninstall.sr.sql").setConnection(xeConnection)
//        .setPrintSql(true).setContinueOnError(true).setShowError(true).process();
//        SqlSplitter splitter = new SqlSplitter(new File(ddlFile));
////        List<SqlStatement> statements = splitter.getSqlStatementList();
//        assertEquals(16,statements.size());
//        int i = 1;
//        for (SqlStatement ss : statements) {
//            String message = String.format("#%d: \n%s",i,ss.getSql());
//            System.out.println(message);
//            i++;
//        }
//        
//        SqlSplitter splitterFromResource = new SqlSplitter(this,ddlResource);
//        List<SqlStatement> statements2 = splitterFromResource.getSqlStatementList();
//        assertEquals(16,statements2.size());
//        
//        SqlRunner runner = new SqlRunner(this,ddlResource);
//        SqlSplitter splitter2 = runner.getSplitter();
//        List<SqlStatement> statementList2 = splitter2.getSqlStatementList();
//        assertEquals(16,statementList2.size());
//        
//        SqlStatements statement2 = splitter2.getSqlStatements();
//        List<SqlStatement> statementList3 = statement2.getStatements();
//        logger.info("splitter statement list size" + statementList3.size());
//        assertEquals(16,statementList3.size());
//        
//        SqlStatements ss4 = splitter2.getSqlStatements();
//        List<SqlStatement>statementList = ss4.getStatements();
//        assertEquals(16,statementList.size());
//        
//        runner.setConnection(xeConnection);
//        runner.setListStatementsBeforeExecute(true);
//        runner.setPrintSql(true);
//        runner.process();
//        
//        DbloggerOracleInstall installer  = new DbloggerOracleInstall(xeConnection,true,true);
//        installer.installCursorTables();
        
    }
}
