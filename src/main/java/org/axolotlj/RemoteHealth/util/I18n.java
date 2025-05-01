package org.axolotlj.RemoteHealth.util;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class I18n {

    private static Locale currentLocale = Locale.getDefault();
    private static ResourceBundle bundle = loadBundle(currentLocale);

    private static ResourceBundle loadBundle(Locale locale) {
        try {
            return ResourceBundle.getBundle("org.axolotlj.RemoteHealth.lang.messages", locale);
        } catch (MissingResourceException e) {
            System.err.println("I18n:: No se encontró archivo de recursos para: " + locale);
            return ResourceBundle.getBundle("org.axolotlj.RemoteHealth.lang.messages", Locale.ENGLISH);
        }
    }

    public static void setLocale(Locale locale) {
        currentLocale = locale;
        bundle = loadBundle(locale);
    }

    public static Locale getCurrentLocale() {
        return currentLocale;
    }

    public static String get(String key) {
        try {
            return bundle.getString(key);
        } catch (MissingResourceException e) {
            return '!' + key + '!';
        }
    }

    public static String get(String key, Object... args) {
        String pattern = get(key);
        return MessageFormat.format(pattern, args);
    }
}
