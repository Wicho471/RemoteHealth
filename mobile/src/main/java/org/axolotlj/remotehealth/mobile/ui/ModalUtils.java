package org.axolotlj.remotehealth.mobile.ui;

import java.io.IOException;

import org.axolotlj.remotehealth.core.javafx.FxmlUtils;
import org.axolotlj.remotehealth.core.logger.Log;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.image.Image;

import com.gluonhq.charm.glisten.control.Dialog;

/**
 * Utilidad para mostrar una ventana modal compatible con Gluon Mobile.
 */
public class ModalUtils {

    /**
     * Abre una ventana modal compatible con dispositivos móviles utilizando un Dialog de Gluon.
     *
     * @param fxmlPath Ruta del archivo FXML
     * @param title Título de la ventana
     * @param parentController Controlador padre a inyectar si es posible (puede ser null)
     * @param image Ícono (no se usa en Gluon Mobile pero se mantiene por compatibilidad)
     */
    public static void openModalWindow(String fxmlPath, String title, Object parentController, Image image) {
        try {
            FXMLLoader loader = FxmlUtils.loadFXML(fxmlPath);
            Parent root = loader.load();

            Object controller = loader.getController();

            if (parentController != null && controller != null) {
                try {
                    var method = controller.getClass().getMethod("setParentController", parentController.getClass());
                    method.invoke(controller, parentController);
                } catch (NoSuchMethodException ignored) {
                    // Método no necesario
                } catch (Exception e) {
                    Log.get().logException("Error al inyectar el controlador padre", e);
                }
            }

            Dialog<Void> dialog = new Dialog<>(title);
            dialog.setContent(root);
            dialog.showAndWait();

        } catch (IOException e) {
            Log.get().logException("Error al cargar el FXML", e);
        }
    }
}
