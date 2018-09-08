package org.javautil.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.javautil.io.ResourceHelper;

public class PropertiesFactory {

    public static Properties getProperties(Object invoker, String resourceName) throws IOException {
        Properties properties = new Properties();
        InputStream input = ResourceHelper.getResourceAsInputStream(invoker, "application.properties");
        properties.load(input);   
        input.close();
        return properties;
    }
    
    public static Properties getProperties(String fileName) throws IOException {
        return getProperties(new File(fileName));
 
    }

    public static Properties getProperties(File file) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        Properties properties = new Properties();
        properties.load(fis);
        fis.close();
        return properties;
    }

}
