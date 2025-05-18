package org.axolotlj.RemoteHealth.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public class TxtUtils {
	public static String loadText(String path) {
	    try (InputStream is = TxtUtils.class.getResourceAsStream(path);
	         BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
	        return reader.lines().collect(Collectors.joining("\n"));
	    } catch (IOException e) {
	        return "No se pudo cargar la información.";
	    }
	}
	
	public static String loadAboutText(String path) {
	    try {
	        InputStream input = TxtUtils.class.getResourceAsStream(path);
	        if (input == null) {
	            return "No se pudo cargar el archivo: " + path;
	        }

	        String content = new String(input.readAllBytes(), StandardCharsets.UTF_8);
	        return content.replace("{{VERSION}}", VersionUtils.getVersion());
	    } catch (IOException e) {
	        System.err.println("TxtUtils.loadAboutText: " + e.getMessage());
	        return "Error al cargar el archivo: " + path;
	    }
	}

}
