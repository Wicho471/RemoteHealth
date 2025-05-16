package org.axolotlj.RemoteHealth.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

public class TxtUtils {
	public static String loadAboutText(String path) {
	    try (InputStream is = TxtUtils.class.getResourceAsStream(path);
	         BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
	        return reader.lines().collect(Collectors.joining("\n"));
	    } catch (IOException e) {
	        return "No se pudo cargar la información.";
	    }
	}

}
