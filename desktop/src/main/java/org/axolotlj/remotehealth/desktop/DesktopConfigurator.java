package org.axolotlj.remotehealth.desktop;

import java.util.Properties;

import org.axolotlj.remotehealth.core.CommonApp;
import org.axolotlj.remotehealth.core.config.ConfigFileHelper;
import org.axolotlj.remotehealth.core.config.PlatformConfigurator;
import org.axolotlj.remotehealth.core.exception.DeviceInfoException;
import org.axolotlj.remotehealth.core.logger.Log;
import org.axolotlj.remotehealth.core.logger.api.DataLogger;
import org.axolotlj.remotehealth.core.model.DeviceInfo;
import org.axolotlj.remotehealth.core.service.DeviceInfoService;
import org.axolotlj.remotehealth.desktop.service.DesktopDeviceInfoProvider;
import org.axolotlj.remotehealth.desktop.utils.DesktopPathResolver;

public class DesktopConfigurator implements PlatformConfigurator {

	private DataLogger dataLogger = Log.get();
	
	static {
		ConfigFileHelper.setPathResolver(new DesktopPathResolver());
		Log.init();
	}

	@Override
	public void checkPaths() {
		try {
			Class.forName("org.axolotlj.remotehealth.desktop.utils.DesktopPaths");
			Class.forName("org.axolotlj.remotehealth.core.path.SharedPaths");
		} catch (ClassNotFoundException e) {
			String message = "Error al precargar rutas";
			dataLogger.logFatal(message);
			throw new RuntimeException(message, e);
		}

	}

	@Override
	public void getDeviceInfo() {
		DeviceInfoService service = new DesktopDeviceInfoProvider();
		try {
			DeviceInfo info = service.getDeviceInfo();
			dataLogger.logInfo(info.toString());
		} catch (DeviceInfoException e) {
			dataLogger.logException("Ocurrio un error al obtener la informacion del dispositivo", e);
		}
	}

	@Override
	public void devConfigs() {
		if (CommonApp.isDevMode()) {

		}
	}

	@Override
	public void getRuntimeArgs() {
		Properties properties = System.getProperties();
		properties.forEach((key, value) -> dataLogger.logDebug(key + " = " + value));
	}
}
