package org.javautil.dblogging.installer;

import java.sql.Connection;
import java.sql.SQLException;

import org.javautil.sql.Dialect;

public class DbloggerInstallDatabaseObjects {
    private Connection connection;

    public DbloggerInstallDatabaseObjects(Connection connection) {
        this.connection = connection;
    }

    public void process() throws SQLException {
        Dialect dialect = Dialect.getDialect(connection);
        switch (dialect) {
        case H2:
        default:
            throw new IllegalArgumentException("Unsupported dialect");
        }
    }
}
