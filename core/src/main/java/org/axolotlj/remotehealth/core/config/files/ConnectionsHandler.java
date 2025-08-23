package org.axolotlj.remotehealth.core.config.files;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

import org.axolotlj.remotehealth.core.config.ConfigFileHelper;
import org.axolotlj.remotehealth.core.io.JsonUtils;
import org.axolotlj.remotehealth.core.logger.DataLogger;
import org.axolotlj.remotehealth.core.logger.Log;
import org.axolotlj.remotehealth.core.model.ConnectionData;

import com.google.gson.JsonSyntaxException;

public class ConnectionsHandler {
	
	private static DataLogger dataLogger = Log.get();

    private ConnectionsHandler() {}

    private static final String CONFIG_FILE_NAME = "device_connections.json";

    public static ArrayList<ConnectionData> load() {
        ArrayList<ConnectionData> connections = new ArrayList<>();
        Path configDir = ConfigFileHelper.resolveMainDir();
        Path configFile = configDir.resolve(CONFIG_FILE_NAME);

        if(!Files.exists(configFile)) return connections;
        
        try {
            ConfigFileHelper.copyDefaultIfMissing(CONFIG_FILE_NAME, configFile);
            connections = JsonUtils.readConnectionsFromFile(configFile);
        } catch (IOException e) {
        	dataLogger.logException("Error al copiar archivo por defecto", e);
        } catch (JsonSyntaxException | IllegalStateException e) {
            dataLogger.logException("Archivo JSON inválido: " , e);
            ConfigFileHelper.backupCorruptedFile(configFile);
            try {
                Files.deleteIfExists(configFile);
                ConfigFileHelper.copyDefaultIfMissing(CONFIG_FILE_NAME, configFile);
                connections = JsonUtils.readConnectionsFromFile(configFile);
            } catch (IOException ex) {
                dataLogger.logException("Error restaurando archivo por defecto: " , ex);
            }
        }

        return connections;
    }

    public static boolean removeConnectionData(int index) {
        ArrayList<ConnectionData> connections = load();
        if (connections != null && index >= 0 && index < connections.size()) {
            connections.remove(index);
            return save(connections);
        }
        return false;
    }

    public static boolean updateName(int index, String newName) {
        ArrayList<ConnectionData> connections = load();
        if (connections != null && index >= 0 && index < connections.size()) {
            connections.get(index).setName(newName);
            return save(connections);
        }
        return false;
    }

    public static boolean update(int index, ConnectionData connectionData) {
        ArrayList<ConnectionData> connections = load();
        if (connections != null && index >= 0 && index < connections.size()) {
            connections.set(index, connectionData);
            return save(connections);
        }
        return false;
    }

    public static boolean save(ArrayList<ConnectionData> connections) {
        Path configFile = ConfigFileHelper.resolveMainDir().resolve(CONFIG_FILE_NAME);
        return JsonUtils.writeConnectionsToFile(connections, configFile);
    }

    public static boolean addConnetcionData(String json) {
    	dataLogger.logInfo("Añadiendo conexion '"+json+"'");
        try {
            ConnectionData newConnection = JsonUtils.parseFromJsonString(json);
            ArrayList<ConnectionData> currentConnections = load();

            if (currentConnections == null) {
                currentConnections = new ArrayList<>();
            }

            currentConnections.add(newConnection);
            return save(currentConnections);

        } catch (JsonSyntaxException e) {
            dataLogger.logException("Error de sintaxis JSON: " , e);
            return false;
        } catch (IllegalArgumentException e) {
            dataLogger.logException("Datos de conexión inválidos: " , e);
            return false;
        } catch (Exception e) {
            dataLogger.logException("Error inesperado: " , e);
            return false;
        }
    }
}
