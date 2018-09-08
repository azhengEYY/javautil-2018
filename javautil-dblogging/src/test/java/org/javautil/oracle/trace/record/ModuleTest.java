package org.javautil.oracle.trace.record;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ModuleTest {
    @Test
    public void test() {
        String text = "*** MODULE NAME:(CdsDataLoader) 2018-09-07T19:16:38.396305-05:00\n";
        Module module = new Module(text,0);
        assertEquals("CdsDataLoader",module.getModuleName());
        assertEquals("2018-09-07T19:16:38.396305-05:00",module.getTimestampString());
 

    }

}
