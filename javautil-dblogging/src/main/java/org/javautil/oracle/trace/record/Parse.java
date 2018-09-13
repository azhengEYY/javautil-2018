/**
 * @(#) Parse.java
 */

package org.javautil.oracle.trace.record;

import java.util.regex.Pattern;

/**
*
*/
public class Parse extends CursorOperation {
    
   

    
    public Parse(final String record, final int lineNumber) {
        super(lineNumber, record);
 
    }

    @Override
    public RecordType getRecordType() {
        return RecordType.PARSE;
    }



    


  
}
