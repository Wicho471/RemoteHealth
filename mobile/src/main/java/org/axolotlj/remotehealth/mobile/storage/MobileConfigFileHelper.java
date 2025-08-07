package org.axolotlj.remotehealth.mobile.storage;

import com.gluonhq.attach.storage.StorageService;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.Optional;

/**
 * Utilidades para manejar archivos de configuración en dispositivos móviles usando Gluon Mobile.
 */
public class MobileConfigFileHelper {

    private static final String DEFAULT_RESOURCE_DIR = "defaults";
    private static final String APP_FOLDER = "RemoteHealth";

    /**
     * Copia un archivo por defecto desde el classpath a almacenamiento privado si no existe.
     *
     * @param defaultFileName Nombre del archivo dentro de resources/defaults/
     * @param subfolder Carpeta interna dentro de la app (por ejemplo: config, data, etc.)
     * @return Ruta del archivo destino
     * @throws IOException si ocurre un error durante la copia
     */
    public static Path copyDefaultIfMissing(String defaultFileName, String subfolder) throws IOException {
        Optional<StorageService> service = StorageService.create();
        if (service.isEmpty()) {
            throw new IOException("No se pudo acceder al servicio de almacenamiento.");
        }

        Optional<File> baseDirOpt = service.get().getPrivateStorage();
        if (baseDirOpt.isEmpty()) {
            throw new IOException("No se pudo obtener el almacenamiento privado.");
        }

        Path basePath = baseDirOpt.get().toPath().resolve(APP_FOLDER).resolve(subfolder);
        Path targetPath = basePath.resolve(defaultFileName);

        if (!Files.exists(targetPath)) {
            Files.createDirectories(basePath);

            try (InputStream in = MobileConfigFileHelper.class.getClassLoader()
                    .getResourceAsStream(DEFAULT_RESOURCE_DIR + "/" + defaultFileName)) {

                if (in == null) {
                    throw new IOException("No se encontró el archivo por defecto: " + defaultFileName);
                }

                Files.copy(in, targetPath, StandardCopyOption.REPLACE_EXISTING);
            }
        }

        return targetPath;
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

    /**
     * Devuelve la ruta de una carpeta interna dentro del almacenamiento privado.
     *
     * @param subfolder Carpeta interna (por ejemplo: "config", "logs", etc.)
     * @return Ruta completa al subdirectorio solicitado
     */
    public static Optional<Path> getAppSubDir(String subfolder) {
        Optional<StorageService> service = StorageService.create();
        if (service.isEmpty()) return Optional.empty();

        return service.get().getPrivateStorage()
                .map(file -> file.toPath().resolve(APP_FOLDER).resolve(subfolder));
    }

    /**
     * Devuelve la ruta al archivo de configuración de idioma.
     *
     * @return Ruta del archivo language.conf
     */
    public static Optional<Path> getLanguageConfigFile() {
        Optional<Path> configDirOpt = getAppSubDir("config");

        if (configDirOpt.isEmpty()) return Optional.empty();

        Path configDir = configDirOpt.get();
        try {
            if (!Files.exists(configDir)) {
                Files.createDirectories(configDir);
            }
        } catch (IOException e) {
            System.err.println("Error al crear carpeta de configuración: " + e.getMessage());
        }

        return Optional.of(configDir.resolve("language.conf"));
    }
}
