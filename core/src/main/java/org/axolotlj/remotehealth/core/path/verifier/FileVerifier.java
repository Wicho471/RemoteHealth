package org.axolotlj.remotehealth.core.path.verifier;

import java.io.File;
import java.net.URL;
import java.util.Objects;

/**
 * Verificador de archivos accesibles por rutas relativas o desde recursos.
 */
public class FileVerifier {

    /**
     * Verifica si el recurso especificado existe en el classpath.
     *
     * @param resourcePath ruta del recurso a verificar
     * @return true si el recurso existe, false en caso contrario
     */
    public boolean resourceExists(String resourcePath) {
        URL resourceUrl = getClass().getResource(resourcePath);
        return Objects.nonNull(resourceUrl);
    }

    /**
     * Verifica si el archivo especificado existe en el sistema de archivos.
     *
     * @param filePath ruta absoluta del archivo
     * @return true si el archivo existe, false en caso contrario
     */
    public boolean fileExists(String filePath) {
        return new File(filePath).exists();
    }
}
