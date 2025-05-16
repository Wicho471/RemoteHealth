package org.axolotlj.RemoteHealth.config.files;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import org.axolotlj.RemoteHealth.config.ConfigFileHelper;
import org.axolotlj.RemoteHealth.model.ConnectionData;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.UUID;

/**
 * Clase encargada de gestionar la carga del archivo de configuración de dispositivos.
 * Lee un archivo JSON, valida su contenido y carga una lista de dispositivos.
 */
public class ConnectionsHandler {

	private ConnectionsHandler() {}
	
    private static final String CONFIG_FILE_NAME = "device_connections.json";

    /**
     * Carga las conexiones de dispositivos desde un archivo JSON.
     *
     * @return Lista de datos de conexión leída del archivo
     */
    public static ArrayList<ConnectionData> load() {
    	ArrayList<ConnectionData> connections = new ArrayList<>();
        Path configDir = ConfigFileHelper.resolveMainDir();
        Path configFile = configDir.resolve(CONFIG_FILE_NAME);

        try {
            ConfigFileHelper.copyDefaultIfMissing(CONFIG_FILE_NAME, configFile);
            connections = parseConfigFile(configFile);
        } catch (IOException e) {
            System.err.println("Error al copiar archivo por defecto: " + e.getMessage());
        } catch (JsonSyntaxException | IllegalStateException e) {
            System.err.println("Archivo JSON inválido: " + e.getMessage());
            ConfigFileHelper.backupCorruptedFile(configFile);

            try {
                Files.deleteIfExists(configFile);
                ConfigFileHelper.copyDefaultIfMissing(CONFIG_FILE_NAME, configFile);
                connections = parseConfigFile(configFile);
            } catch (IOException ex) {
                System.err.println("Error restaurando archivo por defecto: " + ex.getMessage());
            }
        }

        return connections;
    }

    private static ArrayList<ConnectionData> parseConfigFile(Path configFile) {
        ArrayList<ConnectionData> connections = new ArrayList<>();

        try (FileReader reader = new FileReader(configFile.toFile())) {
            JsonElement root = JsonParser.parseReader(reader);
            if (!root.isJsonArray()) return connections;

            JsonArray array = root.getAsJsonArray();
            for (JsonElement elem : array) {
                if (!elem.isJsonObject()) continue;

                JsonObject obj = elem.getAsJsonObject();
                
                String ipv4 = obj.has("ipv4") ? obj.get("ipv4").getAsString() : null;
                String ipv6 = obj.has("ipv6") ? obj.get("ipv6").getAsString() : null;
                String path = obj.has("path") ? obj.get("path").getAsString() : "/";
                int port = obj.has("port") ? obj.get("port").getAsInt() : 80;
                String name = obj.has("name") ? obj.get("name").getAsString() : null;
                String uuid = obj.has("uuid") ? obj.get("uuid").getAsString() : UUID.randomUUID().toString();
                
                connections.add(new ConnectionData(UUID.fromString(uuid),ipv4, ipv6, path, port, name));
            }

        } catch (IOException e) {
            System.err.println("DeviceConnections::parseConfigFile - Error al leer archivo JSON: " + e.getMessage());
        }

        return connections;
    }
    
    /**
     * Elimina una conexión de dispositivo por índice y guarda el archivo actualizado.
     *
     * @param index Índice del dispositivo a eliminar
     */
    public static boolean removeConnectionData(int index) {
        ArrayList<ConnectionData> connections = load();
        if (connections != null && index >= 0 && index < connections.size()) {
            connections.remove(index);
            return save(connections);
        }
        return false;
    }
    
    
    /**
     * Actualiza el nombre de un dispositivo dado su índice.
     *
     * @param index Índice del dispositivo a modificar
     * @param newName Nuevo nombre del dispositivo
     */
    public static boolean updateName(int index, String newName) {
        ArrayList<ConnectionData> connections = load();
        if (connections != null && index >= 0 && index < connections.size()) {
            ConnectionData data = connections.get(index);
            data.setName(newName);
            return save(connections);
        }
        return false;
    }
    
    /**
     * Actualiza los datos generales de una conexion.
     *
     * @param index Índice del dispositivo a modificar
     * @param connectionData Datos de la conexion para actualizar
     */
    public static boolean update(int index, ConnectionData connectionData) {
        ArrayList<ConnectionData> connections = load();
        if (connections != null && index >= 0 && index < connections.size()) {
        	connections.set(index, connectionData);
            return save(connections);
        }
        return false;
    }
    
    /**
     * Guarda la lista actualizada de conexiones en el archivo JSON.
     *
     * @param connections Lista actual de datos de conexión
     */
    public static boolean save(ArrayList<ConnectionData> connections) {
        Path configFile = ConfigFileHelper.resolveMainDir().resolve(CONFIG_FILE_NAME);

        try (FileWriter writer = new FileWriter(configFile.toFile())) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(connections, writer);
            return true;
        } catch (IOException e) {
            System.err.println("DeviceConnections::save - Error al guardar archivo JSON: " + e.getMessage());
            return false;
        }
    }
    
    public static boolean addConnetcionData(String json) {
        try {
            ConnectionData newConnection = solveJson(json);
            ArrayList<ConnectionData> currentConnections = load();

            if (currentConnections == null) {
                currentConnections = new ArrayList<>();
            }

            currentConnections.add(newConnection);
            return save(currentConnections);

        } catch (JsonSyntaxException e) {
            System.err.println("DeviceConnections::addConnetcionData - Error de sintaxis JSON: " + e.getMessage());
            return false;
        } catch (IllegalArgumentException e) {
            System.err.println("DeviceConnections::addConnetcionData - Datos de conexión inválidos: " + e.getMessage());
            return false;
        } catch (Exception e) {
            System.err.println("DeviceConnections::addConnetcionData - Error inesperado: " + e.getMessage());
            return false;
        }
    }

    private static ConnectionData solveJson(String json) {
        JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();
        
        String ipv4 = jsonObject.has("ipv4") ? jsonObject.get("ipv4").getAsString() : null;
        String ipv6 = jsonObject.has("ipv6") ? jsonObject.get("ipv6").getAsString() : null;
        String path = jsonObject.has("path") ? jsonObject.get("path").getAsString() : "/";
        int port = jsonObject.has("port") ? jsonObject.get("port").getAsInt() : 80;
        String name = jsonObject.has("name") ? jsonObject.get("name").getAsString() : null;
        UUID uuid = UUID.randomUUID();
        
        return new ConnectionData(uuid, ipv4, ipv6, path, port, name);
    }
}
