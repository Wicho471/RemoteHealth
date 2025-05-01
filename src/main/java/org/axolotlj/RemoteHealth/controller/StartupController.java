package org.axolotlj.RemoteHealth.controller;

import java.net.URI;
import java.util.ArrayList;
import java.util.Optional;
import java.util.function.Supplier;

import org.axolotlj.RemoteHealth.app.SceneManager.SceneType;
import org.axolotlj.RemoteHealth.app.ui.Alerts;
import org.axolotlj.RemoteHealth.app.ui.TableUtils;
import org.axolotlj.RemoteHealth.config.files.ConnectionsHandler;
import org.axolotlj.RemoteHealth.core.AppContext;
import org.axolotlj.RemoteHealth.core.AppContext.ContextAware;
import org.axolotlj.RemoteHealth.model.ConnectionData;
import org.axolotlj.RemoteHealth.service.DataProcessor;
import org.axolotlj.RemoteHealth.service.logger.DataLogger;
import org.axolotlj.RemoteHealth.service.websocket.WebSocketManager;
import org.axolotlj.RemoteHealth.util.NetworkUtil;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.util.Callback;

public class StartupController implements ContextAware {
	private DataLogger dataLogger;
	private Alert connectingAlert;
	private AppContext appContext;
	private ArrayList<ConnectionData> data;
	private WebSocketManager wsManager;

	@FXML
	private TableView<ConnectionData> deviceTable;

	@FXML
	private ImageView imgDriverStatus;

	@FXML
	private ImageView imgLANStatus;

	@FXML
	private ImageView imgInternetStatus;

	@FXML
	private ImageView imgIpv6Status;

	@FXML
	private Button refreshStatusBtn;

	@FXML
	private Button refreshDevicesBtn;

	@FXML
	private Button addDeviceBtn;

	@FXML
	private MenuBar menuBar;

	@FXML
	private void initialize() {
		dataLogger.logDebug("Abriendo seleccion de dispositivos");
		setupTableColumns();
		loadStatus();
		loadbuttons();

		data = ConnectionsHandler.load();
		if (data != null && !data.isEmpty()) {
			deviceTable.getItems().setAll(data);
		} else {
			dataLogger.logInfo("No se encontraron dispositivos guardados");
		}

		TableUtils.adjustColumns(deviceTable, true);
	}

	@Override
	public void setAppContext(AppContext context) {
		this.appContext = context;
		this.wsManager = context.getWsManager();
		this.dataLogger = context.getDataLogger();
	}

