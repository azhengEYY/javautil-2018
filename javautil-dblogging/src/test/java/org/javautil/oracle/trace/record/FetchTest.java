package org.javautil.oracle.trace.record;



import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class FetchTest {
    
    @Test
    public void test() {
        String text = "FETCH #139721389981080:c=43,e=42,p=0,cr=2,cu=3,mis=5,r=7,dep=1,og=4,plh=1227530427,tim=11885913409\n";
        Fetch fetch = new Fetch(text,0);
        assertEquals(139721389981080L,fetch.getCursorNumber());
        assertEquals(43,fetch.getCpu());
        assertEquals(42,fetch.getElapsedMicroSeconds());
        assertEquals(0,fetch.getPhysicalBlocksRead());
        assertEquals(3,fetch.getCurrentModeBlocks());

        assertEquals(5,fetch.getLibraryCacheMissCount());
        assertEquals(7,fetch.getRowCount());
        assertEquals(1,fetch.getRecursionDepth());
        // TODO what is STR
        assertEquals(4,fetch.getOptimizerGoal());
        // TODO plh
       assertEquals(11885913409L,fetch.getTime());

    }



}
