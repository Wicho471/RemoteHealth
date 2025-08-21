package org.axolotlj.remotehealth.mobile;

import java.util.Properties;

import org.axolotlj.remotehealth.core.config.ConfigFileHelper;
import org.axolotlj.remotehealth.core.config.PlatformConfigurator;
import org.axolotlj.remotehealth.core.logger.Log;
import org.axolotlj.remotehealth.mobile.storage.MobilePathResolver;

public class MobileConfigurator implements PlatformConfigurator {
	
	static {
		ConfigFileHelper.setPathResolver(new MobilePathResolver());
		Log.init();
	}

	@Override
	public void checkPaths() {
		try {
			Class.forName("org.axolotlj.remotehealth.mobile.utils.MobilePaths");
			Class.forName("org.axolotlj.remotehealth.core.path.SharedPaths");
		} catch (ClassNotFoundException e) {
			String message = "No se encontraron las clases marcadas";
			Log.get().logWarn(message);
		} catch (Exception e) {
			String message = "Ocurrio un error al precargar las rutas";
			Log.get().logException(message, e);
		}
	}

	@Override
	public void getDeviceInfo() {
		Log.get().logWarn("Aun investigando como obtener las caracteristicas del dsipositvo");
	}

	@Override
	public void devConfigs() {
//		if (CommonApp.isDevMode()) {
//			try (InputStream in = MobileApp.class.getResourceAsStream("/logging.properties")) {
//				if (in != null) {
//					LogManager.getLogManager().readConfiguration(in);
//				}
//			} catch (Exception e) {
//				Log.get().logException("Error cargando logging.properties: ", e);
//			}
//		}
	}

	@Override
	public void getRuntimeArgs() {
		Properties props = System.getProperties();
		props.forEach((key, value) -> Log.get().logDebug(key + " = " + value));
	}
}
