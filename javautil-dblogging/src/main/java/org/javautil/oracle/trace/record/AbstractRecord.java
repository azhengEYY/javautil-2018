package org.javautil.oracle.trace.record;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractRecord implements Record {

    
    public static final String cursorNumberRegex = "#(\\d*)";


    protected static final Pattern cursorNumberPattern      = Pattern.compile(cursorNumberRegex);
    protected static final Pattern consistentReadBlocksPattern = Pattern.compile("cr=(\\d*).*");
    protected static final Pattern timePattern                 = Pattern.compile("tim=(\\d*)");
    protected static final Pattern depthPattern                 = Pattern.compile("dep=(\\d*)");

    protected int                  lineNumber;
    protected String               text;
    private static final Logger    logger                      = LoggerFactory.getLogger(AbstractRecord.class);

    public AbstractRecord(int lineNumber, String stmt) {
        this.lineNumber = lineNumber;
        this.text = stmt;

    }

    /** TODO move all of these numbers to BigDecimal and return */
    public static Integer getInt(final String recordText, final Pattern pattern, boolean required) {
        Integer returnValue = null;
        if (recordText == null) {
            throw new IllegalStateException("recordText is null");
        }
        String text = getString(recordText, pattern, false);
        if (text == null) {
            if (required) {
                String errorMessage = String.format(
                        "not found text: '%s'\n,pattern: '%s'\n,groupText: '%s'\nexception: %s",
                        recordText, pattern.pattern());
                throw new IllegalArgumentException(errorMessage);
            }
        } else {
            try {
                returnValue = new Integer(text);
            } catch (Exception nfe) {
                String errorMessage = String.format("text: '%s'\n,pattern: '%s'\n,groupText: '%s'\nexception: %s",
                        recordText, pattern.pattern(), text, nfe.getMessage());
                logger.error(errorMessage);
                throw nfe;
            }
        }

        return returnValue;
    }

    

    public static int getInt(final String recordText, final Pattern pattern) {
        if (recordText == null) {
            throw new IllegalStateException("recordText is null");
        }
        int returnValue = -1;
        final Matcher m = pattern.matcher(recordText);
        if (m.find()) {
            returnValue = Integer.parseInt(m.group(1));
        }
        return returnValue;
    }

    /**
     * 
     * @param recordText
     * @param pattern
     * @param required
     * @return null if required is false and not found else the value
     */
    public static Long getLong(final String recordText, final Pattern pattern, boolean required) {
        Long returnValue = null;
        String text = getString(recordText, pattern, false);
        if (text == null) {
            if (required) {
                String errorMessage = String.format(
                        "not found text: '%s'\n,pattern: '%s'",
                        recordText, pattern.pattern());
                throw new IllegalArgumentException(errorMessage);
            }
        } else {
            try {
                returnValue = Long.parseLong(text);
            } catch (Exception nfe) {
                String errorMessage = String.format("text: '%s'\n,pattern: '%s'\n,groupText: '%s'\nexception: %s",
                        recordText, pattern.pattern(), text, nfe.getMessage());
                logger.error(errorMessage);
                throw nfe;
            }
        }

        return returnValue;
    }

    // TODO replicate this error handling and move to javautil
    public static long getLong(final String recordText, final Pattern pattern) {
        return getLong(recordText, pattern, true);
    }

    public static String getString(final String text, final Pattern pattern) {
        return getString(text, pattern, true);
    }

    public static String getString(final String text, final Pattern pattern, boolean required) {
        String returnValue = null;
        final Matcher m = pattern.matcher(text);
        if (m.find()) {
            returnValue = m.group(1);
        } else {
            if (required) {
                String message = String.format("no match: pattern: '%s', text: '%s'", pattern.pattern(), text);
                throw new IllegalArgumentException(message);
            }
        }
        // if (returnValue == null) {
        // String errorMessage = String.format("null text: '%s'\n,pattern: '%s'\n,",
        // text, pattern.pattern());
        // throw new IllegalStateException(errorMessage);
        // }
        return returnValue;
    }

    @Override
    public final int getLineNumber() {
        return lineNumber;
    }

    @Override
    public final String getText() {
        return text;
    }

    @Override
    public String getLineAndText() {
        return String.format("#%d %s", lineNumber, text);

    }

}
