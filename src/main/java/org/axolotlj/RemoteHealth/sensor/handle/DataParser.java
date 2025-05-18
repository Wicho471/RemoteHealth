package org.axolotlj.RemoteHealth.sensor.handle;

import org.axolotlj.RemoteHealth.sensor.data.DataPoint;
import org.axolotlj.RemoteHealth.sensor.data.SensorValue;

public class DataParser {

	public static DataPoint process(String data) {
		String[] splitedData = data.split(",");
		if (splitedData.length != 6) return null;

		try {
			long timeStamp = Long.parseLong(splitedData[0], 16);
			SensorValue<Short> ecg = SensorValueParser.parseShort(splitedData[1]);
			SensorValue<Float> accel = SensorValueParser.parseFloat(splitedData[2]);
			SensorValue<Float> temp = SensorValueParser.parseFloat(splitedData[3]);
			SensorValue<Integer> ir = SensorValueParser.parseInt(splitedData[4]);
			SensorValue<Integer> red = SensorValueParser.parseInt(splitedData[5]);

			return new DataPoint(timeStamp, ecg, accel, temp, ir, red);
		} catch (Exception e) {
			return null;
		}
	}

	public static DataPoint process(String[] parts) {
		if (parts.length != 6) return null;

		try {
			long timeStamp = Long.parseLong(parts[0], 16);
			SensorValue<Short> ecg = SensorValueParser.parseShort(parts[1]);
			SensorValue<Float> accel = SensorValueParser.parseFloat(parts[2]);
			SensorValue<Float> temp = SensorValueParser.parseFloat(parts[3]);
			SensorValue<Integer> ir = SensorValueParser.parseInt(parts[4]);
			SensorValue<Integer> red = SensorValueParser.parseInt(parts[5]);

			return new DataPoint(timeStamp, ecg, accel, temp, ir, red);
		} catch (Exception e) {
			return null;
		}
	}
}
