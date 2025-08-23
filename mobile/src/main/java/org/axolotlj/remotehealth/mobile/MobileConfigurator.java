package org.axolotlj.remotehealth.mobile;

import java.util.Properties;

import org.axolotlj.remotehealth.core.concurrent.AsyncExecutor;
import org.axolotlj.remotehealth.core.config.ConfigFileHelper;
import org.axolotlj.remotehealth.core.config.PlatformConfigurator;
import org.axolotlj.remotehealth.core.logger.DataLogger;
import org.axolotlj.remotehealth.core.logger.Log;
import org.axolotlj.remotehealth.core.util.NetworkUtil;
import org.axolotlj.remotehealth.mobile.attach.Connectivity;
import org.axolotlj.remotehealth.mobile.storage.MobilePathResolver;

import com.gluonhq.attach.device.DeviceService;
import com.gluonhq.attach.orientation.OrientationService;
import com.gluonhq.attach.util.Services;

public class MobileConfigurator implements PlatformConfigurator {

	static {
		System.out.println("[Remote Health] Bloque estatico de MobileConfigurator iniciado");
		ConfigFileHelper.setPathResolver(new MobilePathResolver());
		System.out.println("[Remote Health] Inciando logger");
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
		DataLogger dataLogger = Log.get();
		AsyncExecutor.runFilterTask("DeviceInfoAsync", () -> {
			try {

				dataLogger.logDebug("Obteniendo información del dispositivo y servicios disponibles");

				// DeviceService
				Services.get(DeviceService.class).ifPresentOrElse(device -> {
					dataLogger.logDebug("==== DeviceService ====");
					dataLogger.logDebug("Modelo: " + device.getModel());
					dataLogger.logDebug("UUID: " + device.getUuid());
					dataLogger.logDebug("Plataforma: " + device.getPlatform());
					dataLogger.logDebug("Versión SO: " + device.getVersion());
					dataLogger.logDebug("Es wearable: " + device.isWearable());
					dataLogger.logDebug("Locale: " + device.getLocale());
				}, () -> dataLogger.logWarn("DeviceService no disponible"));

				// ConnectivityService
				dataLogger.logDebug("==== Conectividad ====");
				dataLogger.logDebug("Esta prendido wifi -> " + (NetworkUtil.hasEnabledNetworkInterface() ? "Connectado" : "Sin conexion"));
				dataLogger.logDebug("Esta conectado a una red -> " + (NetworkUtil.isLocalNetworkAvailable() ? "Connectado" : "Sin conexion"));
				Connectivity.create().ifPresentOrElse(monitor -> {
					dataLogger.logDebug("Tiene conexion (ConnectivityService)-> " + (monitor.isConnected()? "Connectado" : "Sin conexion"));
				}, () -> dataLogger.logWarn("ConnectivityService no disponible"));
				dataLogger.logDebug("Soporte para ipv6 -> " + (NetworkUtil.isSupportedIpv6()? "Soportado" : "Sin soporte"));
				dataLogger.logDebug("Conexion global ipv6 -> " + (NetworkUtil.isGlobalIPv6Available()? "Connectado" : "Sin conexion"));
				
				// OrientationService
				Services.get(OrientationService.class).ifPresentOrElse(orientation -> {
					dataLogger.logDebug("==== OrientationService ====");
					orientation.getOrientation().ifPresentOrElse(o -> dataLogger.logDebug("Orientación: " + o.name()),
							() -> dataLogger.logWarn("Orientación no disponible"));
				}, () -> dataLogger.logWarn("OrientationService no disponible"));

			} catch (IllegalStateException e) {
				dataLogger.logException("Se debe estar inicalizada la plataforma JavaFX", e);
			} catch (Exception e) {
				dataLogger.logException("Ocurrio un error inesperado al obtener los datos del dispositivo", e);
			}
			return "Tarea terminada";
		}, success -> {
			dataLogger.logInfo(success);
		}, error -> {
			dataLogger.logException("Ocurrio un error critico", error);
		});

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
		Log.get().logDebug("==== Runtime args ====");
		props.forEach((key, value) -> Log.get().logDebug(key + " = " + value));
	}

}
