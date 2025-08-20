package org.axolotlj.remotehealth.mobile.navigation;

import static org.axolotlj.remotehealth.core.concurrent.FxThreadUtils.runOnUIThread;

import com.gluonhq.charm.glisten.application.MobileApplication;

public class ViewManager {
    public static final String SCANNER_VIEW = "SCANNER_VIEW";
    public static final String MONITOR_VIEW = "MONITOR_VIEW";

    public static void showHomeView() {
        System.out.println("[Remote Health] Mostrando 'HomeView'");
        runOnUIThread(() -> {
            MobileApplication.getInstance().switchView(MobileApplication.HOME_VIEW);
        });
    }

    public static void showScannerView() {
        System.out.println("[Remote Health] Mostrando 'ScannerView'");
        runOnUIThread(() -> {
            MobileApplication.getInstance().switchView(SCANNER_VIEW);
        });
    }

    public static void showMonitorView() {
        System.out.println("[Remote Health] Mostrando 'MonitorView'");
        runOnUIThread(() -> {
            MobileApplication.getInstance().switchView(MONITOR_VIEW);
        });
    }
}
