package org.axolotlj.remotehealth.mobile;

import java.io.IOException;

import org.axolotlj.remotehealth.core.AppContext;
import org.axolotlj.remotehealth.core.CommonApp;
import org.axolotlj.remotehealth.core.config.PlatformConfigurator;
import org.axolotlj.remotehealth.core.javafx.FxmlUtils;
import org.axolotlj.remotehealth.mobile.navigation.ViewManager;
import org.axolotlj.remotehealth.mobile.utils.DevUtils;
import org.axolotlj.remotehealth.mobile.utils.MobilePaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gluonhq.attach.lifecycle.LifecycleEvent;
import com.gluonhq.attach.lifecycle.LifecycleService;
import com.gluonhq.charm.glisten.application.MobileApplication;
import com.gluonhq.charm.glisten.mvc.View;

import javafx.scene.Scene;
import javafx.scene.control.Label;

@SuppressWarnings("deprecation")
public class MobileApp extends MobileApplication {
	
	private static final Logger dataLogger = LoggerFactory.getLogger(MobileApp.class);

	@Override
	public void init() {
		System.out.println("[Remote Health]  Entrando en el metodo init");
		LifecycleService.create().ifPresent(service -> {
			service.addListener(LifecycleEvent.PAUSE, () -> {
				System.out.println();
				dataLogger.info("[Remote Health]  App en pausa");
			});
			service.addListener(LifecycleEvent.RESUME, () -> {
				dataLogger.info("[Remote Health] App reanudada");
			});
		});
		initViews();
		System.out.println("[Remote Health] Saliendo del metodo init");
	}

	@Override
	public void postInit(Scene scene) {
		System.out.println("[Remote Health]  Iniciando post init");
		dataLogger.info("[Remote Health] Iniciando post init");
		if (DevUtils.isDevMode()) {
			scene.getWindow().setWidth(400);
			scene.getWindow().setHeight(700);
		}
		ViewManager.showHomeView();
	}

	@Override
	public void stop() {
		dataLogger.info("[Remote Health] Aplicación cerrándose");
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
			System.err.println("[Remote Health] Ocurrio un error cargando la vista '"+fxml+"'");
			dataLogger.error("[Remote Health] Ocurrio un error cargando la vista '"+fxml+"'", e);
			e.printStackTrace();
			return new View(new Label("Error cargando " + fxml));
		}
	}

	public static void main(String[] args) {
		dataLogger.info("Remote Health] Impresion de mensaje con Logger");
		System.out.println("[Remote Health] Impresion de mensaje con System.out.println");		
		
		PlatformConfigurator configurator = new MobileConfigurator();
		configurator.checkPaths();
		configurator.getDeviceInfo();
		configurator.getRuntimeArgs();
		configurator.devConfigs();
		dataLogger.info("[Remote Health] Configuracion terminada ");
		
		CommonApp.initialize();
		
		launch(args);
	}
}
