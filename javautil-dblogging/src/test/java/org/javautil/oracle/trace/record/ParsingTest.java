package org.javautil.oracle.trace.record;



import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

public class ParsingTest {
    
    private static final String text = "PARSING IN CURSOR #140603589769800 len=49 dep=2 uid=0 oct=3 lid=0 tim=2359115366 hv=1758550401 ad='60f33380' sqlid='0a7q9v9nd2qc1";
    private static final Pattern    parsingCursorNumberPattern = Pattern.compile("PARSING IN CURSOR #(\\d*).*");
    
    @Test
    public void testdirect() {
        Matcher matcher = parsingCursorNumberPattern.matcher(text);
        assertTrue(matcher.matches());
        String cursorNumberText = matcher.group(1);
        assertEquals("140603589769800",cursorNumberText);  
    }
    
    @Test
    public void testdirect2() {
        final String text = "PARSING IN CURSOR #140603589769800 len=49 dep=2 uid=0 oct=3 lid=0 tim=2359115366 hv=1758550401 ad='60f33380' sqlid='0a7q9v9nd2qc1";
        final Pattern    parsingCursorNumberPattern = Pattern.compile("PARSING.*");
        
        Matcher matcher = parsingCursorNumberPattern.matcher(text);
        assertTrue(matcher.matches());
//        String cursorNumberText = matcher.group(1);
//        assertEquals("140603589769800",cursorNumberText);  
    }
    
    @Test
    public void testdirect3() {
        final String text = "PARSING IN CURSOR #140603589769800 len=49 dep=2 uid=0 oct=3 lid=0 tim=2359115366 hv=1758550401 ad='60f33380' sqlid='0a7q9v9nd2qc1";
        final Pattern    parsingCursorNumberPattern = Pattern.compile("PARSING IN CURSOR.*");
        
        Matcher matcher = parsingCursorNumberPattern.matcher(text);
        assertTrue(matcher.matches());
//        String cursorNumberText = matcher.group(1);
//        assertEquals("140603589769800",cursorNumberText);  
    }
}

    
//    @Test
//    public void test() {
//        String text = "PARSING IN CURSOR #140603589769800 len=49 dep=2 uid=0 oct=3 lid=0 tim=2359115366 hv=1758550401 ad='60f33380' sqlid='0a7q9v9nd2qc1";
//
//        Parsing parse = new Parsing(text,0);
//        assertEquals(new Long("140603589769800"),parse.getCursorNumber());
//        assertEquals(49,parse.getSqlTextLength());
//        assertEquals(10,parse.getElapsedMicroSeconds());
//        assertEquals(2,parse.getPhysicalBlocksRead());
//        assertEquals(7,parse.getCurrentModeBlocks());
//
//        assertEquals(1,parse.getLibraryCacheMissCount());
//        assertEquals(0,parse.getRowCount());
//        assertEquals(1,parse.getDepth());
//    
//    //    assertEquals(4,parse.getOptimizerGoal());
//        assertEquals(1227530427,parse.getExplainHash());
//       assertEquals(11885913849L,parse.getTime());
//
//    }
//
//
//
//
//}
