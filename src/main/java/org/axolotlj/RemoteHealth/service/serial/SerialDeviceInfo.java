package org.axolotlj.RemoteHealth.service.serial;

public record SerialDeviceInfo(String systemPortName, String descriptivePortName, String manufacturer) {
}