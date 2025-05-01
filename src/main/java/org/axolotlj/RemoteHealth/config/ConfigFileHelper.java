package org.axolotlj.RemoteHealth.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;

public class ConfigFileHelper {

    private static final String DEFAULT_RESOURCE_DIR = "defaults";

    /**
     * Copia un archivo desde el classpath (resources/defaults) al sistema de archivos del usuario.
     *
     * @param defaultFileName Nombre del archivo por defecto en resources/defaults/
     * @param targetPath Ruta de destino donde se copiará el archivo
     * @throws IOException Si ocurre un error durante la copia
     */
    public static void copyDefaultIfMissing(String defaultFileName, Path targetPath) throws IOException {
        if (Files.exists(targetPath)) return;

        Path parent = targetPath.getParent();
        if (parent != null && !Files.exists(parent)) {
            Files.createDirectories(parent);
        }

        try (InputStream in = ConfigFileHelper.class.getClassLoader()
                .getResourceAsStream(DEFAULT_RESOURCE_DIR + "/" + defaultFileName)) {
            if (in == null) {
                throw new IOException("No se encontró el archivo por defecto: " + defaultFileName);
            }
            Files.copy(in, targetPath, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    /**
     * Crea una copia de respaldo de un archivo si existe.
     *
     * @param path Ruta del archivo original
     */
    public static void backupCorruptedFile(Path path) {
        if (!Files.exists(path)) return;
        try {
            Path backup = path.resolveSibling(path.getFileName() + ".bak");
            Files.copy(path, backup, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            System.err.println("Error al respaldar archivo: " + path.getFileName() + " - " + e.getMessage());
        }
    }
    
    public void copyDefaultFromResources(String resourceName, Path targetPath, Path configDir) {
        try {
            if (!Files.exists(configDir)) {
                Files.createDirectories(configDir);
            }

            try (InputStream in = getClass().getClassLoader().getResourceAsStream("defaults/" + resourceName)) {
                if (in == null) {
                    throw new IOException("Archivo por defecto no encontrado: " + resourceName);
                }
                Files.copy(in, targetPath, StandardCopyOption.REPLACE_EXISTING);
            }

        } catch (IOException e) {
            System.err.println("Error al copiar archivo por defecto: " + resourceName + " - " + e.getMessage());
        }
    }

    public static Path resolveMainDir() {
        String os = System.getProperty("os.name").toLowerCase();
        Path baseDir;

        if (os.contains("win")) {
            baseDir = Paths.get(System.getenv("LOCALAPPDATA"));
        } else if (os.contains("mac")) {
            baseDir = Paths.get(System.getProperty("user.home"), "Library", "Application Support");
        } else {
            baseDir = Paths.get(System.getProperty("user.home"), ".config");
        }

        return baseDir.resolve("RemoteHealth");
    }
    
    public static Path getConfigDir() {
    	return resolveMainDir().resolve("config");
	}
    
    public static Path getDataDir() {
    	return resolveMainDir().resolve("data");
	}
    
    public static Path getDLogsDir() {
    	return resolveMainDir().resolve("logs");
	}
    
    public static Path getLanguageConfigFile() {
        Path configDir = getConfigDir();
        try {
            if (!Files.exists(configDir)) {
                Files.createDirectories(configDir);
            }
        } catch (IOException e) {
            System.err.println("Error al crear carpeta de configuración: " + e.getMessage());
        }
        return configDir.resolve("language.conf");
    }

}
