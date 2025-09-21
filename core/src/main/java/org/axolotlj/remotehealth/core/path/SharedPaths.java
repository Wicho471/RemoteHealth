package org.axolotlj.remotehealth.core.path;

import java.util.List;

import org.axolotlj.remotehealth.core.logger.Log;
import org.axolotlj.remotehealth.core.logger.api.DataLogger;
import org.axolotlj.remotehealth.core.path.verifier.PathsVerifier;

public class SharedPaths {
	public static final String MAIN_PATH = "/org/axolotlj/remotehealth/shared/";

	// ========================= DEFAULTS =========================
	private static final String DEFAULTS_PATH = MAIN_PATH + "defaults/";
	public static final String DEFAULTS_ANALYSIS_FILTERS_PROPERTIES = DEFAULTS_PATH + "analysis_filters.properties";
	public static final String DEFAULTS_DEVICE_CONNECTIONS_JSON = DEFAULTS_PATH + "device_connections.json";
	public static final String DEFAULTS_REAL_TIME_FILTERS_PROPERTIES = DEFAULTS_PATH + "real_time_filters.properties";

	// ========================= DOCS =========================
	private static final String DOCS_PATH = MAIN_PATH + "docs/";
	public static final String DOCS_USERMANUAL_PDF = DOCS_PATH + "UserManual.pdf";

	// ========================= FONTS =========================
	
	private static final String FONTS_PATH = MAIN_PATH + "fonts/";
	
	// -------- Roboto --------
	private static final String FONTS_ROBOTO_PATH = FONTS_PATH + "Roboto/";
	public static final String FONTS_ROBOTO_REGULAR_PATH = FONTS_ROBOTO_PATH + "Roboto-Regular.ttf";
	public static final String FONTS_ROBOTO_BOLD_PATH = FONTS_ROBOTO_PATH + "Roboto-Bold.ttf";
	public static final String FONTS_ROBOTO_ITALIC_PATH = FONTS_ROBOTO_PATH + "Roboto-Italic.ttf";
	public static final String FONTS_ROBOTO_BOLDITALIC_PATH = FONTS_ROBOTO_PATH + "Roboto-BoldItalic.ttf";
	
	// -------- Ubuntu_Mono --------
	private static final String FONTS_UBUNTU_PATH = FONTS_PATH + "Ubuntu_Mono/";
	public static final String FONTS_UBUNTU_BOLD_PATH = FONTS_UBUNTU_PATH + "UbuntuMono-Bold.ttf";
	public static final String FONTS_UBUNTU_BOLDITALIC_PATH = FONTS_UBUNTU_PATH + "UbuntuMono-BoldItalic.ttf";
	public static final String FONTS_UBUNTU_ITALIC_PATH = FONTS_UBUNTU_PATH + "UbuntuMono-Italic.ttf";
	public static final String FONTS_UBUNTU_REGULAR_PATH = FONTS_UBUNTU_PATH + "UbuntuMono-Regular.ttf";

	// ========================= REF =========================
	private static final String REF_PATH = MAIN_PATH + "ref/";
	public static final String REF_CSV = REF_PATH + "[Esp32][Wicho]2025-05-04_13-24-34.csv";
	
	// ========================= MISC =========================
	private static final String MISC_PATH = MAIN_PATH + "misc/";
	public static final String ABOUT_TXT = MISC_PATH + "about.txt";
	public static final String LICENSE_TXT = MISC_PATH + "license.txt";
	public static final String THANKS_TXT = MISC_PATH + "thanks.txt";
	
    static {
    	DataLogger dataLogger = Log.get();
    	dataLogger.logInfo("Verificando presencia de archivos compartidos");
        try {
            PathsVerifier verifier = new PathsVerifier();
            List<String> missing = verifier.verifyAllPaths(SharedPaths.class);

            if (!missing.isEmpty()) {
            	dataLogger.logWarn("Archivos faltantes en SharedPaths:");
                for (String path : missing) {
                	dataLogger.logWarn(" -> " + path);
                }
            } else {
            	dataLogger.logInfo("Todos los recursos definidos en SharedPaths fueron encontrados.");
            }
        } catch (Exception e) {
        	dataLogger.logException("Error durante la verificación automática de SharedPaths", e);
        	e.printStackTrace();
        }
    }
	public static void main(String[] args) {
		Log.get().logInfo("Ejecutando pruebas");
	}
}
