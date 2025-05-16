package org.axolotlj.RemoteHealth.service.logger;

import java.io.IOException;

public class Log {
    private static DataLogger logger;

    public static void init() {
        if (logger != null) return; 

        try {
            logger = new FileDataLogger();
        } catch (IOException e) {
            System.err.println("Fallo al inicializar el logger, se usará NoOp: " + e.getMessage());
            logger = new NoOpDataLogger();
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
