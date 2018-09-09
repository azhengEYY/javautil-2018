package org.javautil.text;

import static org.junit.Assert.assertEquals;

import java.util.List;


import org.javautil.text.CSVTokenizer;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CSVTokenizerTest {

	CSVTokenizer tokenizer = new CSVTokenizer();

	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Test
	public void test1() {
		final String csvString = "1,2,\"toad\"";
		final List<String> parsed = tokenizer.parse(csvString);
		assertEquals(3, parsed.size());
		assertEquals("1", parsed.get(0));
		assertEquals("2", parsed.get(1));
		assertEquals("toad", parsed.get(2));

	}

	/**
	 * looks like 1,2,"toad"" should be 1,2,"toad""" so it is missing an end
	 * quote.
	 */
	@Test(expected = org.javautil.text.TokenizingException.class)
	public void test2() {
		final String csvString = "1,2,\"toad\"\"";
		tokenizer.parse(csvString);

	}

	/**
	 * looks like should be ,1,2,"toad"
	 */
	@Test
	public void test3() {
		final String csvString = ",1,2,\"toad\"";
		final List<String> parsed = tokenizer.parse(csvString);
		assertEquals(4, parsed.size());

		logger.debug(tokenizer.parse(csvString).toString());
		assertEquals("", parsed.get(0));

		assertEquals("1", parsed.get(1));
		assertEquals("2", parsed.get(2));
		assertEquals("toad", parsed.get(3));

	}

	/**
	 * looks like should be ,1,2,toad
	 */
	@Test
	public void test4() {
		final String csvString = ",~1,2,~toad~";
		final List<String> parsed = tokenizer.parse(csvString);
		assertEquals(4, parsed.size());
		assertEquals("", parsed.get(0));
		assertEquals("~1", parsed.get(1));
		assertEquals("2", parsed.get(2));
		assertEquals("~toad~", parsed.get(3));

	}
	
	@Test
    public void testCommaInFieldNoSpace() {
        final String csvString = "\"0000000160\",\"CADBURY\",\"Cadbury Adam USA, LLC\"";
        final List<String> parsed = tokenizer.parse(csvString);
        int i = 0;
        for (String string : parsed) {
            System.out.println(i + ": '" + string + "'");
        }
        assertEquals(3, parsed.size());
        assertEquals("0000000160", parsed.get(0));
        assertEquals("CADBURY", parsed.get(1));
        assertEquals("Cadbury Adam USA, LLC", parsed.get(2));
   
    }
	
	
}
