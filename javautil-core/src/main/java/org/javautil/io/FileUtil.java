package org.javautil.io;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class FileUtil {

	public static List<String> readAllLines(String filePath) throws IOException {
		final Charset charset = Charset.forName("ISO-8859-1");
		final Path traceFilePath = Paths.get(filePath);
		return Files.readAllLines(traceFilePath, charset);
	}

	public static void emitStringList(List<String> lines, OutputStream os) throws IOException {
		final OutputStreamWriter bos = new OutputStreamWriter(os);
		for (final String line : lines) {
			bos.write(line);
		}
	}

	public static String getAsString(String filePath) throws IOException {
		return new String(Files.readAllBytes(Paths.get(filePath)), StandardCharsets.UTF_8);
	}

	/**
	 * @param file
	 * @param extension
	 * @return
	 */
	public static String basename(final String filePathName) {

		String retval;
		final File f = new File(filePathName);
		final String parentDirectoryName = f.getParent();
		if (parentDirectoryName == null) {
			retval = filePathName;
		} else {
			//	logger.info("parentDirectoryName: " + parentDirectoryName);
			retval = filePathName.substring(parentDirectoryName.length() + 1);
		}
		return retval;
	}

	// TODO ensure compatibility with bash basename
	public static String basename(final String filePathName, final String extension) {
		String retval = filePathName;
		final String baseFileName = basename(filePathName);
		if (baseFileName.endsWith(extension)) {
			final int endIndex = baseFileName.length() - extension.length();
			retval = baseFileName.substring(0, endIndex);
		}
		return retval;
	}

}
