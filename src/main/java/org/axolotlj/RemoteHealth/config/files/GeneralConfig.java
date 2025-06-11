package org.axolotlj.RemoteHealth.config.files;

import org.axolotlj.RemoteHealth.config.ConfigFileHelper;
import org.axolotlj.RemoteHealth.config.PropertiesManager;

import java.nio.file.Path;

public class GeneralConfig {

    private static final String CONFIG_FILE_NAME = "general.properties";
    private final PropertiesManager manager;

    public GeneralConfig() {
        Path configDir = ConfigFileHelper.getConfigDir();
        Path configFilePath = configDir.resolve(CONFIG_FILE_NAME);

        try {
            ConfigFileHelper.copyDefaultIfMissing(CONFIG_FILE_NAME, configFilePath);
        } catch (Exception e) {
            System.err.println("Error al copiar archivo de configuración general por defecto: " + e.getMessage());
        }

        this.manager = new PropertiesManager(configFilePath);
    }

    public boolean isDeveloperMode() {
        String value = manager.get("developerMode");
        return value != null && value.equalsIgnoreCase("true");
    }

    public void setDeveloperMode(boolean enabled) {
        manager.set("developerMode", Boolean.toString(enabled));
    }
}
