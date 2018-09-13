package org.javautil.oracle.trace.record;

import static org.junit.Assert.*;

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
       assertEquals(12,stat.getCost());
       assertEquals(13,stat.getSize());
       assertEquals(1,stat.getCardinality());
    }



}
