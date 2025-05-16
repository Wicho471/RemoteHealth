package org.axolotlj.RemoteHealth.util;

import java.io.FileReader;
import java.io.Reader;
import java.nio.file.Path;
import java.util.ArrayList;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.tuple.MutablePair;
import org.axolotlj.RemoteHealth.model.SensorValue;
import org.axolotlj.RemoteHealth.model.StructureData;

public class DataHandler {

	public enum SensorField {
		ECG("timestamp", "ECG"), ACCEL("acc", "Acceleration"), TEMP("temp", "Temperature"), IR("ir", "Infrared"),
		RED("red", "Red");

		private final String key;
		private final String displayName;

		SensorField(String displayName, String key) {
			this.displayName = displayName;
			this.key = key;
		}

		public String getKey() {
			return key;
		}

		public String getDisplayName() {
			return displayName;
		}

		@Override
		public String toString() {
			return displayName;
		}
	}

	public static double[] extractValues(ArrayList<StructureData> dataList, SensorField field) {
		return dataList.stream().mapToDouble(data -> switch (field) {
		case ECG -> data.getEcg().getValue();
		case ACCEL -> data.getAccel().getValue();
		case TEMP -> data.getTemp().getValue();
		case IR -> data.getIr().getValue();
		case RED -> data.getRed().getValue();
		}).toArray();
	}

	public static ArrayList<MutablePair<Long, Double>> extractValidPairs(ArrayList<StructureData> dataList,
			SensorField field) {
		ArrayList<MutablePair<Long, Double>> pairs = new ArrayList<>();

		for (StructureData data : dataList) {
			SensorValue<?> sensorValue = switch (field) {
			case ECG -> data.getEcg();
			case ACCEL -> data.getAccel();
			case TEMP -> data.getTemp();
			case IR -> data.getIr();
			case RED -> data.getRed();
			};

			if (sensorValue.isValid()) {
				Number value = (Number) sensorValue.getValue();
				pairs.add(MutablePair.of(data.getTimeStamp(), value.doubleValue()));
			}
		}

		return pairs;
	}

	public static <T> ArrayList<MutablePair<Long, SensorValue<T>>> extractRawPairs(ArrayList<StructureData> dataList,
			SensorField field) {

		ArrayList<MutablePair<Long, SensorValue<T>>> pairs = new ArrayList<>();

		for (StructureData data : dataList) {
			SensorValue<?> sensorValue = switch (field) {
			case ECG -> data.getEcg();
			case ACCEL -> data.getAccel();
			case TEMP -> data.getTemp();
			case IR -> data.getIr();
			case RED -> data.getRed();
			};

			@SuppressWarnings("unchecked")
			SensorValue<T> typedValue = (SensorValue<T>) sensorValue;

			pairs.add(MutablePair.of(data.getTimeStamp(), typedValue));
		}

		return pairs;
	}

	@SuppressWarnings("deprecation")
	public static ArrayList<StructureData> load(Path filePath) throws Exception {
		ArrayList<StructureData> dataList = new ArrayList<>();

		try (Reader reader = new FileReader(filePath.toFile());
				CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {

			for (CSVRecord record : csvParser) {
				String[] parts = new String[] { record.get("timestamp"), record.get("ecg"), record.get("acc"),
						record.get("temp"), record.get("ir"), record.get("red") };

				StructureData data = decodeData(parts);
				if (data != null) {
					dataList.add(data);
				}
			}
		}

		return dataList;
	}
	
	@SuppressWarnings("deprecation")
	public static int countDataRows(Path filePath) throws Exception {
	    int count = 0;

	    try (Reader reader = new FileReader(filePath.toFile());
	         CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {

	        for (CSVRecord record : csvParser) {
	            String[] parts = new String[]{
	                record.get("timestamp"),
	                record.get("ecg"),
	                record.get("acc"),
	                record.get("temp"),
	                record.get("ir"),
	                record.get("red")
	            };

	            StructureData data = decodeData(parts);
	            if (data != null) {
	                count++;
	            }
	        }
	    }

	    return count;
	}
	
	@SuppressWarnings("deprecation")
	public static long calculateDuration(Path filePath) throws Exception {
	    Long firstTimestamp = null;
	    Long lastTimestamp = null;

	    try (Reader reader = new FileReader(filePath.toFile());
	         CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {

	        for (CSVRecord record : csvParser) {
	            String timestampHex = record.get("timestamp");
	            if (timestampHex == null || timestampHex.isEmpty() || "NR".equals(timestampHex)) {
	                continue;
	            }

	            try {
	                long timestamp = Long.parseLong(timestampHex, 16);

	                if (firstTimestamp == null) {
	                    firstTimestamp = timestamp;
	                }
	                lastTimestamp = timestamp;

	            } catch (NumberFormatException e) {
	                // Ignorar si no es un timestamp válido
	            }
	        }
	    }

	    if (firstTimestamp != null && lastTimestamp != null) {
	        return lastTimestamp - firstTimestamp;  // Duración en la misma unidad del timestamp (normalmente milisegundos)
	    } else {
	        return 0;  // No hay datos válidos
	    }
	}



	private static StructureData decodeData(String[] parts) {
		if (parts.length != 6)
			return null;

		try {
			long timeStamp = Long.parseLong(parts[0], 16);

			SensorValue<Short> ecg = parseSensorShort(parts[1]);
			SensorValue<Float> accel = parseSensorFloat(parts[2]);
			SensorValue<Float> temp = parseSensorFloat(parts[3]);
			SensorValue<Integer> ir = parseSensorInt(parts[4]);
			SensorValue<Integer> red = parseSensorInt(parts[5]);

			return new StructureData(timeStamp, ecg, accel, temp, ir, red);
		} catch (Exception e) {
			return null;
		}
	}

	public static StructureData processData(String data) {
		String[] splitedData = data.split(",");
		if (splitedData.length != 6)
			return null;

		try {
			long timeStamp = Long.parseLong(splitedData[0], 16);

			SensorValue<Short> ecg = parseSensorShort(splitedData[1]);
			SensorValue<Float> accel = parseSensorFloat(splitedData[2]);
			SensorValue<Float> temp = parseSensorFloat(splitedData[3]);
			SensorValue<Integer> ir = parseSensorInt(splitedData[4]);
			SensorValue<Integer> red = parseSensorInt(splitedData[5]);

			return new StructureData(timeStamp, ecg, accel, temp, ir, red);
		} catch (Exception e) {
			return null;
		}
	}

	private static SensorValue<Short> parseSensorShort(String value) {
		try {
			if ("NR".equalsIgnoreCase(value))
				return SensorValue.notReady();
			if ("ERR".equalsIgnoreCase(value))
				return SensorValue.error();
			return SensorValue.valid(Short.parseShort(value, 16));
		} catch (NumberFormatException e) {
			return SensorValue.malformed();
		}
	}

	private static SensorValue<Integer> parseSensorInt(String value) {
		try {
			if ("NR".equalsIgnoreCase(value))
				return SensorValue.notReady();
			if ("ERR".equalsIgnoreCase(value))
				return SensorValue.error();
			return SensorValue.valid(Integer.parseInt(value, 16));
		} catch (NumberFormatException e) {
			return SensorValue.malformed();
		}
	}

	private static SensorValue<Float> parseSensorFloat(String value) {
		try {
			if ("NR".equalsIgnoreCase(value))
				return SensorValue.notReady();
			if ("ERR".equalsIgnoreCase(value))
				return SensorValue.error();
			return SensorValue.valid(Float.parseFloat(value));
		} catch (NumberFormatException e) {
			return SensorValue.malformed();
		}
	}

}
