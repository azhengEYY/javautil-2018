package org.javautil.oracle.trace.record;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class XctendTest {

    @Test
    public void test() {
        String text = "XCTEND rlbk=0, rd_only=1, tim=12205299536\n";
        Xctend operation = new Xctend(text, 0);
        assertEquals(0, operation.getRollback());
        assertEquals(1, operation.getReadOnly());
        assertEquals(12205299536L, operation.getTime());

    }

}
