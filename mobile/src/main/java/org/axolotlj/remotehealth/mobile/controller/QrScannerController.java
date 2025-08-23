package org.axolotlj.remotehealth.mobile.controller;

import static org.axolotlj.remotehealth.core.concurrent.FxThreadUtils.runOnUIThread;

import java.io.File;
import java.util.Optional;

import org.axolotlj.remotehealth.core.config.files.ConnectionsHandler;
import org.axolotlj.remotehealth.core.logger.DataLogger;
import org.axolotlj.remotehealth.core.logger.Log;
import org.axolotlj.remotehealth.mobile.navigation.ViewManager;
import org.axolotlj.remotehealth.mobile.qr.MobileQRDecorer;
import org.axolotlj.remotehealth.mobile.ui.MobileAlertUtil;

import com.gluonhq.attach.barcodescan.BarcodeScanService;
import com.gluonhq.attach.pictures.PicturesService;
import com.gluonhq.attach.util.Services;
import com.gluonhq.charm.glisten.mvc.View;

import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;

public class QrScannerController {

	private final DataLogger dataLogger = Log.get();

	@FXML
	private View mainView;

	@FXML
	private Pane scannerFrame;

	@FXML
	private Label resultLabel;

	@FXML
	public void initialize() {
		dataLogger.logDebug("Iniciando controlador 'QrScannerController'");
		mainView.setOnShowing(event -> startCameraScan());
	}

	private void startCameraScan() {
		dataLogger.logDebug("Intentando iniciar escaneo desde cámara");

		Optional<BarcodeScanService> barcodeService = Services.get(BarcodeScanService.class);

		if (barcodeService.isEmpty()) {
			dataLogger.logWarn("BarcodeScanService no disponible");
			MobileAlertUtil.showWarningAlert("Advertencia", "BarcodeScanService no disponible");
			ViewManager.showHomeView();
			return;
		}

		BarcodeScanService service = barcodeService.get();
		ChangeListener<String> listener = (obs, oldV, newV) -> {
			runOnUIThread(() -> handleScanResult(newV));
		};

		service.resultProperty().addListener(listener);
		service.asyncScan("Escaneo QR", "Alinea el código dentro del marco", "Resultado:");
	}

	private void handleScanResult(String qrContent) {
		if (qrContent == null || qrContent.isBlank()) {
			dataLogger.logWarn("No se obtuvo contenido del QR");
			MobileAlertUtil.showErrorAlert("Error", "QR no válido");
			return;
		}

		dataLogger.logDebug("QR detectado: " + qrContent);

		if (ConnectionsHandler.addConnetcionData(qrContent)) {
			MobileAlertUtil.showInformationAlert("Éxito", "Se añadió exitosamente la conexión");
			ViewManager.showHomeView();
		} else {
			MobileAlertUtil.showErrorAlert("Error", "QR no válido");
		}

		resultLabel.setText(qrContent);
	}

	private void selectImage() {
		dataLogger.logDebug("Intentando seleccionar imagen desde galería");

		Services.get(PicturesService.class).ifPresentOrElse(picturesService -> {
			runOnUIThread(() -> {
				Optional<Image> imageOpt = picturesService.loadImageFromGallery();

				if (imageOpt.isEmpty()) {
					dataLogger.logWarn("El usuario no seleccionó ninguna imagen desde la galería.");
					return;
				}

				Image image = imageOpt.get();
				String qrContent = MobileQRDecorer.decodeQRCode(image);
				if (qrContent != null) {
					MobileAlertUtil.showErrorAlert("Error", "QR no valido");
					return;
				}
				dataLogger.logDebug(qrContent);
				if (ConnectionsHandler.addConnetcionData(qrContent)) {
					MobileAlertUtil.showInformationAlert("Exito", "Se añadio de exitosamente la conexion");
					ViewManager.showHomeView();
				} else {
					MobileAlertUtil.showErrorAlert("Error", "QR no valido");
				}

				Optional<File> fileOpt = picturesService.getImageFile();
				if (fileOpt.isEmpty() || !fileOpt.get().exists()) {
					dataLogger.logWarn("No se obtuvo el archivo correspondiente a la imagen seleccionada.");
					return;
				}

				File pictureFile = fileOpt.get();
				dataLogger.logInfo("Imagen obtenida en la ruta '" + pictureFile.getAbsolutePath() + "'");

			});
		}, () -> {
			dataLogger.logWarn("PicturesService no disponible");
			MobileAlertUtil.showWarningAlert("Advertencia", "PicturesService no disponible");
			ViewManager.showHomeView();
		});
	}

}
