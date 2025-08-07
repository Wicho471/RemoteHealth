package org.axolotlj.remotehealth.core.sensor.correction;

import java.util.List;

import org.axolotlj.remotehealth.core.logger.DataLogger;
import org.axolotlj.remotehealth.core.logger.Log;
import org.axolotlj.remotehealth.core.sensor.data.DataPoint;
import org.axolotlj.remotehealth.core.sensor.handle.SensorField;

/**
 * Servicio central para aplicar procesos de corrección sobre datos de sensores.
 */
public class DataCorrectionService {

    private final EdgeValueFiller edgeFiller;
    private final Interpolator interpolator;
    private final DataLogger dataLogger = Log.get();

    public DataCorrectionService() {
        this.edgeFiller = new EdgeValueFiller();
        this.interpolator = new Interpolator();
    }

    /**
     * Aplica las correcciones completas (relleno de extremos + interpolación) a los datos.
     *
     * @param dataList lista de DataPoint.
     * @param field campo a corregir.
     */
    public void correct(List<DataPoint> dataList) {
    	long startTime = System.currentTimeMillis();
    	for (SensorField field : SensorField.values()) {
    		edgeFiller.fillEdges(dataList, field);
            interpolator.interpolate(dataList, field);
    	}
    	long finalTime = System.currentTimeMillis();
    	dataLogger.logDebug("Proceso de corrección aplicado a [" + dataList.size() + "] registros en " + (finalTime - startTime) + " ms.");
    	DataValidator validator = new DataValidator();
    	if(validator.isValid(dataList)) {
    		dataLogger.logInfo("La correcion se aplico con exito");
    	} else {
    		dataLogger.logWarn("La correcion fallo");
    	}
    }
}

