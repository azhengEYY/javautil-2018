package com.pacificdataservices.pdssr;

import static org.junit.Assert.assertEquals;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.javautil.dblogging.installer.DbloggerOracleInstall;
import org.javautil.sql.ApplicationPropertiesDataSource;
import org.javautil.sql.Binds;
import org.javautil.sql.SqlSplitterException;
import org.javautil.sql.SqlStatement;
import org.javautil.util.ListOfNameValue;
import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO consolidate with CreateSchema
public class OracleSchemaInit {
    private static final Logger logger = LoggerFactory.getLogger(OracleSchemaInit.class);
    static DataSource           dataSource;

    @BeforeClass
    public static void beforeClass() throws Exception, SqlSplitterException {
        dataSource = new ApplicationPropertiesDataSource().getDataSource();
        final Connection connection = dataSource.getConnection();
        final DbloggerOracleInstall installer = new DbloggerOracleInstall(connection, true, false);
        logger.info("about to process install");
        installer.process();
        connection.close();
    }

    /**
     * INDEX 37 LOB 70 PACKAGE 1 PACKAGE BODY 1 SEQUENCE 28 TABLE 30 VIEW 9
     *
     * @throws SQLException
     */
    // @Test TODO restore
    public void testSchema() throws SQLException {

        final Connection connection = dataSource.getConnection();
        final SqlStatement ss = new SqlStatement(connection,
                "select object_type, count(*) object_count from user_objects group by object_type\n"
                        + "order by object_type\n" + "");
        final ListOfNameValue lnv = ss.getListOfNameValue(new Binds(), true);
        // TODO WTF lobs keep growing

        lnv.get(0).getLong("object_count");
        final long packageCount = lnv.get(2).getLong("object_count");
        final long packageBodyCount = lnv.get(3).getLong("object_count");
        final long sequenceCount = lnv.get(4).getLong("object_count");
        final long tableCount = lnv.get(5).getLong("object_count");
        final long viewCount = lnv.get(6).getLong("object_count");

        assertEquals(1L, packageCount); // package
        assertEquals(1L, packageCount);
        assertEquals(1L, packageBodyCount);
        assertEquals(28L, sequenceCount);
        assertEquals(30L, tableCount);
        assertEquals(9L, viewCount);

        // TODO ensure package is compiled and runs

    }

}
