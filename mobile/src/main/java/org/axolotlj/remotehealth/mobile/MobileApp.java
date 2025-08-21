package org.axolotlj.remotehealth.mobile;

import java.io.IOException;

import org.axolotlj.remotehealth.core.AppContext;
import org.axolotlj.remotehealth.core.CommonApp;
import org.axolotlj.remotehealth.core.config.ConfigFileHelper;
import org.axolotlj.remotehealth.core.config.PlatformConfigurator;
import org.axolotlj.remotehealth.core.javafx.FxmlUtils;
import org.axolotlj.remotehealth.core.logger.DataLogger;
import org.axolotlj.remotehealth.core.logger.Log;
import org.axolotlj.remotehealth.mobile.navigation.ViewManager;
import org.axolotlj.remotehealth.mobile.storage.MobilePathResolver;
import org.axolotlj.remotehealth.mobile.utils.DevUtils;
import org.axolotlj.remotehealth.mobile.utils.MobilePaths;

import com.gluonhq.attach.lifecycle.LifecycleEvent;
import com.gluonhq.attach.lifecycle.LifecycleService;
import com.gluonhq.charm.glisten.application.MobileApplication;
import com.gluonhq.charm.glisten.mvc.View;

import javafx.scene.Scene;
import javafx.scene.control.Label;

public class MobileApp extends MobileApplication {
	
	//private static final Logger dataLogger = LoggerFactory.getLogger(MobileApp.class);
	
	@Override
	public void init() {
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
	}

	@Override
	public void postInit(Scene scene) {
//		System.out.println("  Iniciando post init");
		Log.get().logDebug("Iniciando post init");
		if (DevUtils.isDevMode()) {
			Log.get().logDebug("Modo desarrolador detectado, cambiando disposicion de la pantalla");
			scene.getWindow().setWidth(400);
			scene.getWindow().setHeight(700);
		}
		ViewManager.showHomeView();
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
			Log.get().logException("Ocurrio un error cargando la vista '"+fxml+"'", e);
			e.printStackTrace();
			return new View(new Label("Error cargando " + fxml));
		}
	}

	public static void main(String[] args) {
		PlatformConfigurator configurator = new MobileConfigurator();
		configurator.checkPaths();
		configurator.getDeviceInfo();
		configurator.getRuntimeArgs();
		configurator.devConfigs();
		
		Log.get().logInfo("Configuracion terminada ");
		
		CommonApp.initialize();
		
		launch(args);
	}
}
