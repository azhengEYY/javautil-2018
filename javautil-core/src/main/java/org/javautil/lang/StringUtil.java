package org.javautil.lang;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StringUtil {

	private static final Logger logger = LoggerFactory.getLogger(StringUtil.class);
	private static final char[] HEX_CHARS = "0123456789abcdef".toCharArray();
	private static Pattern newlinePattern = Pattern.compile("\r\n|\r|\n");

	public static int newlineCount(String input) {
		final Matcher m = newlinePattern.matcher(input);
		int count = 0;
		while (m.find()) {
			count++;
		}
		return count;
	}

	public static int lineCount(String input) {
		System.out.println("WTF");
		logger.info("counting lines for {}", asHex(input));
		final Matcher m = newlinePattern.matcher(input);
		int count = 0;
		int matcherEnd = -1;
		while (m.find()) {
			matcherEnd = m.end();
			count++;
		}
		logger.info("matcherEnd {}", matcherEnd);
		if (matcherEnd < input.length()) {
			count++;
		}

		return count;
	}

	public static String asHex(byte[] buf) {
		final char[] chars = new char[2 * buf.length];
		for (int i = 0; i < buf.length; ++i) {
			chars[2 * i] = HEX_CHARS[(buf[i] & 0xF0) >>> 4];
			chars[(2 * i) + 1] = HEX_CHARS[buf[i] & 0x0F];
		}
		return new String(chars);
	}

	public static String asHex(String string) {
		return asHex(string.getBytes());
	}
	
	public static String padRight(String s, int n) {
		return String.format("%1$-" + n + "s", s);
	}

	public static String padLeft(String s, int n) {
		return String.format("%1$" + n + "s", s);
	}

	public static String padLeft(String s, int n, char padChar) {
		final StringBuilder sb = new StringBuilder();
		final int padLength = s.length() - n;
		for (int i = 0; i < padLength; i++) {
			sb.append(padChar);
		}
		sb.append(s);
		return sb.toString();
	}

	public static String padRight(String s, int n, char padChar) {
		final StringBuilder sb = new StringBuilder(s);
		final int padLength = s.length() - n;
		for (int i = 0; i < padLength; i++) {
			sb.append(padChar);
		}
		return sb.toString();
	}
	
    public static String[] getLines(String in) {
		return in.split("\n|\r|\r\n");  
    }
    
	public static String getLastLine(String in) {
		String[] lines = getLines(in);
		return lines[lines.length - 1];
	}
	
	public static String getFirstLine(String in) {
		return getLines(in)[0];
	}

}
