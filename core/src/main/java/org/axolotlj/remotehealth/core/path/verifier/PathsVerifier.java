package org.axolotlj.remotehealth.core.path.verifier;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * Verificador centralizado de todos los recursos definidos en la clase Paths.
 */
public class PathsVerifier {

    private final FileVerifier fileVerifier;

    /**
     * Crea una nueva instancia de PathsVerifier con un verificador de archivos.
     */
    public PathsVerifier() {
        this.fileVerifier = new FileVerifier();
    }

    /**
     * Verifica todos los recursos definidos como constantes públicas en la clase Paths.
     *
     * @param clazz clase que contiene las rutas a verificar
     * @return lista de rutas no encontradas
     */
    public <T> List<String> verifyAllPaths(Class<T> clazz) {
        List<String> missingResources = new ArrayList<>();

        Field[] fields = clazz.getDeclaredFields(); 

        for (Field field : fields) {
            if (!Modifier.isPublic(field.getModifiers()) || !Modifier.isStatic(field.getModifiers())) {
                continue;
            }

            if (!field.getType().equals(String.class)) {
                continue;
            }

            try {
                String path = (String) field.get(null);
                if (!fileVerifier.resourceExists(path)) {
                    missingResources.add(path);
                }
            } catch (IllegalAccessException e) {
                System.err.println("Error accessing field: " + field.getName() + " - " + e.getMessage());
            }
        }

        return missingResources;
    }

}
