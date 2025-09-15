package org.axolotlj.remotehealth.desktop.service.bluetooth;

import org.axolotlj.remotehealth.core.service.bluetooth.BluetoothService;
import org.axolotlj.remotehealth.desktop.service.bluetooth.so.LinuxBluetoothService;
import org.axolotlj.remotehealth.desktop.service.bluetooth.so.WindowsMacBluetoothService;

/**
 * Fábrica para obtener la implementación de Bluetooth según el sistema
 * operativo.
 */
public class BluetoothServiceFactory {

	public static BluetoothService getBluetoothService() {
		String os = System.getProperty("os.name").toLowerCase();

		if (os.contains("linux")) {
			return new LinuxBluetoothService();
		} else if (os.contains("win") || os.contains("mac")) {
			return new WindowsMacBluetoothService();
		} else {
			throw new UnsupportedOperationException("Sistema operativo no soportado: " + os);
		}
	}
}
