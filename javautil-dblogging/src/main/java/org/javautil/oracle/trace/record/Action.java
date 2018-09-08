/**
 * @(#) Xctend.java
 */

package org.javautil.oracle.trace.record;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * XCTEND rlbk=%d rd_only=%d
 * ----------------------------------------------------------------------------
 * XCTEND A transaction end marker.
 * 
 * rlbk 1 if a rollback was performed, 0 if no rollback (commit). rd_only 1 if
 * transaction was read only, 0 if changes occurred.
 */

// @todo parse
public class Action extends AbstractRecord {
    
    protected static final Pattern recordPattern  = Pattern.compile("ACTION NAME:\\((.*)\\) (.*)");
    
    private String actionName;
    private String timestampString;

    public Action(final String text, final int lineNumber) {
        super(lineNumber, text);
        final Matcher m = recordPattern.matcher(text);
        if (m.find()) {
            actionName = m.group(1).trim();
            timestampString = m.group(2);
        } else {
            throw new IllegalStateException("failed to parse '" + text + "'");
        }


    }

    @Override
    public RecordType getRecordType() {
        return RecordType.ACTION;
    }
    
  
    public String getActionName() {
    
        return actionName;
    }

  
    public String getTimestampString() {

        return timestampString;
    }
}
