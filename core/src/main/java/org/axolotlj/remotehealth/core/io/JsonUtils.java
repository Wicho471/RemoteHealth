package org.axolotlj.remotehealth.core.io;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.UUID;

import org.axolotlj.remotehealth.core.logger.DataLogger;
import org.axolotlj.remotehealth.core.logger.Log;
import org.axolotlj.remotehealth.core.model.ConnectionData;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

public class JsonUtils {
	private static final DataLogger DATA_LOGGER = Log.get();

    public static ConnectionData parseFromJsonString(String json) throws JsonSyntaxException {
    	DATA_LOGGER.logDebug("Parseando datos -> '"+json+"'");
        JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject(); // Sospecha de error
        return parseConnectionObject(jsonObject);
    }

    private static ConnectionData parseConnectionObject(JsonObject obj) {
        String ipv4 = obj.has("ipv4") && !obj.get("ipv4").isJsonNull() ? obj.get("ipv4").getAsString() : null;
        if(ipv4 == null) DATA_LOGGER.logWarn("Direccion ipv4 registrada como nula");
        else DATA_LOGGER.logDebug("Dato identificado "+ipv4);
        String ipv6 = obj.has("ipv6") && !obj.get("ipv6").isJsonNull() ? obj.get("ipv6").getAsString() : null;
        if(ipv6 == null) DATA_LOGGER.logWarn("Direccion ipv6 registrada como nula");
        else DATA_LOGGER.logDebug("Dato identificado "+ipv6);
        String path = obj.has("path") && !obj.get("path").isJsonNull() ? obj.get("path").getAsString() : "/";
        if(path == null) DATA_LOGGER.logWarn("Path registrada como nula");
        else DATA_LOGGER.logDebug("Dato identificado "+path);
        int port = obj.has("port") && !obj.get("port").isJsonNull() ? obj.get("port").getAsInt() : 80;
        if(port == 80) DATA_LOGGER.logWarn("Puerto registrado con fallback (80)");
        else DATA_LOGGER.logDebug("Dato identificado "+port);
        String name = obj.has("name") && !obj.get("name").isJsonNull() ? obj.get("name").getAsString() : null;
        if(name == null) DATA_LOGGER.logWarn("Nombre registrado como nulo");
        else DATA_LOGGER.logDebug("Dato identificado "+name);
        String uuidStr = obj.has("uuid") && !obj.get("uuid").isJsonNull() ? obj.get("uuid").getAsString() : null;
        UUID uuid;
        try {
            uuid = uuidStr != null && !uuidStr.isBlank() ? UUID.fromString(uuidStr) : UUID.randomUUID();
        } catch (IllegalArgumentException e) {
            uuid = UUID.randomUUID();
        }

        return new ConnectionData(uuid, ipv4, ipv6, path, port, name);
    }
	
	public static ArrayList<ConnectionData> readConnectionsFromFile(Path file) throws IOException {
	    ArrayList<ConnectionData> connections = new ArrayList<>();

	    try (InputStreamReader reader = new InputStreamReader(new FileInputStream(file.toFile()), StandardCharsets.UTF_8)) {
	        JsonElement root = JsonParser.parseReader(reader);
	        if (!root.isJsonArray()) return connections;

	        JsonArray array = root.getAsJsonArray();
	        for (JsonElement elem : array) {
	            if (!elem.isJsonObject()) continue;
	            connections.add(parseConnectionObject(elem.getAsJsonObject()));
	        }
	    }

	    return connections;
	}
	
	public static boolean writeConnectionsToFile(ArrayList<ConnectionData> connections, Path file) {
		try (FileWriter writer = new FileWriter(file.toFile())) {
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			gson.toJson(connections, writer);
			return true;
		} catch (IOException e) {
			DATA_LOGGER.logException("Ocurrio un error al escribir datos", e);
			return false;
		}
	}

}
