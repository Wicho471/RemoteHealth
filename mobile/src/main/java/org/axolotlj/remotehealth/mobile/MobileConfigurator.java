package org.axolotlj.remotehealth.mobile;

import java.io.InputStream;
import java.util.Properties;
import java.util.logging.LogManager;

import org.axolotlj.remotehealth.core.CommonApp;
import org.axolotlj.remotehealth.core.config.ConfigFileHelper;
import org.axolotlj.remotehealth.core.config.PlatformConfigurator;
import org.axolotlj.remotehealth.core.logger.Log;
import org.axolotlj.remotehealth.mobile.storage.MobilePathResolver;

public class MobileConfigurator implements PlatformConfigurator {

//	private DataLogger dataLogger = Log.get();

	static {
		System.out.println("Seteando rutas");
		ConfigFileHelper.setPathResolver(new MobilePathResolver());
		Log.init(MobileConfigurator.class);
	}

	@Override
	public void checkPaths() {
		try {
			Class.forName("org.axolotlj.remotehealth.mobile.utils.MobilePaths");
			Class.forName("org.axolotlj.remotehealth.core.path.SharedPaths");
		} catch (ClassNotFoundException e) {
			String message = "[Remote Health] No se encontraron las clases marcadas";
			System.out.println(message);
			throw new RuntimeException(message, e);
		} catch (Exception e) {
			String message = "[Remote Health] Ocurrio un error al precargar las rutas";
			System.out.println(message);
			throw new RuntimeException(message, e);
		}
	}

	@Override
	public void getDeviceInfo() {
		System.out.println("[Remote Health] Aun investigando como obtener las caracteristicas del dsipositvo");
	}

	@Override
	public void devConfigs() {
		if (CommonApp.isDevMode()) {
			try (InputStream in = MobileApp.class.getResourceAsStream("/logging.properties")) {
				if (in != null) {
					LogManager.getLogManager().readConfiguration(in);
				}
			} catch (Exception e) {
				Log.get().logException("[Remote Health] Error cargando logging.properties: ", e);
			}
		}
	}

	@Override
	public void getRuntimeArgs() {
		Properties props = System.getProperties();
		props.forEach((key, value) -> System.out.println(key + " = " + value));
	}
}
