package org.axolotlj.remotehealth.core.cmd;

import org.axolotlj.remotehealth.core.cmd.response.NetworkStatus;
import org.axolotlj.remotehealth.core.cmd.response.PreferencesStatus;
import org.axolotlj.remotehealth.core.cmd.response.SensorStatus;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class CommandResponseParser {

    private static final Gson gson = new Gson();

    public static NetworkStatus parseNetworkStatus(String response) {
        return gson.fromJson(response, NetworkStatus.class);
    }

    public static PreferencesStatus parsePreferencesStatus(String response) {
        return gson.fromJson(response, PreferencesStatus.class);
    }

    public static SensorStatus parseSensorStatus(String response) {
        return gson.fromJson(response, SensorStatus.class);
    }

    public static CommandType identifyType(String response) {
        try {
            JsonObject obj = gson.fromJson(response, JsonObject.class);
            if (obj.has("type")) {
                String typeStr = obj.get("type").getAsString();
                return CommandType.valueOf(typeStr);
            }
        } catch (Exception e) {
            return CommandType.UNKNOWN;
        }
        return CommandType.UNKNOWN;
    }

}
