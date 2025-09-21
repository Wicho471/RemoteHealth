package org.axolotlj.remotehealth.desktop.ui;

import java.io.IOException;
import java.util.function.Consumer;

import org.axolotlj.remotehealth.core.AppContext;
import org.axolotlj.remotehealth.core.AppContext.ContextAware;
import org.axolotlj.remotehealth.core.logger.Log;
import org.axolotlj.remotehealth.desktop.javafx.FxmlUtils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class ModalUtils {
	/**
	 * Abre una nueva ventana modal a partir de un archivo FXML.
	 *
	 * @param fxmlPath         Ruta del archivo FXML
	 * @param title            Título de la ventana
	 * @param parentController Controlador padre a inyectar si es posible (puede ser
	 *                         null)
	 * @param errorHandler     Consumidor opcional para manejar errores
	 */
	public static <T> T openModalWindow(String fxmlPath, String title, Object parentController, Image image) {
		try {
			FXMLLoader loader = FxmlUtils.loadFXML(fxmlPath);
			Parent root = loader.load();

			T controller = loader.getController();
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
					Log.get().logException("Error al inyectar el controlador padre", e);
				}
			}

			showModal(title, root, image);
			return controller;
		} catch (IOException e) {
			Log.get().logException("Error al cargar el FXML", e);
			return null;
		}
	}

	public static <T> void openModalWindow(String fxmlPath, String title, Object parentController, Image image,
			Consumer<T> controllerConfigurer) {

		try {
			FXMLLoader loader = FxmlUtils.loadFXML(fxmlPath);
			Parent root = loader.load();

			T controller = loader.getController();
			if (controller instanceof ContextAware contextAware) {
				contextAware.setAppContext(AppContext.getInstance());
			}

			if (parentController != null && controller != null) {
				try {
					var method = controller.getClass().getMethod("setParentController", parentController.getClass());
					method.invoke(controller, parentController);
				} catch (NoSuchMethodException ignored) {
				} catch (Exception e) {
					Log.get().logException("Error al inyectar el controlador padre", e);
				}
			}

			if (controllerConfigurer != null && controller != null) {
				controllerConfigurer.accept(controller);
			}

			showModal(title, root, image);
		} catch (IOException e) {
			Log.get().logException("Error al cargar el FXML", e);
		}
	}

	public static void showModal(String title, Parent root, Image image) {
		Stage stage = new Stage();
		stage.setTitle(title);
		stage.getIcons().add(image);
		stage.setScene(new Scene(root));
		stage.initModality(Modality.APPLICATION_MODAL);
		stage.setResizable(false);
		stage.showAndWait();
	}
}
