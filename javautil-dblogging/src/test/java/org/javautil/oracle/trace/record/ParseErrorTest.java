package org.javautil.oracle.trace.record;



import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ParseErrorTest {
    
    @Test
    public void test() {
        String text = "PARSE ERROR #140729315264320:len=3885 dep=3 uid=105 oct=1 lid=107 tim=100406013140 err=922";
       assertEquals(RecordType.PARSE_ERROR, RecordType.getRecordType(text));
        //  String text = "PARSE #139721389981080:c=9,e=10,p=2,cr=5,cu=7,mis=1,r=0,dep=1,og=4,plh=1227530427,tim=11885913849\n";
       
        ParseError parseError = new ParseError(0,text);
        assertEquals(140729315264320L,parseError.getCursorNumber());
    //    assertEquals(3885,parseError());
        // TODO take this out of the hierarchy
     //   assertEquals(9,parseError.getCpu());
        assertEquals(3885,parseError.getSqlTextLength());
        assertEquals(3,parseError.getRecursionDepth());
        assertEquals(105,parseError.getUid());
        assertEquals(1,parseError.getOracleCommandType());
        assertEquals(107,parseError.getLid());
        assertEquals(100406013140L,parseError.getTimestamp());
        assertEquals(922,parseError.getErrorNumber());

    }



}
