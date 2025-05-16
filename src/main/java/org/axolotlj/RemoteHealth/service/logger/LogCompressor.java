package org.axolotlj.RemoteHealth.service.logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import org.axolotlj.RemoteHealth.util.FileCompressor;

/**
 * Utilidad para comprimir archivos de log y gestionar el archivo latest.log.
 */
public class LogCompressor {

    /**
     * Comprime un archivo log en formato GZIP.
     *
     * @param inputFile Archivo original a comprimir.
     * @return Archivo comprimido generado.
     * @throws IOException Si ocurre un error durante la compresión.
     */
    public static File compress(File inputFile) throws IOException {
        return FileCompressor.compress(inputFile);
    }

    /**
     * Crea o reemplaza el archivo latest.log con el contenido de un archivo fuente.
     *
     * @param sourceFile Archivo de origen del cual copiar.
     * @throws IOException Si ocurre un error al copiar el archivo.
     */
    public static void overwriteLatest(File sourceFile) throws IOException {
        File latestFile = new File(sourceFile.getParentFile(), "latest.log");
        Files.copy(sourceFile.toPath(), latestFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    /**
     * Elimina un archivo si existe.
     *
     * @param file Archivo a eliminar.
     * @throws IOException Si ocurre un error al eliminar el archivo.
     */
    public static void deleteOriginal(File file) throws IOException {
        Files.deleteIfExists(file.toPath());
    }
}
