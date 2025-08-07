package org.axolotlj.remotehealth.desktop.service.serial;

public record SerialDeviceInfo(String systemPortName, String descriptivePortName, String manufacturer) {
}