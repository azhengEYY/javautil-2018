package org.javautil.misc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Timer {
	private final long startTime = System.nanoTime();

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	private String name = "";

	private String info = "";

	public Timer() {

	}

	public Timer(String name) {
		this.name = name;
	}

	public Timer(Class<?> callingClass, String name, String info) {
		logger = LoggerFactory.getLogger(callingClass);
		this.name = name;
	}

	public Timer(String name, String info) {
		this.name = name;
		this.info = info;
	}

	public long getElapsedMillis() {
		return (System.nanoTime() - startTime) / 1000000;
	}

	public void logElapsed() {
		logElapsed("");
	}

	public void logElapsed(String additional) {
		final long elapsedMillis = (System.nanoTime() - startTime) / 1000000;
		logger.info(name + ": elapsed millis " + elapsedMillis + " info: " + info + " "
				+ additional);

	}
}
