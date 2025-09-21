package org.axolotlj.remotehealth.core.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;

public class ConfigFileHelper {

    private static final String DEFAULT_RESOURCE_DIR = "defaults";
    private static PathResolver pathResolver; 

    public static void setPathResolver(PathResolver resolver) {
        pathResolver = resolver;
    }

    public static PathResolver getPathResolver() {
        return pathResolver;
    }

    public static Path resolveMainDir() {
        return pathResolver.resolveMainDir();
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
    
    public static Path getCrashReportDir() {
        return resolveMainDir().resolve("crashes");
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

    public static void backupCorruptedFile(Path path) {
        if (!Files.exists(path)) return;
        try {
            Path backup = path.resolveSibling(path.getFileName() + ".bak");
            Files.copy(path, backup, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            System.err.println("Error al respaldar archivo: " + path.getFileName() + " - " + e.getMessage());
        }
    }
}
