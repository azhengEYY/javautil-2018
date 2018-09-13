package org.javautil.oracle.trace.record;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

// TODO eliminate Autoboxing
public abstract class AbstractCursorEvent extends AbstractRecord implements CursorRecord {

    private static final Logger logger = LoggerFactory.getLogger(AbstractCursorEvent.class);
  
    // stat fields
    
    public static final String cursorNumberRegex = "#(\\d*)";

    protected static final Pattern idPattern                = Pattern.compile("id=(\\d*)");

    protected static final Pattern cntPattern               = Pattern.compile("cnt=(\\d*)");
    protected static final Pattern parentIdPattern          = Pattern.compile("pid=(\\d*)");
    protected static final Pattern posPattern               = Pattern.compile("pos=(\\d*).*");
    protected static final Pattern objPattern               = Pattern.compile("obj=(\\d*)");
    protected static final Pattern operationPattern         = Pattern.compile("op='(.*) \\(");
    protected static final Pattern currentReadPattern       = Pattern.compile("\\(cr=(\\d*)");
    protected static final Pattern physicalReadPattern      = Pattern.compile("pr=(\\d*)");
    protected static final Pattern physicalWritePattern     = Pattern.compile("pw=(\\d*)");
    protected static final Pattern timePattern              = Pattern.compile("time='(\\d*)");
    protected static final Pattern cursorNumberPattern      = Pattern.compile(cursorNumberRegex);

    // private static Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private Long                   cursorNumber;

    private String                 sqlid;

  // String                 cursorId;

    public AbstractCursorEvent(int lineNumber, String stmt) {
        super(lineNumber, stmt);
        Matcher matcher = cursorNumberPattern.matcher(stmt);
        matcher.find();
        String cursorNumberText;
        try {
            cursorNumberText = matcher.group(1);
        } catch (Exception e) {
            String message = String.format("regex: '%s', text: '%s' exception: %s", cursorNumberRegex,stmt,e.getMessage());
            logger.error(message);
            throw new RuntimeException(e);
        }
        cursorNumber = Long.parseLong(cursorNumberText);
        
    }


  
    @Override
    public String getSqlid() {
        return sqlid;
    }

    @Override
    public void setSqlid(String sqlid) {
        this.sqlid = sqlid;
    }

//    @Override
//    public String getCursorId() {
//        return cursorId;
//    }
//
//    public void setCursorId(String cursorId) {
//        this.cursorId = cursorId;
//    }

    @Override
    public String toString() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(this);
    }
    
    public long getCursorNumber() {
        return cursorNumber;
    }
    
   

}
