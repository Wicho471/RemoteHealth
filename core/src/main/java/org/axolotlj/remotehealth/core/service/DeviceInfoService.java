package org.axolotlj.remotehealth.core.service;

import org.axolotlj.remotehealth.core.exception.DeviceInfoException;
import org.axolotlj.remotehealth.core.model.DeviceInfo;

/**
 * Servicio de obtención de información del dispositivo.
 */
public interface DeviceInfoService {
    /**
     * Obtiene la información del sistema actual.
     * @return objeto con información del dispositivo
     * @throws DeviceInfoException si ocurre un error durante la recopilación
     */
    DeviceInfo getDeviceInfo() throws DeviceInfoException;
}