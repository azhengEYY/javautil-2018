package org.javautil.oracle.trace.record;



import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class CloseTest {
    
    @Test
    public void test() {
        String text = "CLOSE #139721389981080:c=2,e=3,dep=1,type=3,tim=11885913930\n";
        Close close = new Close(text,0);
        assertEquals(new Long("139721389981080"),close.getCursorNumber());
        assertEquals(2,close.getCpu());
        assertEquals(3,close.getElapsedMicroSeconds());

        assertEquals(1,close.getDepth());
       assertEquals(11885913930L,close.getTime());

    }



}
