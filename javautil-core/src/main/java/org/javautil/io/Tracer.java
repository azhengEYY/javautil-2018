package org.javautil.io;

import java.io.Closeable;
import java.io.FileWriter;
import java.io.IOException;

import org.javautil.util.Events;

public class Tracer implements Closeable{
	private String fileName;
	private FileWriter fileWriter;
	
	private Events events = new Events();

	public Tracer() {
		
	}
	
	public Tracer(Events events) {
	    this.events = events;
	}
	
	public Tracer(String fileName) throws IOException {
		this.fileName = fileName;
		this.fileWriter = new FileWriter(fileName);
	}
	
	public void traceln(String text)  {
		if (fileWriter  != null) {
			try {
				fileWriter.write(text);
				fileWriter.write("\n");
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		
		}
	}
	
	public boolean isTracing() {
		return fileWriter != null;
	}

	@Override
	public void close() throws IOException {
		fileWriter.close();
		
	}

    public String getFileName() {
        return fileName;
    }


}
