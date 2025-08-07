package org.axolotlj.remotehealth.desktop.scene;

import static org.axolotlj.remotehealth.core.concurrent.FxThreadUtils.runOnUIThread;

import java.io.IOException;
import java.lang.management.ManagementFactory;

import org.axolotlj.remotehealth.core.AppContext;
import org.axolotlj.remotehealth.core.AppContext.ContextAware;
import org.axolotlj.remotehealth.core.AppContext.DisposableController;
import org.axolotlj.remotehealth.core.javafx.FxmlUtils;
import org.axolotlj.remotehealth.core.logger.DataLogger;
import org.axolotlj.remotehealth.core.logger.Log;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Administrador de escenas de la aplicación (singleton).
 */
public class SceneManager {

    private static SceneManager instance;
    private static Stage stage;

    private static SceneType lastScene;
    private static Object currentController;
    private static DataLogger dataLogger = Log.get();

    private SceneManager(Stage stage) {
        SceneManager.stage = stage;
    }

    public static void initialize(Stage stage) {
        if (instance == null) {
            instance = new SceneManager(stage);
        }
    }

    public static SceneManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("SceneManager no ha sido inicializado.");
        }
        return instance;
    }

    public static void switchTo(SceneType sceneType) {
        try {
            finalizeController();

            FXMLLoader loader = FxmlUtils.loadFXML(sceneType.getFxmlPath());
            loader.setControllerFactory(clazz -> {
                try {
                    Object controller = clazz.getDeclaredConstructor().newInstance();
                    if (controller instanceof ContextAware) {
                        ((ContextAware) controller).setAppContext(AppContext.getInstance());
                    }
                    dataLogger.logInfo("Cargando controlador: " + clazz.getName());
                    return controller;
                } catch (Exception e) {
                    dataLogger.logException("Error cargando el controlador " + clazz.getName(), e);
                    return null;
                }
            });

            Parent root = loader.load();
            SceneManager.currentController = loader.getController();

            Scene scene = new Scene(root);
            runOnUIThread(() -> {
            	stage.setScene(scene);
            	stage.centerOnScreen();
            	stage.setTitle(sceneType.getTitle());
            	stage.getIcons().clear();
            	stage.getIcons().add(sceneType.getImage());            	
            });
            SceneManager.lastScene = sceneType;

            System.gc();
        } catch (IOException e) {
            dataLogger.logException("Error cargando la escena " + sceneType.name(), e);
        } finally {
            ManagementFactory.getMemoryMXBean().gc();
        }
    }

    public static void finalizeController() {
        if (currentController instanceof DisposableController disposable) {
            dataLogger.logDebug("Cerrando " + currentController.getClass().getSimpleName() + "...");
            disposable.dispose();
        }
    }

    public SceneType getLastScene() {
        return lastScene;
    }

    public static Stage getStage() {
        return stage;
    }
}
