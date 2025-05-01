package org.axolotlj.RemoteHealth.app.ui;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;

import java.util.Optional;

/**
 * Utilidad para mostrar diferentes tipos de alertas.
 */
public class Alerts {

    /**
     * Muestra una alerta de tipo información.
     * 
     * @param title  título de la ventana
     * @param header encabezado del mensaje
     * @param text   contenido principal
     * @return instancia de la alerta mostrada
     */
    public static Alert showInformationAlert(String title, String header, String text) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(text);
        alert.show();
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
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(text);
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
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(text);
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
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(text);
        return alert.showAndWait();
    }
}
