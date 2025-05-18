package org.axolotlj.RemoteHealth.sensor.io;

import java.io.FileReader;
import java.io.Reader;
import java.nio.file.Path;
import java.util.ArrayList;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.axolotlj.RemoteHealth.sensor.data.DataPoint;
import org.axolotlj.RemoteHealth.sensor.handle.SensorValueParser;
import org.axolotlj.RemoteHealth.sensor.data.SensorValue;

@SuppressWarnings("deprecation")
/**
 * Clase encargada de cargar y procesar archivos CSV que contienen datos de
 * sensores.
 */
public class CsvDataManager {

	/**
	 * Carga un archivo CSV y devuelve una lista de DataPoint válidos.
	 *
	 * @param filePath ruta al archivo CSV.
	 * @return lista de DataPoint.
	 * @throws Exception si ocurre un error al leer el archivo.
	 */
	public static ArrayList<DataPoint> load(Path filePath) throws Exception {
		ArrayList<DataPoint> dataList = new ArrayList<>();

		try (Reader reader = new FileReader(filePath.toFile());
				CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {

			for (CSVRecord record : parser) {
				DataPoint dataPoint = parseRecord(record);
				if (dataPoint != null) {
					dataList.add(dataPoint);
				}
			}
		}

		return dataList;
	}

	/**
	 * Cuenta la cantidad de registros válidos en el archivo.
	 *
	 * @param filePath ruta al archivo CSV.
	 * @return número de registros válidos.
	 * @throws Exception si ocurre un error al leer el archivo.
	 */
	public static int countValidDataRows(Path filePath) throws Exception {
		int count = 0;

		try (Reader reader = new FileReader(filePath.toFile());
				CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {

			for (CSVRecord record : parser) {
				if (parseRecord(record) != null) {
					count++;
				}
			}
		}

		return count;
	}

	/**
	 * Calcula la duración total de la muestra en base a los timestamps.
	 *
	 * @param filePath ruta al archivo CSV.
	 * @return duración en milisegundos.
	 * @throws Exception si ocurre un error al leer el archivo.
	 */

	public static long calculateDuration(Path filePath) throws Exception {
		Long firstTimestamp = null;
		Long lastTimestamp = null;

		try (Reader reader = new FileReader(filePath.toFile());
				CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {

			for (CSVRecord record : parser) {
				String timestampHex = record.get("timestamp");

				if (timestampHex == null || timestampHex.isEmpty() || "NR".equalsIgnoreCase(timestampHex)) {
					continue;
				}

				try {
					long timestamp = Long.parseLong(timestampHex, 16);

					if (firstTimestamp == null) {
						firstTimestamp = timestamp;
					}

					lastTimestamp = timestamp;

				} catch (NumberFormatException e) {
					// Valor de timestamp malformado, ignorar
				}
			}
		}

		if (firstTimestamp != null && lastTimestamp != null) {
			return lastTimestamp - firstTimestamp;
		}

		return 0;
	}

	private static DataPoint parseRecord(CSVRecord record) {
		try {
			long timestamp = Long.parseLong(record.get("timestamp"), 16);

			SensorValue<Short> ecg = SensorValueParser.parseShort(record.get("ecg"));
			SensorValue<Float> acc = SensorValueParser.parseFloat(record.get("acc"));
			SensorValue<Float> temp = SensorValueParser.parseFloat(record.get("temp"));
			SensorValue<Integer> ir = SensorValueParser.parseInt(record.get("ir"));
			SensorValue<Integer> red = SensorValueParser.parseInt(record.get("red"));

			return new DataPoint(timestamp, ecg, acc, temp, ir, red);

		} catch (Exception e) {
			return null;
		}
	}
}
