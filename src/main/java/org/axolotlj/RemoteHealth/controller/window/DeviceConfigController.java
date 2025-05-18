package org.axolotlj.RemoteHealth.controller.window;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.imageio.ImageIO;

import org.axolotlj.RemoteHealth.model.ConnectionData;
import org.axolotlj.RemoteHealth.validations.GeneralValidation;

import com.google.gson.Gson;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import org.axolotlj.RemoteHealth.app.ui.AlertUtil;
import org.axolotlj.RemoteHealth.config.files.ConnectionsHandler;

import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
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

		if (GeneralValidation.handleValidation(GeneralValidation.validateName(name))) return;
		if (GeneralValidation.handleValidation(GeneralValidation.validatePort(port))) return;
		if (GeneralValidation.handleValidation(GeneralValidation.validateIPv4(ipv4))) return;
		if (GeneralValidation.handleValidation(GeneralValidation.validateIPv6(ipv6))) return;

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

		// 1️⃣ Crear el objeto JSON
		Map<String, Object> jsonMap = new HashMap<>();
		jsonMap.put("ipv4", connectionData.getIpV4());
		jsonMap.put("ipv6", connectionData.getIpV6());
		jsonMap.put("path", connectionData.getPath());
		jsonMap.put("port", connectionData.getPort());
		jsonMap.put("name", connectionData.getName());

		Gson gson = new Gson();
		String jsonString = gson.toJson(jsonMap);

		try {
			// 2️⃣ Generar el QR
			int size = 500;
			QRCodeWriter qrCodeWriter = new QRCodeWriter();

			Map<EncodeHintType, Object> hints = new HashMap<>();
			hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H); // Máxima corrección

			BitMatrix bitMatrix = qrCodeWriter.encode(jsonString, BarcodeFormat.QR_CODE, size, size, hints);

			WritableImage qrImage = new WritableImage(size, size);
			for (int x = 0; x < size; x++) {
				for (int y = 0; y < size; y++) {
					qrImage.getPixelWriter().setColor(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
				}
			}

			// 3️⃣ Mostrar en ventana emergente
			ImageView qrView = new ImageView(qrImage);
			qrView.setFitWidth(size);
			qrView.setFitHeight(size);
			qrView.setPreserveRatio(true);

			// Botón para guardar
			Button saveButton = new Button("Guardar QR");
			saveButton.setOnAction(e -> {
				FileChooser fileChooser = new FileChooser();
				fileChooser.setTitle("Guardar Código QR");
				fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Imagen PNG", "*.png"));
				fileChooser.setInitialFileName("Conexion_QR_" + connectionData.getName() + ".png");
				File file = fileChooser.showSaveDialog(qrView.getScene().getWindow());
				if (file != null) {
					try {
						ImageIO.write(SwingFXUtils.fromFXImage(qrImage, null), "png", file);
						AlertUtil.showInformationAlert("Guardado", null, "Código QR guardado correctamente.", false);
					} catch (IOException ex) {
						System.err.println("Error al guardar la imagen: " + ex.getMessage());
						ex.printStackTrace();
						AlertUtil.showErrorAlert("Error", "No se pudo guardar el QR", ex.getMessage());
					}
				}
			});

			VBox vbox = new VBox(15, qrView, saveButton);
			vbox.setAlignment(Pos.CENTER);
			vbox.setPadding(new Insets(20));

			Stage qrStage = new Stage();
			qrStage.setTitle("Código QR - " + connectionData.getName());
			qrStage.setScene(new Scene(vbox));
			qrStage.initModality(Modality.APPLICATION_MODAL);
			qrStage.setResizable(false);
			qrStage.showAndWait();

		} catch (WriterException e) {
			System.err.println("Error al generar el QR: " + e.getMessage());
			e.printStackTrace();
			AlertUtil.showErrorAlert("Error", "No se pudo generar el código QR", e.getMessage());
		}
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
