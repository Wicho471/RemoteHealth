package org.axolotlj.remotehealth.core.io;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.UUID;

import org.axolotlj.remotehealth.core.model.ConnectionData;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

public class JsonUtils {
    public static ArrayList<ConnectionData> readConnectionsFromFile(Path file) throws IOException {
        ArrayList<ConnectionData> connections = new ArrayList<>();

        try (FileReader reader = new FileReader(file.toFile())) {
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
            System.err.println("ConnectionJsonHelper::writeConnectionsToFile - Error: " + e.getMessage());
            return false;
        }
    }

    public static ConnectionData parseFromJsonString(String json) throws JsonSyntaxException {
        JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();
        return parseConnectionObject(jsonObject);
    }

    private static ConnectionData parseConnectionObject(JsonObject obj) {
        String ipv4 = obj.has("ipv4") ? obj.get("ipv4").getAsString() : null;
        String ipv6 = obj.has("ipv6") ? obj.get("ipv6").getAsString() : null;
        String path = obj.has("path") ? obj.get("path").getAsString() : "/";
        int port = obj.has("port") ? obj.get("port").getAsInt() : 80;
        String name = obj.has("name") ? obj.get("name").getAsString() : null;
        String uuidStr = obj.has("uuid") ? obj.get("uuid").getAsString() : UUID.randomUUID().toString();

        return new ConnectionData(UUID.fromString(uuidStr), ipv4, ipv6, path, port, name);
    }
}
