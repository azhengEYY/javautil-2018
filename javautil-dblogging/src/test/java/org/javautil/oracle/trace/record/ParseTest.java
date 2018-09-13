package org.javautil.oracle.trace.record;



import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ParseTest {
    
    @Test
    public void test() {
        String text = "PARSE #139721389981080:c=9,e=10,p=2,cr=5,cu=7,mis=1,r=0,dep=1,og=4,plh=1227530427,tim=11885913849\n";
        Parse parse = new Parse(text,0);
        assertEquals(139721389981080L,parse.getCursorNumber());
        assertEquals(9,parse.getCpu());
        assertEquals(10,parse.getElapsedMicroSeconds());
        assertEquals(2,parse.getPhysicalBlocksRead());
        assertEquals(7,parse.getCurrentModeBlocks());

        assertEquals(1,parse.getLibraryCacheMissCount());
        assertEquals(0,parse.getRowCount());
        assertEquals(1,parse.getRecursionDepth());
    
        assertEquals(4,parse.getOptimizerGoal());
        assertEquals(1227530427,parse.getExplainHash());
       assertEquals(11885913849L,parse.getTime());

    }


}
