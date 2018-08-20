package org.javautil.h2;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.sql.Connection;
import java.sql.Statement;

import org.junit.Test;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class H2DropAllObjectsTest {


	@Test
	public void casper() throws Exception {
		final HikariConfig config = new HikariConfig();
		config.setJdbcUrl("jdbc:h2:/tmp/casper");
		config.setUsername("sr");
		config.setPassword("tutorial");
		config.setAutoCommit(true);

		HikariDataSource dataSource = new HikariDataSource(config);
		
		Connection connection = dataSource.getConnection();
		File f = new File("/tmp/casper.mv.db");
		assertTrue (f.exists());
		Statement s = connection.createStatement();
		
		s.execute("drop all objects delete files");
		assertTrue (f.exists());
		s.execute("create table a (b number(9))");
		/* do a lot of work */
		connection.commit();
		s.close();
		connection.close();
		assertTrue (f.exists());

		dataSource.close();
		
		assertFalse (f.exists());
	}

}
