package com.pacificdataservices.pdssr;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

public class LoadFileInputStreamSource implements FilenameFilter {
    
    private final File[] files;
    int index = 0;
    
    private HashMap<InputStream,File> fileByInputStream = new HashMap<>();
    
    public LoadFileInputStreamSource() {

            final String loadFileDir = "src/test/resources/dataloads";
            final File loadDirFile = new File(loadFileDir);
            files = loadDirFile.listFiles(this);
            Arrays.sort(files);
           
        
    }


    public synchronized InputStream next() throws FileNotFoundException {
        InputStream retval = null;
        if (index < files.length) {
 
            File f = files[index];
            retval = new FileInputStream(f);
            fileByInputStream.put(retval,f);
            index++;
        }
        return retval;
        
    }
    
    public void success(InputStream is) {
        fileByInputStream.remove(is);
        // TODO record
        
    }
    
    public void file(InputStream is) {
        fileByInputStream.remove(is);
        // TODO record
        
    }
    
    @Override
    public boolean accept(File dir, String fileName) {
        return fileName.endsWith(".cds");
    }
    
    public File getFile(InputStream is) {
        return fileByInputStream.get(is);
    }

}
