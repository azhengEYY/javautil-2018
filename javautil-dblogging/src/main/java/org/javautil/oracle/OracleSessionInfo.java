package org.javautil.oracle;

import java.sql.Connection;
import java.sql.SQLException;

import org.javautil.sql.SqlStatement;
import org.javautil.util.NameValue;

public class OracleSessionInfo {

    public static String getConnectionInfo(Connection connection) throws SQLException {
        SqlStatement mySessionSS = new SqlStatement(connection, "select * from my_session");
        NameValue mySessionNv = mySessionSS.getNameValue();
        String retval =  String.format("serviceName: %s username: %s", mySessionNv.getString("service_name"),
                mySessionNv.getString("username"));
        mySessionSS.close();
        return retval;
    }
}
