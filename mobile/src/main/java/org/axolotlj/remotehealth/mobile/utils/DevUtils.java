package org.axolotlj.remotehealth.mobile.utils;

import java.io.InputStream;
import java.util.logging.LogManager;

import org.axolotlj.remotehealth.core.logger.Log;
import org.axolotlj.remotehealth.mobile.MobileApp;

public class DevUtils {

	public static void init() {
		if (DevUtils.isDevMode()) {
			DevUtils.configureLogging();
		}
	}

	public static void configureLogging() {
		try (InputStream in = MobileApp.class.getResourceAsStream("/logging.properties")) {
			if (in != null) {
				LogManager.getLogManager().readConfiguration(in);
			}
		} catch (Exception e) {
			Log.get().logException("Error cargando logging.properties: ", e);
		}
	}

	public static boolean isDevMode() {
		return System.getProperty("app.env", "dev").equals("dev");
	}
}
