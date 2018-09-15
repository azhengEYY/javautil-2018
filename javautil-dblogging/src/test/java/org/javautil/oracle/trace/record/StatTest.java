package org.javautil.oracle.trace.record;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class StatTest {
    
    @Test
    public void test() {
        String text = "STAT #140603589829656 id=4 cnt=3 pid=0 pos=2 obj=6 op='FAST DUAL  (cr=7 pr=8 pw=9 str=10 time=23 us cost=12 size=13 card=1)'";
        Stat stat = new Stat(text,0);
        assertEquals(140603589829656L,stat.getCursorNumber());
        assertEquals(4,stat.getId());
        assertEquals(3,stat.getCnt());
        assertEquals(2,stat.getPosition());
        assertEquals(6,stat.getObj());
        assertEquals("FAST DUAL",stat.getOperation());
        assertEquals(7,stat.getConsistentReads());
        assertEquals(8,stat.getPhysicalReads());
        assertEquals(9,stat.getPhysicalWrites());
        // TODO what is STR
        assertEquals(23,stat.getTime());
       assertEquals(new Long(12),stat.getCost());
       assertEquals(new Long(13),stat.getSize());
       assertEquals(new Integer(1),stat.getCardinality());
    }


    @Test
    public void testNoSize() {
        String text = "STAT #140176925801880 id=4 cnt=2 pid=0 pos=1 obj=0 op='CONNECT BY WITH FILTERING (cr=3 pr=9 pw=5 str=1 time=107 us)";
        Stat stat = new Stat(text,0);
        assertEquals(140176925801880L,stat.getCursorNumber());
        assertEquals(4,stat.getId());
        assertEquals(2,stat.getCnt());
        assertEquals(1,stat.getPosition());
        assertEquals(0,stat.getObj());
        assertEquals("CONNECT BY WITH FILTERING",stat.getOperation());
        assertEquals(3,stat.getConsistentReads());
        assertEquals(9,stat.getPhysicalReads());
        assertEquals(5,stat.getPhysicalWrites());
        // TODO what is STR
        assertEquals(107,stat.getTime());
       assertNull(stat.getCost());
       assertNull(stat.getSize());
       assertNull(stat.getCardinality());
    }


}
