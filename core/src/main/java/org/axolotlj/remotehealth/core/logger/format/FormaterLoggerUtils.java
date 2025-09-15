package org.axolotlj.remotehealth.core.logger.format;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.axolotlj.remotehealth.core.logger.LogLevel;

public class FormaterLoggerUtils {
	
	private static final String PREFIX = "Remote Health";
	private static final DateTimeFormatter LOG_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

	public static String formatLogLine(LogLevel level, String message) {
		String timestamp = LocalDateTime.now().format(LOG_FORMATTER);
		String threadName = Thread.currentThread().getName();
		String source = getCallerSource();
		return String.format("[%s] [%s] [%-5s] [%s/%s]: %s", PREFIX, timestamp, level.getLabel(), threadName, source, message);
	}

	public static String getCallerSource() {
	    StackTraceElement[] stack = Thread.currentThread().getStackTrace();

	    for (StackTraceElement element : stack) {
	        String className = element.getClassName();

	        // Ignorar clases internas del logger
	        if (className.equals(Thread.class.getName())) continue;
	        if (className.startsWith("org.axolotlj.remotehealth.core.logger")) continue;

	        // Ahora sí, este debería ser el "caller real"
	        String simpleClassName = className.substring(className.lastIndexOf('.') + 1);
	        String methodName = element.getMethodName();
	        return simpleClassName + "." + methodName;
	    }
	    return "UnknownSource";
	}


	
}
