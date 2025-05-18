package org.axolotlj.RemoteHealth.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class VersionUtils {

    private static final String VERSION;

    static {
        String v = "desconocida";
        try (InputStream input = VersionUtils.class.getResourceAsStream("/org/axolotlj/RemoteHealth/misc/version.properties")) {
            if (input != null) {
                Properties props = new Properties();
                props.load(input);
                v = props.getProperty("version", v);
            }
        } catch (IOException e) {
            System.err.println("VersionUtils: " + e.getMessage());
        }
        VERSION = v;
    }

    public static String getVersion() {
        return VERSION;
    }
}
