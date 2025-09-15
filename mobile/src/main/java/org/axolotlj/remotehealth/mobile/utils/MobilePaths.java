package org.axolotlj.remotehealth.mobile.utils;

import java.util.List;

import org.axolotlj.remotehealth.core.logger.Log;
import org.axolotlj.remotehealth.core.logger.api.DataLogger;
import org.axolotlj.remotehealth.core.path.verifier.PathsVerifier;

public class MobilePaths {
	private static final String MAIN_PATH = "/org/axolotlj/remotehealth/mobile/";

	private static final String VIEW_PATH = MAIN_PATH + "view/";

	public static final String HOME = VIEW_PATH + "home.fxml";
	public static final String QR_SCANNER = VIEW_PATH + "qrscanner.fxml";
	public static final String MONITOR = VIEW_PATH + "monitor.fxml";

	static {
		DataLogger dataLogger = Log.get();
		dataLogger.logInfo("Verificando presencia de archivos para mobile");
		try {

			PathsVerifier verifier = new PathsVerifier();
			List<String> missing = verifier.verifyAllPaths(MobilePaths.class);

			if (!missing.isEmpty()) {
				dataLogger.logWarn("Archivos faltantes en MobilePaths:");
				for (String path : missing) {
					dataLogger.logWarn(" -> " + path);
				}
			} else {
				dataLogger.logInfo("Todos los recursos definidos en MobilePaths fueron encontrados.");
			}
		} catch (Exception e) {
			dataLogger.logException("Error durante la verificación automática de MobilePaths ", e);
		}
	}
}
