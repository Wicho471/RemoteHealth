package org.axolotlj.remotehealth.desktop.service.watchdog;

import org.axolotlj.remotehealth.core.logger.Log;
import org.axolotlj.remotehealth.core.service.watchdog.TaskToWatch;
import org.axolotlj.remotehealth.core.service.watchdog.Watchdog;

import javafx.application.Platform;

public class JavaFXObserver {
	private final int timeOut = 30000; //ms
	
	private Watchdog watchdog;
	private DesktopRestartHandler restartHandler;
	private boolean trigger;
	
	public JavaFXObserver() {
		this.trigger = false;
		this.restartHandler = new DesktopRestartHandler();
	}

	public void run() {
		Log.get().logInfo("Inciando watchdog para JavaFX thread");
		TaskToWatch fxExecutor = task -> Platform.runLater(task);

		watchdog = new Watchdog(timeOut, fxExecutor, restartHandler);
		
		watchdog.start();
	}

	public void finalize() {
		if(!trigger) {
			watchdog.stop();
		}
	}
}
