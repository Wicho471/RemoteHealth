package org.axolotlj.remotehealth.core.model;

/**
 * Representa la información general del dispositivo.
 */
public class DeviceInfo {
	public String osName;
	public String osVersion;
	public String architecture;
	public String cpuModel;
	public int logicalProcessorCount;
	public long totalMemoryBytes;
	public long availableMemoryBytes;
	public String hostname;

	@Override
	public String toString() {
		// Conversión de bytes a gigabytes con 2 decimales
		String totalMemoryGB = String.format("%.2f GB", totalMemoryBytes / (1024.0 * 1024 * 1024));
		String availableMemoryGB = String.format("%.2f GB", availableMemoryBytes / (1024.0 * 1024 * 1024));

		return "DeviceInfo {" +
				"osName='" + osName + '\'' +
				", osVersion='" + osVersion + '\'' +
				", architecture='" + architecture + '\'' +
				", cpuModel='" + cpuModel + '\'' +
				", logicalProcessorCount=" + logicalProcessorCount +
				", totalMemory=" + totalMemoryGB +
				", availableMemory=" + availableMemoryGB +
				", hostname='" + hostname + '\'' +
				'}';
	}

}