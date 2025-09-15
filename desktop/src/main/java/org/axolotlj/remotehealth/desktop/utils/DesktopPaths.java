package org.axolotlj.remotehealth.desktop.utils;

import java.util.List;

import org.axolotlj.remotehealth.core.logger.Log;
import org.axolotlj.remotehealth.core.logger.api.DataLogger;
import org.axolotlj.remotehealth.core.path.verifier.PathsVerifier;

public class DesktopPaths {
	private static final String MAIN_PATH = "/org/axolotlj/remotehealth/desktop/";

    // ========================= BIN =========================
    private static final String BIN_PATH = MAIN_PATH + "bin/";
    public static final String BIN_PRUEBADECARGA_INO_BIN = BIN_PATH + "PruebaDeCarga.ino.bin";
    public static final String BIN_PRUEBADECARGA_INO_BOOTLOADER_BIN = BIN_PATH + "PruebaDeCarga.ino.bootloader.bin";
    public static final String BIN_PRUEBADECARGA_INO_ELF = BIN_PATH + "PruebaDeCarga.ino.elf";
    public static final String BIN_PRUEBADECARGA_INO_MAP = BIN_PATH + "PruebaDeCarga.ino.map";
    public static final String BIN_PRUEBADECARGA_INO_MERGED_BIN = BIN_PATH + "PruebaDeCarga.ino.merged.bin";
    public static final String BIN_PRUEBADECARGA_INO_PARTITIONS_BIN = BIN_PATH + "PruebaDeCarga.ino.partitions.bin";
    public static final String BIN_BOOT_APP0_BIN = BIN_PATH + "boot_app0.bin";

    // ========================= CSS =========================
    private static final String CSS_PATH = MAIN_PATH + "css/";
    public static final String CSS_DASHBOARDSTYLE_CSS = CSS_PATH + "DashboardStyle.css";
    public static final String CSS_STYLES_CSS = CSS_PATH + "styles.css";

    // ========================= LANG =========================
    private static final String LANG_PATH = MAIN_PATH + "lang/";
    public static final String LANG_MESSAGES_PROPERTIES = LANG_PATH + "messages.properties";
    public static final String LANG_MESSAGES_EN_PROPERTIES = LANG_PATH + "messages_en.properties";

    // ========================= VIEW =========================
    private static final String VIEW_PATH = MAIN_PATH + "view/";

    // -------- Include --------
    private static final String VIEW_INCLUDE_PATH = VIEW_PATH + "include/";
    public static final String VIEW_INCLUDE_FILTEROPTIONS_FXML = VIEW_INCLUDE_PATH + "FilterOptions.fxml";
    public static final String VIEW_INCLUDE_MENUBAR_FXML = VIEW_INCLUDE_PATH + "MenuBar.fxml";

    // -------- Scene --------
    private static final String VIEW_SCENE_PATH = VIEW_PATH + "scene/";
    public static final String VIEW_SCENE_DASHBOARDSCENE_FXML = VIEW_SCENE_PATH + "DashboardScene.fxml";
    public static final String VIEW_SCENE_DATAANALYSISSCENE_FXML = VIEW_SCENE_PATH + "DataAnalysisScene.fxml";
    public static final String VIEW_SCENE_DEVICESETUPSCENE_FXML = VIEW_SCENE_PATH + "DeviceSetupScene.fxml";
    public static final String VIEW_SCENE_FILTERSETTINGSSCENE_FXML = VIEW_SCENE_PATH + "FilterSettingsScene.fxml";
    public static final String VIEW_SCENE_FLASHESP32SCENE_FXML = VIEW_SCENE_PATH + "FlashEsp32Scene.fxml";
    public static final String VIEW_SCENE_STARTUPSCENE_FXML = VIEW_SCENE_PATH + "StartupScene.fxml";
    public static final String VIEW_SCENE_CONFIG_ESP32_FXML = VIEW_SCENE_PATH + "ConfigEsp32.fxml";

    // -------- Window --------
    private static final String VIEW_WINDOW_PATH = VIEW_PATH + "window/";
    public static final String VIEW_WINDOW_BENCHMARKWINDOW_FXML = VIEW_WINDOW_PATH + "BenchmarkWindow.fxml";
    public static final String VIEW_WINDOW_CSVSELECTORWINDOW_FXML = VIEW_WINDOW_PATH + "CsvSelectorWindow.fxml";
    public static final String VIEW_WINDOW_DEVICECONFIGWINDOW_FXML = VIEW_WINDOW_PATH + "DeviceConfigWindow.fxml";
    public static final String VIEW_WINDOW_LOG_FXML = VIEW_WINDOW_PATH + "LogsWindow.fxml";
    public static final String VIEW_WINDOW_BLUETOOTH_LIST_FXML = VIEW_WINDOW_PATH + "BluetoothList.fxml";
    public static final String VIEW_WINDOW_WIFI_LIST_FXML = VIEW_WINDOW_PATH + "WifiList.fxml";
    public static final String VIEW_WINDOW_WIFI_PASSWORD_FXML = VIEW_WINDOW_PATH + "WifiPassword.fxml";

    static {
    	DataLogger dataLogger = Log.get();
    	dataLogger.logInfo("Verificando presencia de archivos para desktop");
        try {
        	
            PathsVerifier verifier = new PathsVerifier();
            List<String> missing = verifier.verifyAllPaths(DesktopPaths.class);

            if (!missing.isEmpty()) {
                dataLogger.logWarn("Archivos faltantes en DesktopPaths:");
                for (String path : missing) {
                	dataLogger.logWarn(" -> " + path);
                }
            } else {
            	dataLogger.logInfo("Todos los recursos definidos en DesktopPaths fueron encontrados.");
            }
        } catch (Exception e) {
            dataLogger.logException("Error durante la verificación automática de DesktopPaths", e);
        }
    }
    
    public static void main(String[] args) {
		
	}
}