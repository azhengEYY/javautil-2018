package org.javautil.dblogging;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import org.javautil.sql.SqlSplitterException;

/**
 * Persistence is performed in a different database
 * @author jjs
 *
 */
public class SplitLoggerForOracle extends BasicPersistentenceDblogger implements Dblogger {

    public SplitLoggerForOracle(Connection connection, Dblogger persistenceLogger)
            throws IOException, SQLException, SqlSplitterException {
        super(connection);
        this.persistencelogger = persistenceLogger;
    }

}
