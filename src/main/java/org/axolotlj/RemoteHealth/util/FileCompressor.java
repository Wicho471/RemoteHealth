package org.axolotlj.RemoteHealth.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Utilidad general para comprimir y descomprimir archivos utilizando GZIP.
 */
public class FileCompressor {

    /**
     * Comprime un archivo utilizando GZIP.
     *
     * @param inputFile Archivo de entrada sin comprimir.
     * @return Archivo resultante comprimido con extensión .gz.
     * @throws IOException Si ocurre un error durante la compresión.
     */
    public static File compress(File inputFile) throws IOException {
        File compressedFile = new File(inputFile.getAbsolutePath() + ".gz");

        try (InputStream fis = Files.newInputStream(inputFile.toPath());
             OutputStream fos = Files.newOutputStream(compressedFile.toPath());
             GZIPOutputStream gos = new GZIPOutputStream(fos)) {
            fis.transferTo(gos);
        }

        return compressedFile;
    }

    /**
     * Descomprime un archivo GZIP.
     *
     * @param gzFile Archivo .gz a descomprimir.
     * @return Archivo descomprimido, sin la extensión .gz.
     * @throws IOException Si ocurre un error durante la descompresión.
     */
    public static File decompressAtDir(File gzFile) throws IOException {
        String outputPath = gzFile.getAbsolutePath().replaceFirst("\\.gz$", "");
        File outputFile = new File(outputPath);

        try (InputStream fis = Files.newInputStream(gzFile.toPath());
             GZIPInputStream gis = new GZIPInputStream(fis);
             OutputStream fos = Files.newOutputStream(outputFile.toPath())) {
            gis.transferTo(fos);
        }

        return outputFile;
    }
     
    /**
     * Retorna un InputStream con el contenido descomprimido del archivo GZIP.
     * El stream debe ser cerrado por el consumidor.
     *
     * @param gzFile Archivo .gz a descomprimir.
     * @return InputStream de los datos descomprimidos.
     * @throws IOException Si ocurre un error al abrir el archivo.
     */
    public static InputStream decompressToStream(File gzFile) throws IOException {
        InputStream fis = Files.newInputStream(gzFile.toPath());
        return new GZIPInputStream(fis);
    }
}
