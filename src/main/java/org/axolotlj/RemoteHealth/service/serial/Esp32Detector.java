package org.axolotlj.RemoteHealth.service.serial;

import com.fazecast.jSerialComm.SerialPort;
import java.util.Optional;

/**
 * Clase encargada de detectar si un puerto corresponde a un ESP32.
 */
public class Esp32Detector {

    /**
     * Intenta identificar un puerto que pertenezca a un ESP32.
     *
     * @param port Puerto serial a analizar
     * @return Un objeto con información del dispositivo si parece ser un ESP32
     */
    public Optional<SerialDeviceInfo> detect(SerialPort port) {
        String desc = port.getDescriptivePortName().toLowerCase();
        String mfg = port.getPortDescription().toLowerCase();

        if (desc.contains("usb") && (mfg.contains("silicon labs") || mfg.contains("esp") || mfg.contains("cp210x"))) {
            return Optional.of(new SerialDeviceInfo(
                port.getSystemPortName(),
                port.getDescriptivePortName(),
                port.getPortDescription()
            ));
        }

        return Optional.empty();
    }
}