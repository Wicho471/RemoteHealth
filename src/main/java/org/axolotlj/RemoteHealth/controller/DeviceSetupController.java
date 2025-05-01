package org.axolotlj.RemoteHealth.controller;

import java.io.File;
import java.io.IOException;

import org.axolotlj.RemoteHealth.app.SceneManager.SceneType;
import org.axolotlj.RemoteHealth.app.ui.Alerts;
import org.axolotlj.RemoteHealth.config.files.ConnectionsHandler;
import org.axolotlj.RemoteHealth.core.AppContext;
import org.axolotlj.RemoteHealth.core.AppContext.ContextAware;
import org.axolotlj.RemoteHealth.service.QRScanner;

import com.google.gson.JsonObject;
import com.google.zxing.NotFoundException;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;

public class DeviceSetupController implements ContextAware {

	private AppContext appContext;
	private QRScanner qrScanner;

	@FXML
	private Button backBtn;

	@FXML
	private Button qrScanBtn;

	@FXML
	private TextField nameTextField;

	@FXML
	private TextField ipv4TextField;

	@FXML
	private TextField ipv6TextField;

	@FXML
	private TextField portTextField;

	@FXML
	private TextField pathTextField;

	@FXML
	private Button addManualDeviceBtn;

	@FXML
	private Button turnOnCameraBtn;

	@FXML
	private ImageView cameraView;

	@FXML
	public void initialize() {

	}

	@FXML
	private void handleBackButton() {
		if (qrScanner != null) {
			qrScanner.stop();
		}

		appContext.getSceneManager().switchTo(SceneType.DEVICE_SELECTOR);
	}

	@FXML
	private void handleQrScan() {
		FileChooser fileChooser = new FileChooser();
		fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Imágenes PNG", "*.png"));
		File file = fileChooser.showOpenDialog(appContext.getSceneManager().getStage());
		if (file != null) {
			try {
				String qrContent = QRScanner.decodeQRCode(file.getAbsolutePath());
				System.out.println(qrContent);
				if (ConnectionsHandler.addConnetcionData(qrContent)) {
					Alerts.showInformationAlert("Exito", null, "Se añadio correctamente el dispositivo");
				} else {
					Alerts.showErrorAlert("Error", "No se pudo añadir el dispositivo", "Verifica el codigo QR");
				}

			} catch (NotFoundException | IOException e) {
				System.err.println("Archivo no encontrado -> " + e.getMessage());
			} catch (Exception e) {
				System.err.println("Ocurrio un error inesperado -> " + e.getMessage());
			}
		}
	}

	@FXML
	private void handleAddManualDevice() {
		try {
			String name = nameTextField.getText().trim().isEmpty() ? "Mi dispositivo" : nameTextField.getText();
			String ipv4 = ipv4TextField.getText().trim();
			String ipv6 = ipv6TextField.getText().trim();
			String portStr = portTextField.getText().trim();
			String path = pathTextField.getText().trim().isEmpty() ? "/" : pathTextField.getText().trim();

			if (ipv4.isEmpty() || ipv6.isEmpty() || portStr.isEmpty()) {
				Alerts.showErrorAlert("Campos invalidos", "Algunos datos estan vacios",
						"Todos los campos excepto la ruta y el nombre son obligatorios");
				return;
			}

			int port = Integer.parseInt(portStr);
			if (port < 1 || port > 65535) {
				Alerts.showErrorAlert("Campos invalidos", "Revisa que el campo puerto",
						"El puerto debe estar entre 1 y 65535");
				return;
			}

			JsonObject json = new JsonObject();
			json.addProperty("name", name);
			json.addProperty("ipv4", ipv4);
			json.addProperty("ipv6", ipv6);
			json.addProperty("port", port);
			json.addProperty("path", path);

			if (ConnectionsHandler.addConnetcionData(json.toString())) {
				Alerts.showInformationAlert("Exito", null, "Se añadio correctamente el dispositivo");
			} else {
				Alerts.showErrorAlert("Error", "No se pudo añadir el dispositivo", "Verifica el codigo QR");
			}

		} catch (NumberFormatException e) {
			System.err.println("ConnectionFormHandler::processConnectionData - Puerto inválido: " + e.getMessage());
			Alerts.showErrorAlert("Campos invalidos", "Revisa que el campo puerto",
					"El campo de puerto debe contener un número válido.");
		} catch (Exception e) {
			System.err.println("ConnectionFormHandler::processConnectionData - Error inesperado: " + e.getMessage());
			Alerts.showErrorAlert("Campos invalidos", null, "Ocurrió un error al procesar los datos");
		}
	}

	@FXML
	private void handleTurnOnCamera() {
		turnOnCameraBtn.setText("");
		qrScanner = new QRScanner(cameraView, this::onQRCodeDetected);
		qrScanner.start();
	}

	@Override
	public void setAppContext(AppContext context) {
		this.appContext = context;
	}

	private void onQRCodeDetected(String qrContent) {
		try {
			if (qrScanner != null) {
				qrScanner.stop();
				qrScanner = null;
			}
			Platform.runLater(() -> {
				cameraView.setImage(null);
				turnOnCameraBtn.setText("Encender camara");
				if (qrContent != null) {
					if (ConnectionsHandler.addConnetcionData(qrContent)) {
						Alerts.showInformationAlert("Exito", null, "Se añadio correctamente el dispositivo");

					} else {
						Alerts.showErrorAlert("Error", "No se pudo añadir el dispositivo", "Verifica el codigo QR");
					}
				}
			});
		} catch (Exception e) {
			System.err.println("DeviceSetupController::onQRCodeDetected ->" + e.getMessage());
		}
	}

}
