package org.javautil.oracle.trace.record;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public abstract class Depth extends AbstractRecord {
    private static final String depthRegex = ".*dep=(\\d*).*";
    private static final  Pattern depthPattern                 = Pattern.compile(depthRegex);
    private Logger logger = LoggerFactory.getLogger(Depth.class);
 
    
    private int depth = -1;
    
    public Depth(int lineNumber, String traceRecord) {
    
        super(lineNumber,traceRecord);
        System.out.println("in Depth constructor");
        Matcher depthMatcher = depthPattern.matcher(traceRecord);
        boolean matches = depthMatcher.matches();
        boolean found = depthMatcher.find();
        String status = String.format("matches %s, found %s text %s",matches,found, traceRecord);
        System.out.println("status: " + status);
        if (! depthMatcher.matches()) {
            String message = String.format("no match: %s\n%s",depthRegex,traceRecord);
            throw new IllegalStateException(message);
        } 

        depth = Integer.parseInt(depthMatcher.group(1));
        System.out.println("depth is " + depth);

        
    }

    public int getRecursionDepth() {
        return depth;
    }

 




}
