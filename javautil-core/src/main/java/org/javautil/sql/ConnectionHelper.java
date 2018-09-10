package org.javautil.sql;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map.Entry;

public class ConnectionHelper {

	public static void executeProcedure(Connection connection, final String sql, Binds binds) throws SQLException {
		final CallableStatement callableStatement = connection.prepareCall(sql);
		for (final Entry<String, Object> entry : binds.entrySet()) {
			callableStatement.setObject(entry.getKey(), entry.getValue());
		}
		callableStatement.executeUpdate();
		callableStatement.close();

	}

	public static void clobWrite(Clob clob, Writer writer) throws SQLException, IOException {
        final int length = 1024 * 1024;
        final Reader reader = clob.getCharacterStream();
        final char[] buffer = new char[length];
        int count;
        while ((count = reader.read(buffer)) != -1) {
            writer.write(buffer, 0, count);
        }
        clob.free();
        reader.close();
    }
}