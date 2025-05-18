package org.axolotlj.RemoteHealth.controller.scene;

import java.io.IOException;

import org.axolotlj.RemoteHealth.app.SceneType;
import org.axolotlj.RemoteHealth.app.ui.AlertUtil;
import org.axolotlj.RemoteHealth.app.ui.FileChooserUtils;
import org.axolotlj.RemoteHealth.config.files.ConnectionsHandler;
import org.axolotlj.RemoteHealth.core.AppContext;
import org.axolotlj.RemoteHealth.core.AppContext.ContextAware;
import org.axolotlj.RemoteHealth.core.AppContext.DisposableController;
import org.axolotlj.RemoteHealth.service.QRScanner;

import com.google.gson.JsonObject;
import com.google.zxing.NotFoundException;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;

public class DeviceSetupController implements ContextAware, DisposableController {

	private AppContext appContext;
	private QRScanner qrScanner;

	@FXML
	private Button backBtn, qrScanBtn, addManualDeviceBtn, turnOnCameraBtn;
	@FXML
	private TextField nameTextField, ipv4TextField, ipv6TextField, portTextField, pathTextField;
	@FXML
	private ImageView cameraView;

	@FXML
	public void initialize() {
		Platform.runLater(() -> {
			appContext.getSceneManager().getStage().setResizable(false);
		});
	}

	@FXML
	private void handleBackButton() {
		if (qrScanner != null) {
			qrScanner.stop();
		}
		Platform.runLater(() -> {
			appContext.getSceneManager().getStage().setResizable(true);
		});
		appContext.getSceneManager().switchTo(SceneType.DEVICE_SELECTOR);
	}

	@FXML
	private void handleQrScan() {
		FileChooserUtils
				.chooseFile(appContext.getSceneManager().getStage(), "Seleccionar código QR", "Imágenes PNG", "*.png")
				.ifPresent(file -> {
					try {
						String qrContent = QRScanner.decodeQRCode(file.getAbsolutePath());
						if (ConnectionsHandler.addConnetcionData(qrContent)) {
							AlertUtil.showInformationAlert("Éxito", null, "Se añadió correctamente el dispositivo",
									true);
						} else {
							AlertUtil.showErrorAlert("Error", "No se pudo añadir el dispositivo",
									"Verifica el código QR");
						}
					} catch (NotFoundException | IOException e) {
						System.err.println("Archivo no encontrado -> " + e.getMessage());
					} catch (Exception e) {
						System.err.println("Ocurrió un error inesperado -> " + e.getMessage());
					}
				});
	}

	@FXML
	private void handleAddManualDevice() {
		try {
			String name = nameTextField.getText().trim().isEmpty() ? "Mi dispositivo" : nameTextField.getText();
			String ipv4 = ipv4TextField.getText().trim();
			String ipv6 = ipv6TextField.getText().trim();
			String portStr = portTextField.getText().trim();
			String path = pathTextField.getText().trim();

			if (ipv4.isEmpty() || ipv6.isEmpty() || portStr.isEmpty()) {
				AlertUtil.showErrorAlert("Campos invalidos", "Algunos datos estan vacios",
						"Todos los campos excepto la ruta y el nombre son obligatorios");
				return;
			}

			int port = Integer.parseInt(portStr);
			if (port < 1 || port > 65535) {
				AlertUtil.showErrorAlert("Campos invalidos", "Revisa que el campo puerto",
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
				AlertUtil.showInformationAlert("Exito", null, "Se añadio correctamente el dispositivo", true);
			} else {
				AlertUtil.showErrorAlert("Error", "No se pudo añadir el dispositivo", "Verifica el codigo QR");
			}

		} catch (NumberFormatException e) {
			System.err.println("ConnectionFormHandler::processConnectionData - Puerto inválido: " + e.getMessage());
			AlertUtil.showErrorAlert("Campos invalidos", "Revisa que el campo puerto",
					"El campo de puerto debe contener un número válido.");
		} catch (Exception e) {
			System.err.println("ConnectionFormHandler::processConnectionData - Error inesperado: " + e.getMessage());
			AlertUtil.showErrorAlert("Campos invalidos", null, "Ocurrió un error al procesar los datos");
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
						AlertUtil.showInformationAlert("Exito", null, "Se añadio correctamente el dispositivo", true);

					} else {
						AlertUtil.showErrorAlert("Error", "No se pudo añadir el dispositivo", "Verifica el codigo QR");
					}
				}
			});
		} catch (Exception e) {
			System.err.println("DeviceSetupController::onQRCodeDetected ->" + e.getMessage());
		}
	}

	@Override
	public void dispose() {
		if (qrScanner != null) {
			qrScanner.stop(); 
			qrScanner = null;
		}
		cameraView.setImage(null); 
	}

}
