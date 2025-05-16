package org.axolotlj.RemoteHealth.app.ui;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.util.Optional;

import org.axolotlj.RemoteHealth.app.Images;

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
        Alert alert = new Alert(AlertType.INFORMATION);
        setIcon(alert, Images.IMG_ICONS_INFO);
        setArgs(alert, title, header, text);
        if(show) alert.show();
        return alert;
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
        Alert alert = new Alert(AlertType.ERROR);
        setIcon(alert, Images.IMG_ICONS_ERROR);
        setArgs(alert, title, header, text);
        alert.showAndWait();
        return alert;
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
        Alert alert = new Alert(AlertType.WARNING);
        setIcon(alert, Images.IMG_ICONS_WARNING);
        setArgs(alert, title, header, text);
        alert.showAndWait();
        return alert;
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
        Alert alert = new Alert(AlertType.CONFIRMATION);
        setIcon(alert, Images.IMG_ICONS_CHOISE);
        setArgs(alert, title, header, text);
        return alert.showAndWait();
    }
    
    private static void setIcon(Alert alert, Image image) {
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(image);
    }
    
    private static void setArgs(Alert alert,String title, String header, String text) {
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(text);
	}
}
