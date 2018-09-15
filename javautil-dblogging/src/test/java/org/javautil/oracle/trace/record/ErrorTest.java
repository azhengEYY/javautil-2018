package org.javautil.oracle.trace.record;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ErrorTest {

    @Test
    public void test() {
        String text = "ERROR #140176822405936:err=955 tim=100405783650\n";
        Error error = new Error(0, text);
        assertEquals(140176822405936L, error.getCursorNumber());
        assertEquals(100405783650L, error.getTime());
    }
}
