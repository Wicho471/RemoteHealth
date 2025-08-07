package org.axolotlj.remotehealth.core.simulation;

import java.util.List;

import org.axolotlj.remotehealth.core.io.CsvUtils;

/**
 * Simulador basado en datos reales desde un archivo CSV.
 */
public class RealDataSimulator implements DataPayloadGenerator {

    private final List<String> dataLines;
    private int currentIndex = 0;

    /**
     * Crea una nueva instancia de simulador de datos reales.
     * 
     * @param resourcePath Ruta al recurso CSV en el classpath.
     */
    public RealDataSimulator(String resourcePath) {
        this.dataLines = CsvUtils.getAllLines(resourcePath);
    }

    @Override
    public String generatePayload() {
        if (dataLines.isEmpty()) return "";

        String[] columns = dataLines.get(currentIndex).split(",");
        currentIndex = (currentIndex + 1) % dataLines.size();

        long newTimestamp = System.currentTimeMillis();

        StringBuilder messageBuilder = new StringBuilder(Long.toHexString(newTimestamp).toUpperCase());
        for (int i = 1; i < columns.length; i++) {
            messageBuilder.append(",").append(columns[i].trim());
        }

        return messageBuilder.toString();
    }
}
