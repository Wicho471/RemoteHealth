package org.axolotlj.remotehealth.core.concurrent;

import org.axolotlj.remotehealth.core.logger.Log;

import javafx.application.Platform;

public final class FxThreadUtils {

	private FxThreadUtils() {
	}

	/**
	 * Ejecuta una acción en el hilo de JavaFX. Si ya estás en ese hilo, la ejecuta
	 * de inmediato. Si no, la encola con Platform.runLater().
	 *
	 * @param action Acción a ejecutar en el hilo de la interfaz.
	 */
	public static void runOnUIThread(Runnable action) {
		try {
			if (Platform.isFxApplicationThread()) {
				action.run();
			} else {
				Platform.runLater(action);
			}
		} catch (IllegalStateException e) {
			Log.get().logException("El hilo JavaFx aun no esta inicializado", e);
		} catch (Exception e) {
			Log.get().logException("Ocurrio un error inesperado en el hilo JavaFX", e);
		}
	}
}