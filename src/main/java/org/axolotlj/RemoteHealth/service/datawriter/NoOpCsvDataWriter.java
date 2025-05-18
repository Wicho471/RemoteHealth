package org.axolotlj.RemoteHealth.service.datawriter;

import org.axolotlj.RemoteHealth.sensor.data.DataPoint;

/**
 * Implementación nula que no realiza ninguna operación. Útil para evitar comprobaciones de null.
 */
public class NoOpCsvDataWriter extends CsvDataWriter {

    @Override
    public void writeData(String csvLine) {
        // No-op
    }

    @Override
    public void close() {
        // No-op
    }

    @Override
    public String getDataFilePath() {
        return "";
    }

	@Override
	public void writeData(DataPoint csvLine) {
		
	}
}
