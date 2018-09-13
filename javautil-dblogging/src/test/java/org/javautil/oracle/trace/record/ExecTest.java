package org.javautil.oracle.trace.record;



import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ExecTest {
    
    @Test
    public void test() {
        String text = "EXEC #139721389981080:c=132,e=134,p=64,cr=4,cu=8,mis=16,r=32,dep=1,og=2,plh=1227530427,tim=11885913351\n";
        Exec exec = new Exec(text,0);
        assertEquals(139721389981080L,exec.getCursorNumber());
        assertEquals(132,exec.getCpu());
        assertEquals(134,exec.getElapsedMicroSeconds());
        assertEquals(64,exec.getPhysicalBlocksRead());
        assertEquals(8,exec.getCurrentModeBlocks());

        assertEquals(16,exec.getLibraryCacheMissCount());
        assertEquals(32,exec.getRowCount());
        assertEquals(1,exec.getRecursionDepth());
        // TODO what is STR
        assertEquals(2,exec.getOptimizerGoal());
        // TODO plh
       assertEquals(11885913351L,exec.getTime());

    }



}
