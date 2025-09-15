package org.axolotlj.remotehealth.mobile;

import java.io.IOException;

import org.axolotlj.remotehealth.core.AppContext;
import org.axolotlj.remotehealth.core.config.PlatformConfigurator;
import org.axolotlj.remotehealth.core.javafx.util.FxmlUtils;
import org.axolotlj.remotehealth.core.logger.Log;
import org.axolotlj.remotehealth.core.logger.api.DataLogger;
import org.axolotlj.remotehealth.mobile.navigation.ViewManager;
import org.axolotlj.remotehealth.mobile.service.websocket.WebSocketManager;
import org.axolotlj.remotehealth.mobile.service.websocket.WebSocketServerSimulator;
import org.axolotlj.remotehealth.mobile.utils.MobilePaths;

import com.gluonhq.attach.lifecycle.LifecycleEvent;
import com.gluonhq.attach.lifecycle.LifecycleService;
import com.gluonhq.attach.util.Platform;
import com.gluonhq.charm.glisten.application.MobileApplication;
import com.gluonhq.charm.glisten.mvc.View;

import javafx.scene.Scene;
import javafx.scene.control.Label;

@SuppressWarnings("deprecation")
public class MobileApp extends MobileApplication {
	
	public static PlatformConfigurator configurator;

	static {
		System.out.println("[Remote Health] preferIPv4Stack=" + System.getProperty("java.net.preferIPv4Stack"));
		System.out.println("[Remote Health] preferIPv6Addresses=" + System.getProperty("java.net.preferIPv6Addresses"));
	}
	
	@Override
	public void init() {
		System.out.println("[Remote Health] Iniciando Init");
		System.out.println("[Remote Health] Revisando informacion del disp");
		configurator.getDeviceInfo();
		DataLogger dataLogger = Log.get();
		dataLogger.logDebug("Entrando en el metodo init");
		LifecycleService.create().ifPresent(service -> {
			service.addListener(LifecycleEvent.PAUSE, () -> {
				dataLogger.logInfo("App en pausa");
			});
			service.addListener(LifecycleEvent.RESUME, () -> {
				dataLogger.logInfo("App reanudada");
			});
		});
		initViews();
		dataLogger.logDebug("Saliendo del metodo init");
		System.out.println("[Remote Health] Terminando init");
	}

	@Override
	public void postInit(Scene scene) {
		System.out.println("[Remote Health] Iniciando post init");
		Log.get().logDebug("Iniciando post init");
		if (Platform.isDesktop()) {
			Log.get().logDebug("Modo escritorio, cambiando disposicion de la pantalla");
			scene.getWindow().setWidth(400);
			scene.getWindow().setHeight(700);
		}
		ViewManager.showHomeView();
		System.out.println("[Remote Health] Terminando post init");
	}

	@Override
	public void stop() {
		Log.get().logInfo("Aplicación cerrándose");
		AppContext.getInstance().finalize();
	}

	private void initViews() {
		addView(MobileApplication.HOME_VIEW, MobilePaths.HOME);
		addView(ViewManager.SCANNER_VIEW, MobilePaths.QR_SCANNER);
		addView(ViewManager.MONITOR_VIEW, MobilePaths.MONITOR);
	}

	private void addView(String name, String view) {
		addViewFactory(name, () -> loadView(view));
	}

	private View loadView(String fxml) {
		try {
			return FxmlUtils.loadFXML(fxml).load();
		} catch (IOException e) {
//			System.err.println(" Ocurrio un error cargando la vista '"+fxml+"'");
			Log.get().logException("Ocurrio un error cargando la vista '" + fxml + "'", e);
			e.printStackTrace();
			return new View(new Label("Error cargando " + fxml));
		}
	}

	public static void main(String[] args) {
            
		System.out.println("[Remote Health]");
		MobileApp.configurator = new MobileConfigurator();
		System.out.println("[Remote Health] Revisando rutas");
		configurator.checkPaths();
		System.out.println("[Remote Health] Revisando argumentos del del disp");
		configurator.getRuntimeArgs();
		System.out.println("[Remote Health] Revisando configuraciones del administrador");
		configurator.devConfigs();

		System.out.println("[Remote Health] Configuracion terminada");
		Log.get().logInfo("Configuracion terminada ");

		// CommonApp.initialize();
		AppContext.initialize(new WebSocketServerSimulator(), queue -> new WebSocketManager(queue));
		launch(args);
	}
}
