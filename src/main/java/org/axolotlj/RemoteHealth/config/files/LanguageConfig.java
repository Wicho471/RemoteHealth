package org.axolotlj.RemoteHealth.config.files;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

import org.axolotlj.RemoteHealth.config.ConfigFileHelper;

public class LanguageConfig {

    public static Locale loadSavedLocale() {
        Path langFile = ConfigFileHelper.getLanguageConfigFile();
        if (!Files.exists(langFile)) return new Locale("es"); // idioma por defecto

        try {
            String code = Files.readString(langFile).trim();
            return switch (code.toLowerCase()) {
                case "en" -> Locale.ENGLISH;
                default -> new Locale("es");
            };
        } catch (IOException e) {
            System.err.println("No se pudo leer configuración de idioma: " + e.getMessage());
            return new Locale("es");
        }
    }

    public static void saveLocale(Locale locale) {
        Path langFile = ConfigFileHelper.getLanguageConfigFile();
        try {
            Files.writeString(langFile, locale.getLanguage());
        } catch (IOException e) {
            System.err.println("No se pudo guardar configuración de idioma: " + e.getMessage());
        }
    }
}
