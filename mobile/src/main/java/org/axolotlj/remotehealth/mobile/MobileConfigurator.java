package org.axolotlj.remotehealth.mobile;

import java.io.InputStream;
import java.util.logging.LogManager;

import org.axolotlj.remotehealth.core.CommonApp;
import org.axolotlj.remotehealth.core.config.ConfigFileHelper;
import org.axolotlj.remotehealth.core.config.PlatformConfigurator;
import org.axolotlj.remotehealth.core.logger.DataLogger;
import org.axolotlj.remotehealth.core.logger.Log;
import org.axolotlj.remotehealth.mobile.storage.MobilePathResolver;

public class MobileConfigurator implements PlatformConfigurator {
	
	private DataLogger dataLogger = Log.get();

	static {
		ConfigFileHelper.setPathResolver(new MobilePathResolver());
		CommonApp.initialize();
	}

	@Override
	public void checkPaths() {
		try {
			Class.forName("org.axolotlj.remotehealth.mobile.utils.MobilePaths");
			Class.forName("org.axolotlj.remotehealth.core.path.SharedPaths");
		} catch (ClassNotFoundException e) {
			String message = "Error al precargar rutas";
			dataLogger.logFatal(message);
			throw new RuntimeException(message, e);
		}
	}

	@Override
	public void getDeviceInfo() {
		dataLogger.logWarn("Aun investigando como obtener las caracteristicas del dsipositvo");
	}

	@Override
	public void devConfigs() {
		if (CommonApp.isDevMode()) {
			try (InputStream in = MobileApp.class.getResourceAsStream("/logging.properties")) {
				if (in != null) {
					LogManager.getLogManager().readConfiguration(in);
				}
			} catch (Exception e) {
				Log.get().logException("Error cargando logging.properties: ", e);
			}
		}
	}
}
