//package org.axolotlj.remotehealth.mobile.service;
//
//import com.gluonhq.attach.display.DisplayService;
//import com.gluonhq.attach.lifecycle.LifecycleService;
//import com.gluonhq.attach.statusbar.StatusBarService;
//import com.gluonhq.attach.storage.StorageService;
//import com.gluonhq.attach.util.Platform;
//import com.gluonhq.attach.util.Util;
//
//import org.axolotlj.remotehealth.core.exception.DeviceInfoException;
//import org.axolotlj.remotehealth.core.model.DeviceInfo;
//import org.axolotlj.remotehealth.core.service.DeviceInfoService;
//import org.axolotlj.remotehealth.core.service.logger.Log;
//
//import java.util.Optional;
//
///**
// * Implementación del proveedor de información del dispositivo para dispositivos móviles usando Gluon Attach.
// */
//public class MobileDeviceInfoProvider implements DeviceInfoService {
//
//    @Override
//    public DeviceInfo getDeviceInfo() throws DeviceInfoException {
//        try {
//            DeviceInfo info = new DeviceInfo();
//
//            info.osName = Platform.getCurrent().toString();
//            info.osVersion = System.getProperty("os.version", "unknown");
//            info.architecture = System.getProperty("os.arch", "unknown");
//            info.cpuModel = "Not Available";
//            info.logicalProcessorCount = Runtime.getRuntime().availableProcessors();
//            info.totalMemoryBytes = Runtime.getRuntime().maxMemory();
//            info.availableMemoryBytes = Runtime.getRuntime().freeMemory();
//            info.hostname = "Not Available";
//
//            Optional<DisplayService> displayService = DisplayService.create();
//            displayService.ifPresent(ds -> {
//            	
//            });
//
//            Optional<Util> utilService = Util.create();
//            utilService.ifPresent(us -> info.deviceUUID = us.getUuid());
//
//            Optional<StatusBarService> statusBarService = StatusBarService.create();
//            statusBarService.ifPresent(sb -> sb.setColor(null));
//
//            Optional<StorageService> storageService = StorageService.create();
//            storageService.ifPresent(ss -> {
//            	ss.isExternalStorageWritable();
//            });
//
//            Optional<LifecycleService> lifecycleService = LifecycleService.create();
//            lifecycleService.ifPresent(ls -> ls.shutdown());
//
//            return info;
//        } catch (Exception e) {
//            Log.get().logError("Error obteniendo informacion del dispositivo móvil: " + e.getMessage());
//            throw new DeviceInfoException("No se pudo obtener la información del dispositivo móvil");
//        }
//    }
//}
