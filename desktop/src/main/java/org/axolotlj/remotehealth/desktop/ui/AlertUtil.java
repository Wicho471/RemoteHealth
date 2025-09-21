package org.axolotlj.remotehealth.desktop.ui;

import static org.axolotlj.remotehealth.desktop.javafx.current.FxThreadUtils.runOnUIThread;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import org.axolotlj.remotehealth.core.validations.ValidationResult;
import org.axolotlj.remotehealth.desktop.ui.assets.Images;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * Utilidad para mostrar diferentes tipos de alertas.
 */
public class AlertUtil {

	/**
	 * Muestra una alerta de tipo información.
	 * 
	 * @param title  título de la ventana
	 * @param header encabezado del mensaje
	 * @param text   contenido principal
	 * @return instancia de la alerta mostrada
	 */
	public static Alert showInformationAlert(String title, String header, String text, boolean show) {
		AtomicReference<Alert> ref = new AtomicReference<>();

		runOnUIThread(() -> {
			Alert alert = new Alert(AlertType.INFORMATION);
			setIcon(alert, Images.IMG_ICONS_INFO);
			setArgs(alert, title, header, text);
			if (show) alert.show();
			ref.set(alert);
		});
		return ref.get();
	}

	/**
	 * Muestra una alerta de error y espera una confirmación.
	 * 
	 * @param title  título de la ventana
	 * @param header encabezado del mensaje
	 * @param text   contenido principal
	 * @return instancia de la alerta mostrada
	 */
	public static Alert showErrorAlert(String title, String header, String text) {
		AtomicReference<Alert> ref = new AtomicReference<>();

		runOnUIThread(() -> {
			Alert alert = new Alert(AlertType.ERROR);
			setIcon(alert, Images.IMG_ICONS_ERROR);
			setArgs(alert, title, header, text);
			alert.showAndWait();
			ref.set(alert);
		});
		return ref.get();
	}

	/**
	 * Muestra una alerta de advertencia.
	 * 
	 * @param title  título de la ventana
	 * @param header encabezado del mensaje
	 * @param text   contenido principal
	 * @return instancia de la alerta mostrada
	 */
	public static Alert showWarningAlert(String title, String header, String text) {
		AtomicReference<Alert> ref = new AtomicReference<>();

		runOnUIThread(() -> {
			Alert alert = new Alert(AlertType.WARNING);
			setIcon(alert, Images.IMG_ICONS_WARNING);
			setArgs(alert, title, header, text);
			alert.showAndWait();
			ref.set(alert);
		});
		return ref.get();
	}

	/**
	 * Muestra una alerta de confirmación y espera la respuesta del usuario.
	 * 
	 * @param title  título de la ventana
	 * @param header encabezado del mensaje
	 * @param text   contenido principal
	 * @return botón seleccionado por el usuario
	 */
	public static Optional<ButtonType> showConfirmationAlert(String title, String header, String text) {
		AtomicReference<Optional<ButtonType>> ref = new AtomicReference<>();
		runOnUIThread(() -> {
			Alert alert = new Alert(AlertType.CONFIRMATION);
			setIcon(alert, Images.IMG_ICONS_CHOISE);
			setArgs(alert, title, header, text);
			ref.set(alert.showAndWait());
		});
		return ref.get();
	}

	/**
	 * Maneja el resultado de una validación mostrando una alerta si el valor es
	 * inválido.
	 * 
	 * Si el resultado no es válido, se muestra una alerta de advertencia con el
	 * tipo y mensaje de error proporcionado en el objeto {@link ValidationResult}.
	 *
	 * @param result El resultado de la validación a evaluar.
	 * @return true si la validación falló y se mostró una alerta; false si la
	 *         validación fue exitosa.
	 */
	public static boolean handleValidation(ValidationResult result) {
		if (!result.isValid()) {
			showWarningAlert("Campo invalido", result.getErrorType().getDesc(), result.getMessage());
			return true;
		}
		return false;
	}

	private static void setIcon(Alert alert, Image image) {
		Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
		stage.getIcons().add(image);
	}

	private static void setArgs(Alert alert, String title, String header, String text) {
		alert.setTitle(title);
		alert.setHeaderText(header);
		alert.setContentText(text);
	}

	public static void buildingModule() {
		showInformationAlert("Modulo en contruccion", "Verifica la version mas reciente",
				"Este modulo aun se encuenta en desarollo", true);
	}
}
