package org.axolotlj.RemoteHealth.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Administrador de preferencias persistentes para la aplicación.
 */
public class PropertiesManager {

    private Path configPath;

    private final Properties properties;
    
    public PropertiesManager(Path path) {
    	System.out.println("PropertiesManager::PropertiesManager "+path.toString());
        this.configPath = path;
    	this.properties = new Properties();
        load();
    }

    /**
     * Obtiene el valor de una propiedad almacenada.
     * @param key Clave de la propiedad.
     * @return Valor asociado, o null si no existe.
     */
    public String get(String key) {
        return properties.getProperty(key);
    }

    /**
     * Establece o actualiza una propiedad y la guarda en el archivo.
     * @param key Clave de la propiedad.
     * @param value Valor que se desea asociar.
     */
    public void set(String key, String value) {
        properties.setProperty(key, value);
        save();
    }

    /**
     * Elimina una propiedad del archivo de configuración.
     * @param key Clave de la propiedad a eliminar.
     */
    public void remove(String key) {
        properties.remove(key);
        save();
    }

    /**
     * Elimina todas las preferencias guardadas.
     */
    public void clear() {
        properties.clear();
        save();
    }

    /**
     * Verifica si una clave existe en las preferencias.
     * @param key Clave que se desea verificar.
     * @return true si existe, false en caso contrario.
     */
    public boolean contains(String key) {
        return properties.containsKey(key);
    }

    private void load() {
        try {
            File configFile = configPath.toFile();
            if (configFile.exists()) {
                try (FileInputStream in = new FileInputStream(configFile)) {
                    properties.load(in);
                }
            } else {
                Files.createDirectories(configPath.getParent());
                Files.createFile(configPath);
            }
        } catch (Exception e) {
            System.err.println("Error al cargar preferencias: " + e.getMessage());
        }
    }

    private void save() {
        try (FileOutputStream out = new FileOutputStream(configPath.toFile())) {
            properties.store(out, "RemoteHealth Properties");
        } catch (Exception e) {
            System.err.println("Error al guardar: " + e.getMessage());
        }
    }
    
    public void saveAll() {
        save();
    }
}
