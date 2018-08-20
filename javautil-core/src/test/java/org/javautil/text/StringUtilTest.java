package org.javautil.text;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class StringUtilTest {

	@Test
	public void oneLineNoNewline() {
		final String text = "text";
		assertEquals(0,StringUtil.newlineCount(text));
		assertEquals(1,StringUtil.lineCount(text));
	}

	@Test
	public void oneLineNewline() {
		final String text = "text with newline\n";
		assertEquals(1,StringUtil.newlineCount(text));
		assertEquals(1,StringUtil.lineCount(text));
	}

	@Test
	public void oneNewLine() {
		final String text = "\n";
		assertEquals(1,StringUtil.newlineCount(text));
		assertEquals(1,StringUtil.lineCount(text));
	}

	@Test
	public void oneCRNewLine() {
		final String text = "\r\n";
		assertEquals(1,StringUtil.newlineCount(text));
		assertEquals(1,StringUtil.lineCount(text));
	}

	@Test
	public void trailingNewLines() {
		final String text = "line1\n\n\n";
		assertEquals(3,StringUtil.newlineCount(text));
		assertEquals(3,StringUtil.lineCount(text));
	}


	@Test
	public void intermediateNewLinesNoFinalNewLine() {
		final String text = "line1\n\n\nLine 4";
		assertEquals(3,StringUtil.newlineCount(text));
		assertEquals(4,StringUtil.lineCount(text));
	}

	@Test
	public void blankLinesNoNewLineAtEnd() {
		final String text = "line 1\n\n\nline 4";
		assertEquals(3,StringUtil.newlineCount(text));
		assertEquals(4,StringUtil.lineCount(text));
	}


}
