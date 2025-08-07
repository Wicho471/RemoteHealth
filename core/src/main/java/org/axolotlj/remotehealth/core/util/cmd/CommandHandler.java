package org.axolotlj.remotehealth.core.util.cmd;

public class CommandHandler {
	public final static String PREFIX = "RESP:";

	public static String extractResponseContent(String response) {
	    if (response == null || response.isBlank()) return "";

	    CommandType type = CommandType.fromResponse(response);
	    String prefix = type.getExpectedResponsePrefix();

	    if (type == CommandType.UNKNOWN || !response.startsWith(prefix)) {
	        return "";
	    }

	    return response.substring(prefix.length() + 1);
	}
}