	@SuppressWarnings("unchecked")
	private void setupTableColumns() {
		// Número
		TableColumn<ConnectionData, String> numberCol = new TableColumn<>("Numero");
		numberCol.setCellValueFactory(cd -> {
			int index = deviceTable.getItems().indexOf(cd.getValue()) + 1;
			return new SimpleStringProperty(String.valueOf(index));
		});
		numberCol.setCellFactory(centeredTextCellFactory());

		// Nombre
		TableColumn<ConnectionData, String> nameCol = new TableColumn<>("Nombre");
		nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
		nameCol.setCellFactory(TextFieldTableCell.forTableColumn());
		nameCol.setOnEditCommit(event -> {
			String newName = event.getNewValue();
			ConnectionData data = event.getRowValue();
			int index = event.getTablePosition().getRow();

			if (newName == null || newName.trim().isEmpty()) {
				Alerts.showWarningAlert("Nombre inválido", null, "El nombre no puede estar vacío.");
				deviceTable.refresh();
				return;
			}

			Optional<ButtonType> result = Alerts.showConfirmationAlert("Confirmación de cambio",
					"¿Deseas cambiar el nombre del dispositivo?", "De: " + data.getName() + "\nA: " + newName);

			if (result.isPresent() && result.get() == ButtonType.OK) {
				data.setName(newName);
				ConnectionsHandler.updateName(index, newName);
				deviceTable.refresh();
			} else {
				deviceTable.refresh();
			}
		});

		// Estatus local
		TableColumn<ConnectionData, Void> statusLocalCol = new TableColumn<>("Local");
		statusLocalCol.setCellFactory(col -> new TableCell<>() {
			private final Button connectButton = new Button();

			{
				connectButton.setOnAction(event -> {
					ConnectionData data = getTableView().getItems().get(getIndex());
					URI target = data.getUri4(); // ← IPV4
					if (target == null) {
						Alerts.showErrorAlert("Error de conexión", "Dirección inválida",
								"Este dispositivo no tiene una dirección IPv4 válida para conexión.");
						return;
					}
					startConnection(data, target, false);
				});
			}

			@Override
			protected void updateItem(Void item, boolean empty) {
				super.updateItem(item, empty);
				if (empty || getIndex() >= getTableView().getItems().size()) {
					setGraphic(null);
				} else {
					ConnectionData data = getTableView().getItems().get(getIndex());
					String ip = data.getIpV4();

					connectButton.setText("Comprobando...");
					connectButton.setDisable(true);
					connectButton.setStyle("-fx-text-fill: orange;");

					// Hilo para no bloquear la UI
					new Thread(() -> {
						boolean reachable = NetworkUtil.isReachable(ip);

						if (!reachable) {
							Platform.runLater(() -> {
								disableButton(connectButton);
							});
							return;
						}

						NetworkUtil.ping(data.getUri4(), isAvailable -> {
							Platform.runLater(() -> {
								if (isAvailable) {
									enabledButton(connectButton);
								} else {
									disableButton(connectButton);
								}
							});
						});

					}, "Check-Local-Ping").start();

					HBox box = new HBox(connectButton);
					box.setAlignment(Pos.CENTER);
					setGraphic(box);
				}
			}
		});

		// Estatus remoto
		TableColumn<ConnectionData, Void> statusRemoteCol = new TableColumn<>("Remoto");
		statusRemoteCol.setCellFactory(col -> new TableCell<>() {
			private final Button connectButton = new Button();

			{
				connectButton.setOnAction(event -> {
					ConnectionData data = getTableView().getItems().get(getIndex());
					URI target = data.getUri6(); // ← IPV6
					if (target == null) {
						Alerts.showErrorAlert("Error de conexión", "Dirección inválida",
								"Este dispositivo no tiene una dirección IPv6 válida para conexión.");
						return;
					}
					startConnection(data, target, true);
				});
			}

			@Override
			protected void updateItem(Void item, boolean empty) {
				super.updateItem(item, empty);
				if (empty || getIndex() >= getTableView().getItems().size()) {
					setGraphic(null);
				} else {
					ConnectionData data = getTableView().getItems().get(getIndex());
					String ip = data.getIpV6();

					connectButton.setText("Comprobando...");
					connectButton.setDisable(true);
					connectButton.setStyle("-fx-text-fill: orange;");

					new Thread(() -> {
						boolean reachable = NetworkUtil.isReachable(ip);

						if (!reachable) {
							Platform.runLater(() -> {
								disableButton(connectButton);
							});
							return;
						}

						NetworkUtil.ping(data.getUri6(), isAvailable -> {
							Platform.runLater(() -> {
								if (isAvailable) {
									enabledButton(connectButton);
								} else {
									disableButton(connectButton);
								}
							});
						});
					}, "Check-Remote-Ping").start();

					HBox box = new HBox(connectButton);
					box.setAlignment(Pos.CENTER);
					setGraphic(box);
				}
			}
		});

		// Botón eliminar
		TableColumn<ConnectionData, Void> deleteCol = new TableColumn<>("Eliminar");
		deleteCol.setCellFactory(col -> new TableCell<>() {
			private final Button btn = new Button("Eliminar");

			{
				btn.setOnAction(event -> {
					ConnectionData data = getTableView().getItems().get(getIndex());

					Optional<ButtonType> result = Alerts.showConfirmationAlert("Confirmacion elimnacion",
							"¿Desas eliminar este dispositivo?", "Dispositivo: " + data.getName());
					if (result.isPresent() && result.get() == ButtonType.OK) {
						int index = getIndex();
						deviceTable.getItems().remove(data);
						ConnectionsHandler.removeConnectionData(index);
					}
				});

			}

			@Override
			protected void updateItem(Void item, boolean empty) {
				super.updateItem(item, empty);
				if (empty) {
					setGraphic(null);
				} else {
					HBox box = new HBox(btn);
					box.setAlignment(Pos.CENTER);
					setGraphic(box);
				}
			}
		});

		deviceTable.getColumns().setAll(deleteCol, numberCol, nameCol, statusLocalCol, statusRemoteCol);
	}

