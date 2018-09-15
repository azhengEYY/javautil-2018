package org.javautil.oracle.trace.record;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

public class LobreadTest {
    
    @Test
    public void test() {
        String text = "LOBREAD: type=TEMPORARY LOB,bytes=32528,c=99,e=577,p=8,cr=7,cu=1,tim=648123782\n";
        Lobread lob = new Lobread(text,0);
        assertEquals(1,lob.getCurrentModeBlocks());
        assertEquals(32528,lob.getBytes());
        assertEquals(577,lob.getElapsedMicroSeconds());
        assertEquals(7,lob.getConsistentReadBlocks());
        assertEquals(8,lob.getPhysicalBlocksRead());
       assertEquals(648123782L,lob.getTime());
    
    }
    
    @Test
    public void test2() {
        String text = "LOBREAD: type=TEMPORARY LOB,bytes=32528,c=99,e=577,p=8,cr=7,cu=1,tim=648123782\n";
        final Pattern elapsedMicrosecondsPattern   = Pattern.compile(",e=(\\d*)");
        Matcher matcher = elapsedMicrosecondsPattern.matcher(text);
        boolean found = matcher.find();
        assertTrue(found);
        assertEquals("577",matcher.group(1));
        
        
    }



}
