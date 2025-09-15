package org.axolotlj.remotehealth.core.logger;

import org.axolotlj.remotehealth.core.logger.api.DataLogger;
import org.axolotlj.remotehealth.core.logger.api.FileDataLogger;
import org.axolotlj.remotehealth.core.logger.api.NoOpDataLogger;
import org.axolotlj.remotehealth.core.logger.api.Slf4jFileDataLogger;

public class Log {
	private static DataLogger logger;

	public static DataLogger init() {
		if (logger != null)
			return logger;

		try {
			logger = new FileDataLogger();
			System.out.println("[Remote Health] Logger iniciado correctamente");
			logger.logInfo("Logger iniciado correctamente");
			return logger;
		} catch (Exception e) {
			System.err.println("Fallo al inicializar el logger personalizado, se usará slf4j: " + e.getMessage());
			e.printStackTrace();
			try {
				return logger = new Slf4jFileDataLogger();
			} catch (Exception e2) {
				System.err.println("Fallo inesperado al inicializar el loggers, se usará NoOp: \n" + e.getMessage());
				e.printStackTrace();
				return logger = new NoOpDataLogger();
			}
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
