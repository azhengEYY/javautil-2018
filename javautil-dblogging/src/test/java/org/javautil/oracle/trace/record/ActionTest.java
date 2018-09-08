package org.javautil.oracle.trace.record;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ActionTest {
    @Test
    public void test() {
        
        String text = "*** ACTION NAME:(loadFile) 2018-09-07T19:16:43.772375-05:00\n";
      
        Action action = new Action(text,0);
        assertEquals("loadFile",action.getActionName());
        assertEquals("2018-09-07T19:16:43.772375-05:00",action.getTimestampString());
 

    }

    @Test
    public void testNoAction() {
        
   
        String text = "*** ACTION NAME:( ) 2018-09-07T19:16:43.779139-05:00\n";

        Action action = new Action(text,0);
        assertEquals("",action.getActionName());
        assertEquals("2018-09-07T19:16:43.779139-05:00",action.getTimestampString());
 

    }
}
