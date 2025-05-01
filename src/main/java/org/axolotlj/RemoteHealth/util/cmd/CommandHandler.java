package org.axolotlj.RemoteHealth.util.cmd;

public class CommandHandler {
	public final static String PREFIX = "RESP:";
	
	public enum CommandType {

		NETWORK_STATUS("ShowNetworkStatus","RESP:NETWORK_STATUS"),
		SENSORS_STATUS("ShowSensorStatus","RESP:SENSOR_STATUS"),
		PREFERENCES_STATUS("ShowPreferences","RESP:PREFERENCES"),
		
		CHANGE_SSID_STA("SetValue ssid-sta ","RESP:CHANGE_SSID_STA"),
		CHANGE_PASSWORD_STA("SetValue password-sta ","RESP:CHANGE_PASSWORD_STA"),
		CHANGE_SSID_AP("SetValue ssid-ap ","RESP:CHANGE_SSID_AP"),
		CHANGE_PASSWORD_AP("SetValue password-ap ","RESP:CHANGE_PASSWORD_AP"),
		
	    RESTART_SYSTEM("RestartSystem", "RESP:ESTARTING"),

	    UNKNOWN("unknown", "RESP:UNKNOWN");

	    private final String commandText;
	    private final String expectedResponsePrefix;

	    CommandType(String commandText, String expectedResponsePrefix) {
	        this.commandText = commandText;
	        this.expectedResponsePrefix = expectedResponsePrefix;
	    }

	    public String getCommandText() {
	        return commandText;
	    }

	    public String getExpectedResponsePrefix() {
	        return expectedResponsePrefix;
	    }

	    public static CommandType fromResponse(String response) {
	        for (CommandType type : values()) {
	            if (response.startsWith(type.expectedResponsePrefix)) {
	                return type;
	            }
	        }
	        return UNKNOWN;
	    }

	    public static CommandType fromCommandText(String text) {
	        for (CommandType type : values()) {
	            if (text.equalsIgnoreCase(type.commandText)) {
	                return type;
	            }
	        }
	        return UNKNOWN;
	    }
	}

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
