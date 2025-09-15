package org.axolotlj.remotehealth.mobile.navigation;

import static org.axolotlj.remotehealth.core.javafx.current.FxThreadUtils.runOnUIThread;

import org.axolotlj.remotehealth.core.logger.Log;
import org.axolotlj.remotehealth.core.logger.api.DataLogger;

import com.gluonhq.charm.glisten.application.MobileApplication;

@SuppressWarnings("deprecation")
public class ViewManager {
	private static final DataLogger DATA_LOGGER = Log.get();

	public static final String SCANNER_VIEW = "SCANNER_VIEW";
	public static final String MONITOR_VIEW = "MONITOR_VIEW";

	public static void showHomeView() {
		DATA_LOGGER.logDebug("Mostrando 'HomeView'");
		runOnUIThread(() -> {
			MobileApplication.getInstance().switchView(MobileApplication.HOME_VIEW);
		});
	}

	public static void showScannerView() {
		DATA_LOGGER.logDebug("Mostrando 'ScannerView'");
		runOnUIThread(() -> {
			MobileApplication.getInstance().switchView(SCANNER_VIEW);
		});
	}

	public static void showMonitorView() {
		DATA_LOGGER.logDebug("Mostrando 'MonitorView'");
		runOnUIThread(() -> {
			MobileApplication.getInstance().switchView(MONITOR_VIEW);
		});
	}
}
