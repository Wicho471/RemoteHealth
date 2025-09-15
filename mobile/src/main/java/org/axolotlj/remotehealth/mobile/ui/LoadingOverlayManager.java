package org.axolotlj.remotehealth.mobile.ui;

import org.axolotlj.remotehealth.core.logger.Log;
import org.axolotlj.remotehealth.core.logger.api.DataLogger;

import com.gluonhq.charm.glisten.application.MobileApplication;
import com.gluonhq.charm.glisten.control.ProgressIndicator;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/**
 * Clase para mostrar y ocultar una pantalla de carga global con indicador centrado
 * y que libera interacción completamente al ocultarse.
 */
public final class LoadingOverlayManager {

    private static final DataLogger dataLogger = Log.get();
    private static StackPane overlayPane;

    private LoadingOverlayManager() { }

    public static void showLoading() {
        Platform.runLater(() -> {
            if (overlayPane != null && overlayPane.isVisible()) {
                return;
            }

            Scene scene = getCurrentScene();
            if (scene == null) {
                dataLogger.logWarn("No hay escena activa para mostrar el overlay.");
                return;
            }

            Rectangle background = new Rectangle();
            background.widthProperty().bind(scene.widthProperty());
            background.heightProperty().bind(scene.heightProperty());
            background.setFill(Color.rgb(0, 0, 0, 0.5));

            ProgressIndicator progressIndicator = new ProgressIndicator();
            progressIndicator.setRadius(24);

            overlayPane = new StackPane(background, progressIndicator);
            overlayPane.setAlignment(Pos.CENTER);
            overlayPane.setPickOnBounds(true);
            overlayPane.prefWidthProperty().bind(scene.widthProperty());
            overlayPane.prefHeightProperty().bind(scene.heightProperty());
            overlayPane.setLayoutX(0);
            overlayPane.setLayoutY(0);

            Node root = scene.getRoot();
            if (root instanceof Pane pane) {
                pane.getChildren().add(overlayPane);
            } else {
                dataLogger.logWarn("El root actual no es Pane, no se puede mostrar overlay.");
            }
        });
    }

    public static void hideLoading() {
        Platform.runLater(() -> {
            if (overlayPane != null) {
                Scene scene = getCurrentScene();
                if (scene != null) {
                    Node root = scene.getRoot();
                    if (root instanceof Pane pane) {
                        pane.getChildren().remove(overlayPane);
                    }
                }
                overlayPane = null;
            }
        });
    }

    private static Scene getCurrentScene() {
        Node view = MobileApplication.getInstance().getView();
        return (view != null) ? view.getScene() : null;
    }
}
