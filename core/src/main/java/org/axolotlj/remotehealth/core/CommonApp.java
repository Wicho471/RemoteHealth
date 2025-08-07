package org.axolotlj.remotehealth.core;

import java.util.Locale;

import org.axolotlj.remotehealth.core.config.files.LanguageConfig;
import org.axolotlj.remotehealth.core.lang.I18n;

public class CommonApp {
	
	private static final boolean IS_DEV_MODE = System.getProperty("app.env", "dev").equals("dev");
	
	public static void initialize() {
		Locale savedLocale = LanguageConfig.loadSavedLocale();
		I18n.setLocale(savedLocale);

		AppContext.initialize();
	}
	
	public static boolean isDevMode() {
		return IS_DEV_MODE;
	}
}
