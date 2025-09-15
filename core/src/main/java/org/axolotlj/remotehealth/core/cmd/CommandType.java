package org.axolotlj.remotehealth.core.cmd;

public enum CommandType {

	NETWORK_STATUS("ShowNetworkStatus"),
	SENSORS_STATUS("ShowSensorStatus"),
	PREFERENCES_STATUS("ShowPreferences"),
	
	SET_STA_CREDENTIALS("SetSTACredentials"),
	SET_AP_CREDENTIALS("SetAPCredentials"),
	
	SET_BRIGHNESS("SetBrightness"),
	
    RESTART_SYSTEM("RestartSystem"),

    UNKNOWN("unknown");

    private final String commandText;

    CommandType(String commandText) {
        this.commandText = commandText;
    }

    public String getCommandText() {
        return commandText;
    }
}