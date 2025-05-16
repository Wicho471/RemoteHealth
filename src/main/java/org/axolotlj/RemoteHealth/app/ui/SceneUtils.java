package org.axolotlj.RemoteHealth.app.ui;

import java.io.IOException;
import java.util.function.Consumer;

import org.axolotlj.RemoteHealth.core.AppContext;
import org.axolotlj.RemoteHealth.core.AppContext.ContextAware;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class SceneUtils {
    /**
     * Abre una nueva ventana modal a partir de un archivo FXML.
     *
     * @param fxmlPath Ruta del archivo FXML
     * @param title Título de la ventana
     * @param parentController Controlador padre a inyectar si es posible (puede ser null)
     * @param errorHandler Consumidor opcional para manejar errores
     */
    public static void openModalWindow(String fxmlPath, String title, Object parentController, Image image, Consumer<String> errorHandler ) {
        try {
            FXMLLoader loader = FxmlUtils.loadFXML(fxmlPath);
            Parent root = loader.load();

            Object controller = loader.getController();
            if (controller instanceof ContextAware contextAware) {
                contextAware.setAppContext(AppContext.getInstance());
            }

            if (parentController != null && controller != null) {
                try {
                    var method = controller.getClass().getMethod("setParentController", parentController.getClass());
                    method.invoke(controller, parentController);
                } catch (NoSuchMethodException ignored) {
                    // Método no necesario
                } catch (Exception e) {
                    notifyError(errorHandler, "SceneUtils.openModalWindow -> Error al inyectar el controlador padre: " + e.getMessage());
                }
            }

            Stage stage = new Stage();
            stage.setTitle(title);
            stage.getIcons().add(image);
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

        } catch (IOException e) {
            notifyError(errorHandler, "SceneUtils.openModalWindow -> Error al cargar el FXML: " + e.getMessage());
        }
    }
    
    private static void notifyError(Consumer<String> handler, String message) {
        if (handler != null) {
            handler.accept(message);
        } else {
            System.err.println(message);
        }
    }
}
