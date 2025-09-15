package org.axolotlj.remotehealth.desktop.controller.window;

import java.net.URI;
import java.util.Optional;

import org.axolotlj.remotehealth.core.config.files.ConnectionsHandler;
import org.axolotlj.remotehealth.core.model.ConnectionData;
import org.axolotlj.remotehealth.core.validations.GeneralValidation;
import org.axolotlj.remotehealth.desktop.ui.AlertUtil;
import org.axolotlj.remotehealth.desktop.ui.modal.QR;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class DeviceConfigController {

	private ConnectionData connectionData;
	private boolean deleted = false; 
	private Runnable deleteAction;
	private int index;

	@FXML
	private TextField uuidTextField, nameTextField, ipv4TextField, ipv6TextField, pathTextField, portTextField,
			uri4TextField, uri6TextField;

	public void setData(ConnectionData data, int index, Runnable deleteAction) {
		this.connectionData = data;
		this.deleteAction = deleteAction;
		this.index = index;

		uuidTextField.setText(data.getUuid().toString());
		nameTextField.setText(data.getName());
		ipv4TextField.setText(data.getIpV4());
		ipv6TextField.setText(data.getIpV6());
		pathTextField.setText(data.getPath());
		portTextField.setText(String.valueOf(data.getPort()));

		if (data.getUri4() != null) {
			uri4TextField.setText(data.getUri4().toString());
		}
		if (data.getUri6() != null) {
			uri6TextField.setText(data.getUri6().toString());
		}
	}

	@FXML
	private void applyHandle() {
		String name = nameTextField.getText().trim();
		String ipv4 = ipv4TextField.getText().trim();
		String ipv6 = ipv6TextField.getText().trim();
		String path = pathTextField.getText().trim();
		String port = portTextField.getText().trim();

		if (AlertUtil.handleValidation(GeneralValidation.validateName(name))) return;
		if (AlertUtil.handleValidation(GeneralValidation.validatePort(port))) return;
		if (AlertUtil.handleValidation(GeneralValidation.validateIPv4(ipv4))) return;
		if (AlertUtil.handleValidation(GeneralValidation.validateIPv6(ipv6))) return;

		this.connectionData.setIpv4(ipv4);
		this.connectionData.setIpv6(ipv6);
		this.connectionData.setPath(path);
		this.connectionData.setPort(port);
		this.connectionData.setName(name);

		URI uri4 = connectionData.getUri4();
		URI uri6 = connectionData.getUri6();

		uri4TextField.setText(uri4 != null ? uri4.toString() : "");
		uri6TextField.setText(uri6 != null ? uri6.toString() : "");

		if (ConnectionsHandler.update(index, connectionData)) {
			Alert alert = AlertUtil.showInformationAlert("Guardado", null,
					"Los datos se han actualizado correctamente.", false);
			alert.showAndWait();
			closeWindow();
		} else {
			AlertUtil.showErrorAlert("Error", null, "No se pudieron actualizar los datos.");
		}

	}

	@FXML
	private void deleteHandle() {
		Optional<ButtonType> response = AlertUtil.showConfirmationAlert("Confirmación de eliminación",
				"¿Estás seguro de que deseas eliminar este dispositivo?", "Esta acción no se puede deshacer.");

		if (response.isPresent() && response.get() == ButtonType.OK) {
			if (deleteAction != null) {
				deleteAction.run();
			}
			closeWindow();
		}
	}

	@FXML
	private void showQrHandle() {
		QR.showQrHandle(connectionData);
	}

	@FXML
	private void returnHandle() {
		closeWindow();
	}

	private void closeWindow() {
		Stage stage = (Stage) uuidTextField.getScene().getWindow();
		stage.close();
	}

	// Getter para saber si fue eliminado (puedes usar esto en tu ventana principal)
	public boolean isDeleted() {
		return deleted;
	}

	// Getter para obtener el objeto actualizado (en caso lo uses para sobrescribir)
	public ConnectionData getUpdatedData() {
		return connectionData;
	}
}
