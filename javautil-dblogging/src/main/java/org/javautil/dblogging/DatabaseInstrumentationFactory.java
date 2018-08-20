package org.javautil.dblogging;

import java.sql.Connection;

import javax.sql.DataSource;

import org.javautil.sql.DataSourceFactory;
import org.javautil.sql.Dialect;
import org.javautil.sql.SqlSplitterException;

public class DatabaseInstrumentationFactory {

    public static Dblogger getDatabaseInstrumentation(Connection connection)
            throws SqlSplitterException, Exception {
        switch (Dialect.getDialect(connection)) {
        case ORACLE:
            return new DbloggerForOracle(connection);
        case H2:
            DataSource ds = DataSourceFactory.getH2Permanent("/scratch/dblogging", "sa", "tutorial");
            Connection conn = ds.getConnection();
            H2Install installer = new H2Install(conn).setDrop(false).setNoFail(true);
            installer.process();
            DbloggerH2 dblogger = new DbloggerH2(conn);
            return dblogger;

        default:
            return new DatabaseLoggerNoOperation();
        }

    }
}
