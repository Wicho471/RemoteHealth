package org.axolotlj.remotehealth.desktop.controller.scene;

import org.axolotlj.remotehealth.core.AppContext.DisposableController;
import org.axolotlj.remotehealth.core.cmd.CommandCommunicator;
import org.axolotlj.remotehealth.core.cmd.CommandExecutor;
import org.axolotlj.remotehealth.core.cmd.CommandResponseParser;
import org.axolotlj.remotehealth.core.cmd.CommandType;
import org.axolotlj.remotehealth.core.cmd.response.NetworkStatus;
import org.axolotlj.remotehealth.core.cmd.response.PreferencesStatus;
import org.axolotlj.remotehealth.core.cmd.response.SensorStatus;
import org.axolotlj.remotehealth.core.javafx.util.ImageViewUtils;
import org.axolotlj.remotehealth.core.logger.Log;
import org.axolotlj.remotehealth.core.logger.api.DataLogger;
import org.axolotlj.remotehealth.desktop.ui.TextUtils;
import org.axolotlj.remotehealth.desktop.utils.Images;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;

//org.axolotlj.remotehealth.desktop.controller.scene.ConfigEsp32Controller
public class ConfigEsp32Controller implements DisposableController {

	private final DataLogger dataLogger = Log.get();

	private CommandExecutor executor;

	@FXML
	TextField currentSSID, ipv4, ipv6, ssidAp, passwordAp, oximeterBrightness;

	@FXML
	ImageView sta, internet, infrared, oximeter, accelerometer;

	@FXML
	CheckBox apEnabled;

	@FXML
	public void initialize() {
	}

	@FXML
	private void handleAddWifi() {
		
	}

	@FXML
	private void handleRefresh() {
		if (executor == null) {
			dataLogger.logWarn("El ejecutor es nulo");
			return;
		}
		setStatusCharging();
		executor.sendCommandAndWait(CommandType.NETWORK_STATUS, 5000).thenAccept(response -> {
			NetworkStatus networkStatus = CommandResponseParser.parseNetworkStatus(response);
			ImageViewUtils.setImage(sta, networkStatus.STA() ? Images.IMG_ICONS_COMPROBADO: Images.IMG_ICONS_ERROR);
			ImageViewUtils.setImage(internet, networkStatus.internet() ? Images.IMG_ICONS_COMPROBADO: Images.IMG_ICONS_ERROR);
			TextUtils.setText(ipv4, networkStatus.ipv4());
			TextUtils.setText(ipv6, networkStatus.ipv6());
		}).exceptionally(ex -> {
			ImageViewUtils.setImage(sta, Images.IMG_VITALS_ASK);
			ImageViewUtils.setImage(internet, Images.IMG_VITALS_ASK);
			TextUtils.setText(ipv4, "Desconocido");
			TextUtils.setText(ipv6, "Desconocido");
			dataLogger.logWarn("Error o timeout: " + ex.getMessage());
			return null;
		});
		executor.sendCommandAndWait(CommandType.SENSORS_STATUS, 5000).thenAccept(response -> {
			SensorStatus sensorStatus = CommandResponseParser.parseSensorStatus(response);
			ImageViewUtils.setImage(accelerometer, sensorStatus.accelerometer() ? Images.IMG_ICONS_COMPROBADO: Images.IMG_ICONS_ERROR);
			ImageViewUtils.setImage(oximeter, sensorStatus.oximeter() ? Images.IMG_ICONS_COMPROBADO: Images.IMG_ICONS_ERROR);
			ImageViewUtils.setImage(infrared, sensorStatus.infrarred() ? Images.IMG_ICONS_COMPROBADO: Images.IMG_ICONS_ERROR);
		}).exceptionally(ex -> {
			ImageViewUtils.setImage(accelerometer, Images.IMG_VITALS_ASK);
			ImageViewUtils.setImage(oximeter, Images.IMG_VITALS_ASK);
			ImageViewUtils.setImage(infrared, Images.IMG_VITALS_ASK);
			dataLogger.logWarn("Error o timeout: " + ex.getMessage());
			return null;
		});
		executor.sendCommandAndWait(CommandType.PREFERENCES_STATUS, 5000).thenAccept(response -> {
			PreferencesStatus preferencesStatus = CommandResponseParser.parsePreferencesStatus(response);
			TextUtils.setText(ssidAp, preferencesStatus.SSID_AP());
			TextUtils.setText(passwordAp, preferencesStatus.passwordAP());
			TextUtils.setText(oximeterBrightness, preferencesStatus.oximeterBrightnes());
		}).exceptionally(ex -> {
			TextUtils.setText(ssidAp, "Desconocido");
			TextUtils.setText(passwordAp, "Desconocido");
			TextUtils.setText(oximeterBrightness, "Desconocido");
			dataLogger.logWarn("Error o timeout: " + ex.getMessage());
			return null;
		});
	}
	
	private void setStatusCharging() {
		ImageViewUtils.setImage(sta, Images.IMG_ICONS_TRABAJO_EN_PROGRESO);
		ImageViewUtils.setImage(internet, Images.IMG_ICONS_TRABAJO_EN_PROGRESO);
		ImageViewUtils.setImage(oximeter, Images.IMG_ICONS_TRABAJO_EN_PROGRESO);
		ImageViewUtils.setImage(infrared, Images.IMG_ICONS_TRABAJO_EN_PROGRESO);
		ImageViewUtils.setImage(accelerometer, Images.IMG_ICONS_TRABAJO_EN_PROGRESO);
		TextUtils.setText(ipv4, "Cargando...");
		TextUtils.setText(ipv6, "Cargando...");
		TextUtils.setText(ssidAp, "Cargando...");
		TextUtils.setText(passwordAp, "Cargando...");
		TextUtils.setText(oximeterBrightness, "Cargando...");
		apEnabled.setSelected(false);
	}

	@FXML
	private void handleShowQr() {

	}

	@FXML
	private void handleAddConnection() {

	}

	@FXML
	private void handleInitMonitor() {

	}

	@FXML
	private void handleOpenCmd() {

	}

	@FXML
	private void handleApply() {

	}

	@FXML
	private void handleClose() {

	}

	public void setCommandCommunicator(CommandCommunicator communicator) {		
		dataLogger.logDebug("Se seteo el comunicador de comandos");
		this.executor = new CommandExecutor(communicator);
		handleRefresh();
	}

	@Override
	public void dispose() {
		dataLogger.logWarn("Falta implementar la finalizacion");
	}

}
