/**
 * @(#) Xctend.java
 */

package org.javautil.oracle.trace.record;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Module extends AbstractRecord {
    
    protected static final Pattern recordPattern  = Pattern.compile("MODULE NAME:\\((.*)\\) (.*)");


    private String moduleName;
    private String timestampString;
    public Module(final String text, final int lineNumber) {
        super(lineNumber, text);
        final Matcher m = recordPattern.matcher(text);
        if (m.find()) {
            moduleName = m.group(1);
            timestampString = m.group(2);
        } else {
            throw new IllegalStateException("failed to parse '" + text + "'");
        }

    }

    // TODO put a get record type in RecordType
    @Override
    public RecordType getRecordType() {
        return RecordType.MODULE;
    }

  
    public String getModuleName() {
    
        return moduleName;
    }

  
    public String getTimestampString() {

        return timestampString;
    }
}
