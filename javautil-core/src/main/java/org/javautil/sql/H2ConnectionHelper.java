package org.javautil.sql;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.javautil.io.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class H2ConnectionHelper {
	private final static Logger logger = LoggerFactory.getLogger(H2ConnectionHelper.class);

	static ArrayList<String> getDatabaseFile(Connection conn) throws IOException {
		final String info = conn.toString();
		logger.info("info: " + info);
		final Pattern p = Pattern.compile(".*url=jdbc:h2:([^ ]*).*");
		final Matcher m = p.matcher(info);
		String filebase = null;
		if (m.matches()) {
			filebase = m.group(1);
		} else {
			System.out.println("wtf");
		}
		FileUtil.basename(filebase);
		logger.info("filebase: " + filebase);
		final ArrayList<String> files = new ArrayList<>();

		try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(Paths.get("/tmp"), "*.dbf*"))
		{
			dirStream.forEach(path -> System.out.println(path));
			//    files.add(path.getFileName()));
		}
		return files;

	}
}