	private void enabledButton(Button button) {
		Platform.runLater(() -> {
			button.setText("Disponible");
			button.setDisable(false);
			button.setStyle("-fx-text-fill: green;");
		});
	}

	private void disableButton(Button button) {
		Platform.runLater(() -> {
			button.setText("Sin conexión");
			button.setDisable(true);
			button.setStyle("-fx-text-fill: red;");
		});
	}

	private <T> Callback<TableColumn<ConnectionData, T>, TableCell<ConnectionData, T>> centeredTextCellFactory() {
		return column -> new TableCell<>() {
			@Override
			protected void updateItem(T item, boolean empty) {
				super.updateItem(item, empty);
				if (empty || item == null) {
					setGraphic(null);
				} else {
					Label label = new Label(item.toString());
					HBox box = new HBox(label);
					box.setAlignment(Pos.CENTER);
					setGraphic(box);
				}
			}
		};
	}

	private void startConnection(ConnectionData data, URI uri, boolean isRemote) {
		dataLogger.logInfo("Intentando establecer conexion con -> " + data.toString());

		Platform.runLater(() -> {
			this.connectingAlert = Alerts.showInformationAlert("Conectando", null, "Estableciendo conexion...");
		});

		wsManager.connect(this::onConnectionSuccess, this::onConnectionFailure, data, isRemote);
	}

	private void onConnectionSuccess() {
		var messageQueue = appContext.getMessageQueue();
		var processedQueue = appContext.getProcessedQueue();

		DataProcessor processor = new DataProcessor(messageQueue, processedQueue);
		appContext.setDataProcessor(processor);
		processor.startProcessing();

		Platform.runLater(() -> {
			if (connectingAlert != null) {
				connectingAlert.close();
			}
			appContext.getSceneManager().switchTo(SceneType.DASHBOARD);
		});
	}

	private void onConnectionFailure() {
		Platform.runLater(() -> {
			if (connectingAlert != null) {
				connectingAlert.close();
			}
			Alerts.showErrorAlert("Error", "No se pudo conectar", "Verifica tus conexiones a la red");
		});
	}

	private void loadStatus() {
		setStatusAsync(imgDriverStatus, NetworkUtil::hasActiveNetworkInterface);
		setStatusAsync(imgLANStatus, NetworkUtil::isLocalNetworkAvailable);
		setStatusAsync(imgInternetStatus, NetworkUtil::isInternetAvailable);
		setStatusAsync(imgIpv6Status, NetworkUtil::isGlobalIPv6Available);
	}

	private void setStatusAsync(ImageView imageView, Supplier<Boolean> checkFunction) {
		Platform.runLater(() -> imageView.setImage(new Image("org/axolotlj/RemoteHealth/img/trabajo-en-progreso.png")));

		new Thread(() -> {
			boolean result = false;
			try {
				result = checkFunction.get();
			} catch (Exception e) {
				String errMesagge = "StartupController::setStatusAsync - Error verificando estado: " + e.getMessage();
				dataLogger.logError(errMesagge);
				System.err.println(errMesagge);
			}

			boolean finalResult = result;
			Platform.runLater(() -> {
				String imgPath = finalResult ? "org/axolotlj/RemoteHealth/img/comprobado.png"
						: "org/axolotlj/RemoteHealth/img/boton-x.png";
				imageView.setImage(new Image(imgPath));
			});
		}, "Thread-setStatusAsync").start();
	}

	private void loadbuttons() {
		refreshStatusBtn.setOnAction(event -> loadStatus());

		refreshDevicesBtn.setOnAction(event -> {
			data = ConnectionsHandler.load();
			if (data != null && !data.isEmpty()) {
				deviceTable.getItems().setAll(data);
			} else {
				deviceTable.getItems().clear();
			}
		});

		addDeviceBtn.setOnAction(event -> {
			appContext.getSceneManager().switchTo(SceneType.DEVICE_SETUP);
		});
	}
}
