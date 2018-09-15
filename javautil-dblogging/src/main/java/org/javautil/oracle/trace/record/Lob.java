package org.javautil.oracle.trace.record;

import java.util.HashMap;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public  abstract class Lob extends TimeAndIO {
    


    public static final String exctendRegex = "XCTEND rlbk=(\\d*), rd_only=(\\d*), tim=(\\d*)";
 //   private static final 
//    LOBPGSIZE: type=TEMPORARY LOB,bytes=8132,c=16,e=17,p=0,cr=0,cu=0,tim=648119653
//            LOBREAD: type=TEMPORARY LOB,bytes=32528,c=99,e=577,p=8,cr=8,cu=0,tim=648123782


    protected static final Pattern bytesPattern  = Pattern.compile("bytes=(\\d*)");
    protected static final Pattern typePattern  = Pattern.compile("type=([^,]*)");
    
    public static final String TEMPORARY_LOB = "Temporary Lob";

    public static final HashMap<String,String> lobTypes = new HashMap<>();
   
    private final int bytes;
    private  final String  lobType;



    private static final Logger logger = LoggerFactory.getLogger(Lob.class);
    
    static {
        lobTypes.put(TEMPORARY_LOB, TEMPORARY_LOB);
       
    }
//    LOBPGSIZE: type=TEMPORARY LOB,bytes=8132,c=16,e=17,p=0,cr=0,cu=0,tim=648119653
//            LOBREAD: type=TEMPORARY LOB,bytes=32528,c=99,e=577,p=8,cr=8,cu=0,tim=648123782


    public Lob(int lineNumber, final String text) {
        super(lineNumber, text);
        //
        Matcher bytesMatcher = bytesPattern.matcher(text);
        bytesMatcher.find();
        bytes =(Integer.parseInt(bytesMatcher.group(1)));
        //
        Matcher typeMatcher = typePattern.matcher(text);
        typeMatcher.find();
        String lobTypeString = typeMatcher.group(1);
        String knownType = lobTypes.get(lobTypeString);
        if (knownType == null) {
            logger.warn("Unknown lobType {}", lobTypeString);
            lobType = lobTypeString;
        } else {
            lobType = knownType;
        }
       
       
}


    
    public String getLobType() {
        return lobType;
    }
    public int getBytes() {
        return bytes;
    }


    
}
