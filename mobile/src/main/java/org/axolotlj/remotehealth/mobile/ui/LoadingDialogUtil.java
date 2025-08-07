package org.axolotlj.remotehealth.mobile.ui;

import com.gluonhq.charm.glisten.control.Dialog;
import com.gluonhq.charm.glisten.control.ProgressIndicator;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/**
 * Utilidad para mostrar una pantalla de carga en Gluon Mobile.
 */
public final class LoadingDialogUtil {

    private LoadingDialogUtil() {}

    /**
     * Crea y muestra un diálogo de carga con el mensaje proporcionado.
     *
     * @param message Mensaje a mostrar
     * @return Diálogo activo que puede ser cerrado cuando la tarea finalice
     */
    public static Dialog<Void> showLoading(String message) {
        ProgressIndicator indicator = new ProgressIndicator();
        Label label = new Label(message);

        VBox content = new VBox(15, indicator, label);
        content.setStyle("-fx-alignment: center; -fx-padding: 20;");

        Dialog<Void> dialog = new Dialog<>();
        dialog.setContent(content);
        dialog.setAutoHide(false);
        dialog.showAndWait();

        return dialog;
    }
}