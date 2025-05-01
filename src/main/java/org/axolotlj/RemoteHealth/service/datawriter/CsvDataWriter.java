package org.axolotlj.RemoteHealth.service.datawriter;

import org.axolotlj.RemoteHealth.model.StructureData;

/**
 * Interfaz para escribir datos en formato CSV.
 */
public abstract class CsvDataWriter {

    /**
     * Escribe una línea CSV.
     *
     * @param csvLine Línea en formato CSV
     */
    public abstract void writeData(String csvLine);
    
    /**
     * Escribe una línea CSV.
     *
     * @param csvLine Línea en formato CSV
     */
    public abstract void writeData(StructureData csvLine);

    /**
     * Cierra los recursos usados por el escritor.
     */
    public abstract void close();

    /**
     * Devuelve la ruta absoluta al archivo de datos, si aplica.
     *
     * @return Ruta al archivo o cadena vacía si no aplica
     */
    public abstract String getDataFilePath();
}
