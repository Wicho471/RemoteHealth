package org.axolotlj.remotehealth.desktop.controller.window;

import static org.axolotlj.remotehealth.core.javafx.current.FxThreadUtils.runOnUIThread;

import java.util.List;

import org.axolotlj.remotehealth.core.AppContext.DisposableController;
import org.axolotlj.remotehealth.core.javafx.current.AsyncExecutor;
import org.axolotlj.remotehealth.core.logger.Log;
import org.axolotlj.remotehealth.core.logger.api.DataLogger;
import org.axolotlj.remotehealth.core.service.bluetooth.BluetoothDeviceInfo;
import org.axolotlj.remotehealth.core.service.bluetooth.BluetoothService;
import org.axolotlj.remotehealth.desktop.controller.scene.ConfigEsp32Controller;
import org.axolotlj.remotehealth.desktop.scene.SceneManager;
import org.axolotlj.remotehealth.desktop.scene.SceneType;
import org.axolotlj.remotehealth.desktop.service.bluetooth.BluetoothServiceFactory;
import org.axolotlj.remotehealth.desktop.ui.AlertUtil;
import org.axolotlj.remotehealth.desktop.ui.TableUtils;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;

/**
 * Controlador para la ventana que muestra la lista de dispositivos Bluetooth
 * disponibles.
 */
public class BluetoothListController implements DisposableController {

	private final DataLogger dataLogger = Log.get();

	private static final BluetoothService scanner = BluetoothServiceFactory.getBluetoothService();

	@FXML
	private TableView<BluetoothDeviceInfo> bluetoothTable;
	@FXML
	private VBox charging;
	@FXML
	private StackPane stackPane;

	private final ObservableList<BluetoothDeviceInfo> deviceList = FXCollections.observableArrayList();

	@SuppressWarnings("unchecked")
	@FXML
	public void initialize() {
		showTable(false);

		TableColumn<BluetoothDeviceInfo, String> nameColumn = new TableColumn<>("Nombre");
		nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

		TableColumn<BluetoothDeviceInfo, String> addressColumn = new TableColumn<>("Dirección");
		addressColumn.setCellValueFactory(new PropertyValueFactory<>("address"));

		TableColumn<BluetoothDeviceInfo, Void> actionColumn = new TableColumn<>("Acción");
		actionColumn.setCellFactory(createActionCellFactory());

		bluetoothTable.getColumns().addAll(nameColumn, addressColumn, actionColumn);
		bluetoothTable.setItems(deviceList);

		TableUtils.adjustColumns(bluetoothTable, false);

		loadDevices();
	}

	private void showTable(boolean show) {
		runOnUIThread(() -> {
			bluetoothTable.setVisible(show);
			bluetoothTable.setManaged(show);

			charging.setVisible(!show);
			charging.setManaged(!show);
		});

	}

	private Callback<TableColumn<BluetoothDeviceInfo, Void>, javafx.scene.control.TableCell<BluetoothDeviceInfo, Void>> createActionCellFactory() {
		return column -> new javafx.scene.control.TableCell<>() {
			private final Button connectButton = new Button("Conectar");

			{
				connectButton.setOnAction(event -> {
					BluetoothDeviceInfo device = getTableView().getItems().get(getIndex());
					handleConnect(device);
				});
			}

			@Override
			protected void updateItem(Void item, boolean empty) {
				super.updateItem(item, empty);
				if (empty) {
					setGraphic(null);
				} else {
					setGraphic(connectButton);
				}
			}
		};
	}

	private void loadDevices() {
		showTable(false);
		AsyncExecutor.runAsyncTask("BluetoothDeviceCheker", () -> {
			List<BluetoothDeviceInfo> devices = scanner.getDevices();
			dataLogger.logDebug("Dispositivos detectados " + devices.size());
			return devices;
		}, result -> {
			deviceList.setAll(result);
			showTable(true);
		}, ex -> {
			dataLogger.logException("Error tratando de buscar dipositivos bluetooth", ex);
		});

	}

	private void handleConnect(BluetoothDeviceInfo device) {
		if (device == null) {
			System.out.println("No se seleccionó ningún dispositivo");
			return;
		}
		scanner.establishConnection(device, () -> {
			SceneManager.switchTo(SceneType.CONFIG_ESP32);
			ConfigEsp32Controller configEsp32Controller = (ConfigEsp32Controller) SceneManager.currentController;
			configEsp32Controller.setCommandCommunicator(scanner);
			Stage stage = (Stage) bluetoothTable.getScene().getWindow();
			stage.close();
		}, () -> {
			AlertUtil.showErrorAlert("Error", "No se pudo conectar al dispositivo",
					"Verifica el dispositivo y reintenta conecar");
		});
	}

	@FXML
	private void handleRefresh() {
		loadDevices();
	}

	@FXML
	private void handleClose() {
		dispose();
	}

	@Override
	public void dispose() {

	}
}
