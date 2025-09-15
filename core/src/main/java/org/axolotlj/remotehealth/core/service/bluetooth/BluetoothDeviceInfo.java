package org.axolotlj.remotehealth.core.service.bluetooth;

/**
 * Representa un dispositivo Bluetooth disponible.
 */
public class BluetoothDeviceInfo {

    private final String name;
    private final String address;

    public BluetoothDeviceInfo(String name, String address) {
        this.name = name;
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }
}