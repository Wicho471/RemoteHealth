package org.axolotlj.remotehealth.mobile.ui;

import static org.axolotlj.remotehealth.core.concurrent.FxThreadUtils.runOnUIThread;

import java.util.concurrent.atomic.AtomicReference;

import org.axolotlj.remotehealth.core.validations.ValidationResult;

import com.gluonhq.charm.glisten.control.Alert;
import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;

import javafx.scene.Node;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;

/**
 * Utilidad para mostrar diferentes tipos de alertas en Gluon Mobile.
 */
public class MobileAlertUtil {

    /**
     * Muestra una alerta de tipo información.
     *
     * @param title   título del mensaje
     * @param message contenido principal
     * @return instancia de la alerta mostrada
     */
    public static Alert showInformationAlert(String title, String message) {
        AtomicReference<Alert> ref = new AtomicReference<>();
        runOnUIThread(() -> {
            Alert alert = createAlert(AlertType.INFORMATION, title, message);
            alert.showAndWait();
            ref.set(alert);
        });
        return ref.get();
    }

    /**
     * Muestra una alerta de error.
     *
     * @param title   título del mensaje
     * @param message contenido principal
     * @return instancia de la alerta mostrada
     */
	public static Alert showErrorAlert(String title, String message) {
        AtomicReference<Alert> ref = new AtomicReference<>();
        runOnUIThread(() -> {
            Alert alert = createAlert(AlertType.ERROR, title, message);
            alert.showAndWait();
            ref.set(alert);
        });
        return ref.get();
    }

    /**
     * Muestra una alerta de advertencia.
     *
     * @param title   título del mensaje
     * @param message contenido principal
     * @return instancia de la alerta mostrada
     */
    public static Alert showWarningAlert(String title, String message) {
        AtomicReference<Alert> ref = new AtomicReference<>();
        runOnUIThread(() -> {
            Alert alert = createAlert(AlertType.WARNING, title, message);
            alert.showAndWait();
            ref.set(alert);
        });
        return ref.get();
    }

    /**
     * Muestra una alerta de confirmación y devuelve si el usuario aceptó.
     *
     * @param title   título del mensaje
     * @param message contenido principal
     * @return true si el usuario seleccionó OK, false si CANCEL
     */
    public static boolean showConfirmationAlert(String title, String message) {
        AtomicReference<Boolean> accepted = new AtomicReference<>(false);
        runOnUIThread(() -> {
            Alert alert = new Alert(AlertType.CONFIRMATION);
            alert.setContentText(message);
            alert.setGraphic(buildGraphic(AlertType.CONFIRMATION, title));

            AtomicReference<ButtonType> selected = new AtomicReference<>();

            ButtonType okType = new ButtonType("Aceptar");
            ButtonType cancelType = ButtonType.CANCEL;

            Button okButton = new Button("Aceptar");
            okButton.setOnAction(e -> {
                selected.set(okType);
                alert.hide();
            });

            Button cancelButton = new Button("Cancelar");
            cancelButton.setOnAction(e -> {
                selected.set(cancelType);
                alert.hide();
            });

            alert.getButtons().setAll(okButton, cancelButton);
            alert.showAndWait();

            accepted.set(ButtonType.OK.equals(selected.get()));
        });
        return accepted.get();
    }

    /**
     * Muestra una alerta informando que un módulo está en construcción.
     */
    public static void buildingModule() {
        showInformationAlert("Módulo en construcción", "Este módulo aún se encuentra en desarrollo.\nVerifica la versión más reciente.");
    }

    /**
     * Maneja el resultado de una validación mostrando una alerta si el valor es inválido.
     *
     * @param result El resultado de la validación a evaluar.
     * @return true si la validación falló y se mostró una alerta; false si fue exitosa.
     */
    public static boolean handleValidation(ValidationResult result) {
        if (!result.isValid()) {
            showWarningAlert(result.getErrorType().getDesc(), result.getMessage());
            return true;
        }
        return false;
    }

    private static Alert createAlert(AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setContentText(message);
        alert.setGraphic(buildGraphic(type, title));
        return alert;
    }

    private static Node buildGraphic(AlertType type, String title) {
        Node icon;
        switch (type) {
            case ERROR:
                icon = MaterialDesignIcon.ERROR.graphic();
                break;
            case WARNING:
                icon = MaterialDesignIcon.WARNING.graphic();
                break;
            case CONFIRMATION:
                icon = MaterialDesignIcon.QUESTION_ANSWER.graphic();
                break;
            case INFORMATION:
            default:
                icon = MaterialDesignIcon.INFO.graphic();
                break;
        }

        HBox container = new HBox(10);
        Text titleText = new Text(title);
        container.getChildren().addAll(icon, titleText);
        return container;
    }
}
