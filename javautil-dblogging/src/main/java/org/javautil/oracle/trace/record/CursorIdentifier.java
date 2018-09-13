package org.javautil.oracle.trace.record;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

// TODO eliminate Autoboxing
// TODO no idea why SQLID is attempted to be maintained here eliminate
public abstract class CursorIdentifier extends AbstractRecord  {

    private static final Logger logger = LoggerFactory.getLogger(CursorIdentifier.class);
  
    
    public static final String cursorNumberRegex = "#(\\d*)";


    protected static final Pattern cursorNumberPattern      = Pattern.compile(cursorNumberRegex);


    private Long                   cursorNumber;

    private String                 sqlid;


    public CursorIdentifier(int lineNumber, String stmt) {
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



    public String getSqlid() {
        return sqlid;
    }

 
    public void setSqlid(String sqlid) {
        this.sqlid = sqlid;
    }



//    @Override
//    public String toString() {
//        Gson gson = new GsonBuilder().setPrettyPrinting().create();
//        return gson.toJson(this);
//    }
    
    public long getCursorNumber() {
        return cursorNumber;
    }
    
   

}
