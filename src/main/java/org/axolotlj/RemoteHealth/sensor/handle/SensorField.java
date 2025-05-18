package org.axolotlj.RemoteHealth.sensor.handle;

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