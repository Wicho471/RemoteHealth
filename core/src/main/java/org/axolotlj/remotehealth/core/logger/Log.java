package org.axolotlj.remotehealth.core.logger;

import java.io.IOException;

public class Log {
    private static DataLogger logger;

    public static DataLogger init() {
        if (logger != null) return logger; 

        try {
        	logger = new FileDataLogger();
        	logger.logInfo("Logger iniciado correctamente");
            return logger;
        } catch (IOException e) {
            System.err.println("Fallo al inicializar el logger, se usará NoOp: " + e.getMessage());
            return logger = new NoOpDataLogger();
        } catch (Exception e) {
        	System.err.println("Fallo inesperado al inicializar el logger, se usará NoOp: \n" + e.getMessage());
            return logger = new NoOpDataLogger();
		}
    }
    
    public static void setLogger(DataLogger logger) {
    	Log.logger = logger;
	}

    public static DataLogger get() {
        if (logger == null) {
            init(); 
        }
        return logger;
    }
}
