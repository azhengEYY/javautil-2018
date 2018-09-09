package org.javautil.lang;

public class ThreadUtil {

	public static String getStackTrace() {

		final StringBuilder sb = new StringBuilder();
		for (final StackTraceElement frame : Thread.currentThread().getStackTrace()) {
			sb.append(String.format("%6d %s %s\n", frame.getLineNumber(), frame.getClassName(), frame.getMethodName()));
		}
		return sb.toString();
	}
	
	public static String getStackTrace(int skip) {

        final StringBuilder sb = new StringBuilder();
        int i = 1;
        for (final StackTraceElement frame : Thread.currentThread().getStackTrace()) {
            if (i++ > skip) {
            sb.append(String.format("%6d %s %s\n", frame.getLineNumber(), frame.getClassName(), frame.getMethodName()));
            }
        }
        return sb.toString();
    }
}
