package org.axolotlj.RemoteHealth.util;

public class Paths {
    public static final String MAIN_PATH = "/org/axolotlj/RemoteHealth/";

    public static final String BIN_DIR = MAIN_PATH + "bin/";
    public static final String CSS_DIR = MAIN_PATH + "css/";
    public static final String DEFAULTS_DIR = MAIN_PATH + "defaults/";
    public static final String FXML_DIR = MAIN_PATH + "fxml/";
    public static final String IMG_DIR = MAIN_PATH + "img/";
    public static final String LANG_DIR = MAIN_PATH + "lang/";

    // Subcarpetas dentro de IMG
    public static final String IMG_FAVICONS_DIR = IMG_DIR + "favicons/";
    public static final String IMG_ICONS_DIR = IMG_DIR + "icons/";
    public static final String IMG_BUTTONS_DIR = IMG_DIR + "buttons/";

    // BIN files
    public static final String BIN_PRUEBA_CARGA = BIN_DIR + "PruebaDeCarga.ino.bin";
    public static final String BIN_BOOTLOADER = BIN_DIR + "PruebaDeCarga.ino.bootloader.bin";
    public static final String BIN_ELF = BIN_DIR + "PruebaDeCarga.ino.elf";
    public static final String BIN_MAP = BIN_DIR + "PruebaDeCarga.ino.map";
    public static final String BIN_MERGED = BIN_DIR + "PruebaDeCarga.ino.merged.bin";
    public static final String BIN_PARTITIONS = BIN_DIR + "PruebaDeCarga.ino.partitions.bin";
    public static final String BIN_BOOT_APP0 = BIN_DIR + "boot_app0.bin";

    // CSS files
    public static final String CSS_DASHBOARD_STYLE = CSS_DIR + "DashboardStyle.css";
    public static final String CSS_STYLES = CSS_DIR + "styles.css";

    // Defaults files
    public static final String DEFAULTS_ANALYSIS_FILTERS = DEFAULTS_DIR + "analysis_filters.properties";
    public static final String DEFAULTS_DEVICE_CONNECTIONS = DEFAULTS_DIR + "device_connections.json";
    public static final String DEFAULTS_REAL_TIME_FILTERS = DEFAULTS_DIR + "real_time_filters.properties";

    // FXML files
    public static final String FXML_DASHBOARD_SCENE = FXML_DIR + "DashboardScene.fxml";
    public static final String FXML_DATA_ANALYSIS_SCENE = FXML_DIR + "DataAnalysisScene.fxml";
    public static final String FXML_DEVICE_SETUP_SCENE = FXML_DIR + "DeviceSetupScene.fxml";
    public static final String FXML_ESP32_TOOLS_SCENE = FXML_DIR + "ESP32ToolsScene.fxml";
    public static final String FXML_FILTER_OPTIONS = FXML_DIR + "FilterOptions.fxml";
    public static final String FXML_FILTER_SETTINGS_SCENE = FXML_DIR + "FilterSettingsScene.fxml";
    public static final String FXML_FLASH_ESP32_SCENE = FXML_DIR + "FlashEsp32Scene.fxml";
    public static final String FXML_MENU_BAR = FXML_DIR + "MenuBar.fxml";
    public static final String FXML_STARTUP_SCENE = FXML_DIR + "StartupScene.fxml";

    // IMG files
    public static final String IMG_AD8232 = IMG_DIR + "AD8232.png";
    public static final String IMG_BATERIA = IMG_DIR + "Bateria.jpg";
    public static final String IMG_ESP32 = IMG_DIR + "ESP32.png";
    public static final String IMG_MAX30102 = IMG_DIR + "MAX30102.png";
    public static final String IMG_MLX90614T = IMG_DIR + "MLX90614t.png";
    public static final String IMG_MMA8452Q = IMG_DIR + "MMA8452Q.png";
    public static final String IMG_BOTON_X = IMG_DIR + "boton-x.png";
    public static final String IMG_COMPROBADO = IMG_DIR + "comprobado.png";
    public static final String IMG_HEALTH_CHECKUP = IMG_DIR + "health-checkup.png";
    public static final String IMG_TRABAJO_EN_PROGRESO = IMG_DIR + "trabajo-en-progreso.png";

    // IMG Favicons
    public static final String IMG_APP_ICON = IMG_FAVICONS_DIR + "app-icon.png";
    public static final String DASHBOARD_ICON = IMG_FAVICONS_DIR + "Dashboard.png";
    public static final String ESP32_ICON = IMG_FAVICONS_DIR + "Microcontroler.png";
    public static final String OPTIONS_ICON = IMG_FAVICONS_DIR + "Options.png";
    public static final String SETTING_ICON = IMG_FAVICONS_DIR + "Settings.png";
    public static final String ANALYSIS_ICON = IMG_FAVICONS_DIR + "Analysis.png";
    public static final String QR_ICON = IMG_FAVICONS_DIR + "Qr.png";
    public static final String UPLOAD_ICON = IMG_FAVICONS_DIR + "Upload.png";

    // IMG Icons
    public static final String IMG_BACK = IMG_ICONS_DIR + "back.png";
    public static final String IMG_FAST_FORWARD = IMG_ICONS_DIR + "fast-forward.png";
    public static final String IMG_NEXT = IMG_ICONS_DIR + "next.png";
    public static final String IMG_REC_BUTTON = IMG_ICONS_DIR + "rec-button.png";
    public static final String IMG_REWIND = IMG_ICONS_DIR + "rewind.png";
    public static final String IMG_STOP_RECORD = IMG_ICONS_DIR + "stop-record.png";
    
    // IMG Buttons
    public static final String IMG_NO_CONNECTION = IMG_BUTTONS_DIR + "no-wifi.png";
    public static final String IMG_CONNECTION = IMG_BUTTONS_DIR + "conectado.png";
    public static final String IMG_ = IMG_BUTTONS_DIR + ".png";

    // Lang files
    public static final String LANG_MESSAGES = LANG_DIR + "messages.properties";
    public static final String LANG_MESSAGES_EN = LANG_DIR + "messages_en.properties";
}
