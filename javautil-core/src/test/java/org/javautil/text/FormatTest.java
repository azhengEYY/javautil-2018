package org.javautil.text;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class FormatTest {
	//https://dzone.com/articles/java-string-format-examples
	@Test
	public void StringFormatTest() {
		String result;
		
		result = String.format("%09d", 93);
		assertEquals("000000093",result);
		
		result = String.format("%09d", -93);
		assertEquals("-00000093",result);
		
		result =  String.format("%9d", 93);
		assertEquals("       93",result);
		
		result = String.format("|%+9d|", 93);
		assertEquals("|      +93|",result);
		
		result = String.format("|%,d|", 10000000);
		assertEquals("|10,000,000|",result);
		
		result = String.format("%(d", -36); // prints: |(36)|
		assertEquals("(36)",result);
		
		result = String.format("%#o", 93);
		assertEquals("0135",result);
	
		result = String.format("|%#x|", 93);
		assertEquals("|0x5d|",result);
	
		result = String.format("%#X", 93);
		assertEquals("0X5D",result);
		
		result = String.format("|%30s|", "Hello World"); 
		assertEquals("|                   Hello World|",result);// prints: | Hello World|
		
		//Specify Maximum Number of Characters
		result = String.format("%.5s", "Hello World"); 
		assertEquals("Hello",result);
	}
}
