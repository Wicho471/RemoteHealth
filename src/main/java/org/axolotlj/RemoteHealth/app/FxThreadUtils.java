package org.axolotlj.RemoteHealth.app;


import javafx.application.Platform;

public final class FxThreadUtils {

    private FxThreadUtils() {}

    /**
     * Ejecuta una acción en el hilo de JavaFX.
     * Si ya estás en ese hilo, la ejecuta de inmediato.
     * Si no, la encola con Platform.runLater().
     *
     * @param action Acción a ejecutar en el hilo de la interfaz.
     */
    public static void runOnUIThread(Runnable action) {
        if (Platform.isFxApplicationThread()) {
            action.run();
        } else {
            Platform.runLater(action);
        }
    }
}