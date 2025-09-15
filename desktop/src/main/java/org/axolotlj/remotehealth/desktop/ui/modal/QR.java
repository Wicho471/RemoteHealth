package org.axolotlj.remotehealth.desktop.ui.modal;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import org.axolotlj.remotehealth.core.model.ConnectionData;
import org.axolotlj.remotehealth.desktop.ui.AlertUtil;

import com.google.gson.Gson;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class QR {

	public static void showQrHandle(ConnectionData connectionData) {
		showQrHandle(connectionData.getIpV4(), connectionData.getIpV6(), connectionData.getPath(), connectionData.getPort(), connectionData.getName());
	} 
	
	public static void showQrHandle(String ipv4, String ipv6, String path, int port, String name) {

		// 1️⃣ Crear el objeto JSON
		Map<String, Object> jsonMap = new HashMap<>();
		jsonMap.put("ipv4", ipv4);
		jsonMap.put("ipv6", ipv6);
		jsonMap.put("path", path);
		jsonMap.put("port", port);
		jsonMap.put("name", name);

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
				fileChooser.setInitialFileName("Conexion_QR_" + name + ".png");
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
			qrStage.setTitle("Código QR - " + name);
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
}
