package org.javautil.oracle.trace.record;



import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class FetchTest {
    
    @Test
    public void test() {
        String text = "FETCH #139721389981080:c=43,e=42,p=0,cr=2,cu=3,mis=5,r=7,dep=1,og=4,plh=1227530427,tim=11885913409\n";
        Exec exec = new Exec(text,0);
        assertEquals(new Long("139721389981080"),exec.getCursorNumber());
        assertEquals(43,exec.getCpu());
        assertEquals(42,exec.getElapsedMicroSeconds());
        assertEquals(0,exec.getPhysicalBlocksRead());
        assertEquals(3,exec.getCurrentModeBlocks());

        assertEquals(5,exec.getLibraryCacheMissCount());
        assertEquals(7,exec.getRowCount());
        assertEquals(1,exec.getDepth());
        // TODO what is STR
        assertEquals(4,exec.getOptimizerGoal());
        // TODO plh
       assertEquals(11885913409L,exec.getTime());

    }



}
