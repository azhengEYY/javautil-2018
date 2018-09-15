package org.javautil.oracle.trace.record;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class LobpgsizeTest {

    @Test
    public void test() {
        String text = "LOBPGSIZE: type=TEMPORARY LOB,bytes=8132,c=16,e=17,p=8,cr=7,cu=1,tim=648119653\n" +
                "";
        LobPgsize lob = new LobPgsize(text, 0);
        assertEquals("TEMPORARY LOB", lob.getLobType());
        assertEquals(1, lob.getCurrentModeBlocks());
        assertEquals(8132, lob.getBytes());
        assertEquals(17, lob.getElapsedMicroSeconds());
        assertEquals(7, lob.getConsistentReadBlocks());
        assertEquals(8, lob.getPhysicalBlocksRead());
        assertEquals(648119653L, lob.getTime());

    }


}
