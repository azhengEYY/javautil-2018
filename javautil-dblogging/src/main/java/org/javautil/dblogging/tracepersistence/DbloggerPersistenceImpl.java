package org.javautil.dblogging.tracepersistence;

import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.javautil.sql.NamedSqlStatements;
import org.javautil.sql.SequenceHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO these should all throw Dblogger exception, don't want
//  to blow up a job because of an error in the logger
public class DbloggerPersistenceImpl extends AbstractDbloggerPersistence implements DbloggerPersistence {

    private Logger                     logger             = LoggerFactory.getLogger(DbloggerPersistenceImpl.class);

    private long                      jobStartMilliseconds;

    private String                    moduleName;

    private String                    actionName;

    SequenceHelper                    sequenceHelper;

 

    private CallableStatement         endJobStatement;

    protected List<CallableStatement> callableStatements = new ArrayList<>();

    public DbloggerPersistenceImpl(Connection connection) throws IOException, SQLException {
        super(connection);
        statements = NamedSqlStatements.getNameSqlStatementsFromSqlSplitterResource(this, "ddl/h2/dblogger_dml.ss.sql");
        sequenceHelper = new SequenceHelper(connection);
    }

    CallableStatement prepareCall(String sql) throws SQLException {
        final CallableStatement retval = connection.prepareCall(sql);
        callableStatements.add(retval);
        return retval;
    }



    public void dispose() throws SQLException {
        statements.close();
    }

  
   
}
