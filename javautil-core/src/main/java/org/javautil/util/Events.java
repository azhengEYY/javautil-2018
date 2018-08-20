package org.javautil.util;

import java.util.TreeMap;

// TODO finish
public class Events extends TreeMap<String,Integer>{  
	
    public boolean exists(Class<?> clazz, String name) {
        String eventName = clazz.getName() + "." + name;
        return (super.get(eventName) != null);
    }
    
    public String getEventName(Class<?> clazz, String name) {
           return clazz.getName() + "." + name;
    }
    
    public void add(Class<?> clazz, String ... names ) {
        for (String name : names) {
            super.put(getEventName(clazz,name),1);
        }
    }
    


}
