package org.axolotlj.remotehealth.desktop.service;

import org.axolotlj.remotehealth.core.exception.DeviceInfoException;
import org.axolotlj.remotehealth.core.logger.Log;
import org.axolotlj.remotehealth.core.model.DeviceInfo;
import org.axolotlj.remotehealth.core.service.DeviceInfoService;

import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.software.os.OperatingSystem;

/**
 * Implementación del proveedor de información del dispositivo para escritorio.
 */
public class DesktopDeviceInfoProvider implements DeviceInfoService {

    @Override
    public DeviceInfo getDeviceInfo() throws DeviceInfoException {
        try {
            SystemInfo systemInfo = new SystemInfo();
            OperatingSystem os = systemInfo.getOperatingSystem();
            CentralProcessor processor = systemInfo.getHardware().getProcessor();
            GlobalMemory memory = systemInfo.getHardware().getMemory();

            DeviceInfo info = new DeviceInfo();
            info.osName = os.getFamily();
            info.osVersion = os.getVersionInfo().getVersion();
            info.architecture = System.getProperty("os.arch");
            info.cpuModel = processor.getProcessorIdentifier().getName();
            info.logicalProcessorCount = processor.getLogicalProcessorCount();
            info.totalMemoryBytes = memory.getTotal();
            info.availableMemoryBytes = memory.getAvailable();
            info.hostname = os.getNetworkParams().getHostName();
            return info;
        } catch (Exception e) {
            Log.get().logFatal("Error obteniendo informacion sobre el dispositivo: " + e.getMessage());
            throw new DeviceInfoException("No se pudo obtener la información del dispositivo");
        }
    }
}
