package org.axolotlj.remotehealth.core.service.bluetooth;

import java.util.List;
import java.util.function.Consumer;

import org.axolotlj.remotehealth.core.cmd.CommandCommunicator;

/**
 * Define las operaciones comunes de Bluetooth que deben implementar
 * las clases específicas de cada sistema operativo.
 */
public interface BluetoothService extends CommandCommunicator {

    /**
     * Escanea los dispositivos Bluetooth disponibles.
     *
     * @return lista de dispositivos encontrados
     */
    List<BluetoothDeviceInfo> getDevices();

    /**
     * Establece conexión con un dispositivo Bluetooth.
     *
     * @param device dispositivo al que se desea conectar
     * @return true si la conexión fue exitosa, false en caso contrario
     */
    void establishConnection(BluetoothDeviceInfo device, Runnable onSuccess, Runnable onFailure);
    
    /**
     * Cierra la conexión con el dispositivo Bluetooth.
     */
    void closeConnection();
    
    /**
     * Verifica si esta encendido el bluetooth
     * @return si esta encendido
     */
    boolean isEnabled();
    
    /**
     * Si el dispositivo se desconecta
     * @param handleDisconection
     */
    void onSignalLost(Runnable handleDisconection);
}
