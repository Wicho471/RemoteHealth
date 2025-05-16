package org.axolotlj.RemoteHealth.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CsvUtils {
	
	public static List<String> getAllLines(String path) {
		if(!path.endsWith(".csv")) {
			System.err.println("El archivo es csv");
			return new ArrayList<>();
		}
		List<String> list = new ArrayList<String>();
    	try (InputStream is = CsvUtils.class.getClassLoader().getResourceAsStream(path)) {
    	    if (is != null) {
    	        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
    	            list = reader.lines().collect(Collectors.toList());
    	            if (!list.isEmpty()) {
    	                list.remove(0); 
    	            }
    	        }
    	    } else {
    	        System.err.println("CsvUtils::getAllLines -> Archivo CSV no encontrado en recursos.");
    	        return new ArrayList<>();
    	    }
    	} catch (IOException e) {
    		System.err.println("CsvUtils::getAllLines -> "+e.getMessage());
    	    return new ArrayList<>();
    	}
    	return list;
	}
	
	/**
	 * Obtiene una lista de archivos CSV válidos, descomprimiendo aquellos que estén en formato .csv.gz.
	 * Los archivos .gz se descomprimen a archivos temporales para poder ser tratados como CSV estándar.
	 *
	 * @param folder Carpeta donde buscar archivos .csv y .csv.gz
	 * @return Lista de pares [archivoOriginal, archivoDescomprimido o mismo archivo si no está comprimido]
	 */
	public static List<File> getAllCsvFiles(File folder) {
		List<File> result = new ArrayList<>();

		File[] files = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".csv") || name.toLowerCase().endsWith(".csv.gz"));
		if (files == null) {
			return result;
		}

		for (File file : files) {
			String fileName = file.getName();

			if (fileName.toLowerCase().endsWith(".csv")) {
				result.add(file);
			} else if (fileName.toLowerCase().endsWith(".csv.gz")) {
				try {
					String newFileName = fileName.substring(0, fileName.length() - 3); // Quita ".gz"
					Path tempFile = Path.of(System.getProperty("java.io.tmpdir"), newFileName);
					File targetFile = tempFile.toFile();
					targetFile.deleteOnExit();

					try (
						java.util.zip.GZIPInputStream gis = new java.util.zip.GZIPInputStream(new java.io.FileInputStream(file));
						java.io.FileOutputStream fos = new java.io.FileOutputStream(targetFile)
					) {
						byte[] buffer = new byte[1024];
						int len;
						while ((len = gis.read(buffer)) > 0) {
							fos.write(buffer, 0, len);
						}
					}

					result.add(targetFile);
				} catch (IOException e) {
					System.err.println("Error al descomprimir archivo: " + fileName + " -> " + e.getMessage());
				}
			}
		}

		return result;
	}


}
