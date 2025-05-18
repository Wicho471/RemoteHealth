package org.axolotlj.RemoteHealth.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;
import java.util.function.Supplier;

import org.axolotlj.RemoteHealth.app.Images;
import org.axolotlj.RemoteHealth.app.SceneType;
import org.axolotlj.RemoteHealth.app.ui.AlertUtil;
import org.axolotlj.RemoteHealth.app.ui.ButtonUtils;
import org.axolotlj.RemoteHealth.app.ui.FxmlUtils;
import org.axolotlj.RemoteHealth.app.ui.ImageViewUtils;
import org.axolotlj.RemoteHealth.app.ui.TableUtils;
import org.axolotlj.RemoteHealth.app.ui.ToolTipUtil;
import org.axolotlj.RemoteHealth.config.files.ConnectionsHandler;
import org.axolotlj.RemoteHealth.controller.include.MenuBarController;
import org.axolotlj.RemoteHealth.controller.window.DeviceConfigController;
import org.axolotlj.RemoteHealth.core.AppContext;
import org.axolotlj.RemoteHealth.core.AppContext.ContextAware;
import org.axolotlj.RemoteHealth.core.AppContext.DisposableController;
import org.axolotlj.RemoteHealth.lang.I18n;
import org.axolotlj.RemoteHealth.lang.LocaleChangeListener;
import org.axolotlj.RemoteHealth.lang.LocaleChangeNotifier;
import org.axolotlj.RemoteHealth.model.ConnectionData;
import org.axolotlj.RemoteHealth.service.DataProcessor;
import org.axolotlj.RemoteHealth.service.logger.DataLogger;
import org.axolotlj.RemoteHealth.service.websocket.WebSocketManager;
import org.axolotlj.RemoteHealth.service.websocket.WebSocketServerSimulator;
import org.axolotlj.RemoteHealth.util.NetworkUtil;
import org.axolotlj.RemoteHealth.util.paths.Paths;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

public class StartupController implements ContextAware, LocaleChangeListener, DisposableController {
	// Static fields
	private static AppContext appContext;
	private static DataLogger dataLogger;
	private static ArrayList<ConnectionData> data;
	private static Alert connectingAlert;
	private static WebSocketManager wsManager;
	private javafx.beans.value.ChangeListener<Boolean> simulatorListener;
	private MenuBarController menuBarController;

	// FXML components
	@FXML
	private TableView<ConnectionData> deviceTable;
	@FXML
	private ImageView imgDriverStatus, imgLANStatus, imgInternetStatus, imgIpv6Status, simuStatusImg;
	@FXML
	private Button refreshStatusBtn, refreshDevicesBtn, addDeviceBtn, connectSimuBtn;
	@FXML
	private MenuBar menuBar;
	@FXML
	private TextField searchTextField;

	@FXML
	private void initialize() {
		LocaleChangeNotifier.addListener(this);

		dataLogger.logDebug("Abriendo seleccion de dispositivos");
		setupTableColumns();
		loadStatus();
		loadbuttons();

		Optional.ofNullable(ConnectionsHandler.load()).filter(list -> !list.isEmpty()).ifPresentOrElse(
				list -> deviceTable.getItems().setAll(list),
				() -> dataLogger.logInfo("No se encontraron dispositivos guardados"));

		onLocaleChanged();
		setToolTips();
		setupSearchField();
		updateSimuStatus(appContext.getSimulator().isActive());
		simulatorListener = (obs, wasActive, isActive) -> updateSimuStatus(isActive);
		appContext.getSimulator().activeProperty().addListener(simulatorListener);
		
	}
	
	@FXML
	private void connectSimuBtnHandle() {
		WebSocketServerSimulator simulator = appContext.getSimulator();
		if(simulator.isActive()) {
			startConnection(simulator.getConnection(), false);
		}
	}

	private void updateSimuStatus(boolean isActive) {
		Image image = isActive ? Images.IMG_ICONS_GREEN : Images.IMG_ICONS_RED;
		ImageViewUtils.setImage(simuStatusImg, image);
		connectSimuBtn.setDisable(!isActive);
	}

	private void setupSearchField() {
		TableUtils.bindSearch(searchTextField, deviceTable, ConnectionsHandler::load,
				query -> data -> data.getName() != null && data.getName().toLowerCase().contains(query));
	}

	@Override
	public void setAppContext(AppContext context) {
		StartupController.appContext = context;
		StartupController.wsManager = context.getWsManager();
		StartupController.dataLogger = context.getDataLogger();
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
				AlertUtil.showWarningAlert("Nombre inválido", null, "El nombre no puede estar vacío.");
				deviceTable.refresh();
				return;
			}

			Optional<ButtonType> result = AlertUtil.showConfirmationAlert("Confirmación de cambio",
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
			private final ImageView iconView = new ImageView();

			{
				ImageViewUtils.setImage(iconView, Images.IMG_BUTTONS_LOADING, 25, 25);
				ButtonUtils.setGraphicImage(connectButton, iconView);
				connectButton.setText("Conectando...");
				connectButton.setOnAction(event -> {
					ConnectionData data = getTableView().getItems().get(getIndex());
					if (data.getUri4() == null) {
						AlertUtil.showErrorAlert("Error de conexión", "Dirección inválida",
								"Este dispositivo no tiene una dirección IPv4 válida para conexión.");
						return;
					}
					startConnection(data, false);
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

					iconView.setImage(Images.IMG_BUTTONS_LOADING);
					ButtonUtils.waitingButton(connectButton);
					connectButton.setDisable(true);

					new Thread(() -> {
						boolean reachable = NetworkUtil.isReachable(ip);
						Platform.runLater(() -> {
							Image image = reachable ? Images.IMG_BUTTONS_CONECTAR : Images.IMG_BUTTONS_NO_WIFI;
							if (reachable) {
								ButtonUtils.enabledButton(connectButton);
							} else {
								ButtonUtils.disableButton(connectButton);
							}
							ImageViewUtils.setImage(iconView, image, 25, 25);

							connectButton.setDisable(!reachable);
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
			private final ImageView iconView = new ImageView(Images.IMG_BUTTONS_LOADING);

			{
				ButtonUtils.setGraphicImage(connectButton, iconView);
				ImageViewUtils.setImage(iconView, Images.IMG_BUTTONS_LOADING, 25, 25);
				connectButton.setText("Conectando...");
				connectButton.setOnAction(event -> {
					ConnectionData data = getTableView().getItems().get(getIndex());
					if (data.getUri6() == null) {
						AlertUtil.showErrorAlert("Error de conexión", "Dirección inválida",
								"Este dispositivo no tiene una dirección IPv6 válida para conexión.");
						return;
					}
					startConnection(data, true);
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

					iconView.setImage(Images.IMG_BUTTONS_LOADING);
					ButtonUtils.waitingButton(connectButton);
					connectButton.setDisable(true);

					new Thread(() -> {
						boolean reachable = NetworkUtil.isReachable(ip);
						Platform.runLater(() -> {
							Image image = reachable ? Images.IMG_BUTTONS_CONECTAR : Images.IMG_BUTTONS_NO_WIFI;
							if (reachable) {
								ButtonUtils.enabledButton(connectButton);
							} else {
								ButtonUtils.disableButton(connectButton);
							}
							ImageViewUtils.setImage(iconView, image, 25, 25);
							;
						});
					}, "Check-Remote-Ping").start();

					HBox box = new HBox(connectButton);
					box.setAlignment(Pos.CENTER);
					setGraphic(box);
				}
			}
		});

		// Botón eliminar
		TableColumn<ConnectionData, Void> deleteCol = new TableColumn<>("Configurar");
		deleteCol.setCellFactory(col -> new TableCell<>() {
			private final Button btn = new Button();
			private final ImageView imageView = new ImageView();
			{
				ImageViewUtils.setImage(imageView, Images.IMG_BUTTONS_CONFIGURACIONES, 25, 25);
				ButtonUtils.setGraphicImage(btn, imageView);
				btn.setOnAction(event -> {
					ConnectionData data = getTableView().getItems().get(getIndex());
					int index = getIndex();

					Optional<ButtonType> result = AlertUtil.showConfirmationAlert("Confirmación de configuración",
							"¿Deseas configurar este dispositivo?", "Dispositivo: " + data.getName());

					if (result.isPresent() && result.get() == ButtonType.OK) {
						// Abrimos la ventana emergente
						try {
							FXMLLoader loader = FxmlUtils.loadFXML(Paths.VIEW_WINDOW_DEVICECONFIGWINDOW_FXML);
							Pane root = (Pane) loader.load();

							DeviceConfigController controller = loader.getController();
							controller.setData(data, index, () -> {
								// 🚨 Aquí es donde eliminamos y refrescamos la tabla
								Platform.runLater(() -> {
									ConnectionsHandler.removeConnectionData(index);
									deviceTable.getItems().remove(index);
								});
							});

							Stage stage = new Stage();
							stage.setTitle("Configurar dispositivo");
							stage.setScene(new Scene(root));
							stage.initModality(Modality.APPLICATION_MODAL);
							stage.setResizable(false);
							stage.showAndWait();

							// Opcional: Refresh tabla si necesitas actualizar vista tras aplicar cambios
							deviceTable.refresh();

						} catch (IOException e) {
							System.err.println(e.getMessage());
							AlertUtil.showErrorAlert("Error", "No se pudo abrir la ventana de configuración",
									e.getMessage());
						}
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
		TableUtils.adjustColumns(deviceTable, true);
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

	private void startConnection(ConnectionData data, boolean isRemote) {
		dataLogger.logInfo("Intentando establecer conexion con -> " + data.toString());

		Platform.runLater(() -> {
			StartupController.connectingAlert = AlertUtil.showInformationAlert("Conectando", null,
					"Estableciendo conexion...", true);
		});

		wsManager.connect(this::onConnectionSuccess, this::onConnectionFailure, data, isRemote);
	}

	private void onConnectionSuccess() {
		var messageQueue = appContext.getMessageQueue();
		var processedQueue = appContext.getProcessedQueue();

		DataProcessor processor = new DataProcessor(messageQueue, processedQueue, dataLogger);
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
			AlertUtil.showErrorAlert("Error", "No se pudo conectar", "Verifica tus conexiones a la red");
		});
	}

	private void loadStatus() {
		setStatusAsync(imgDriverStatus, NetworkUtil::hasActiveNetworkInterface);
		setStatusAsync(imgLANStatus, NetworkUtil::isLocalNetworkAvailable);
		setStatusAsync(imgInternetStatus, NetworkUtil::isInternetAvailable);
		setStatusAsync(imgIpv6Status, NetworkUtil::isGlobalIPv6Available);
	}

	private void setStatusAsync(ImageView imageView, Supplier<Boolean> checkFunction) {
		ImageViewUtils.setImage(imageView, Images.IMG_ICONS_TRABAJO_EN_PROGRESO);

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
			Image imgPath = finalResult ? Images.IMG_ICONS_COMPROBADO : Images.IMG_ICONS_BOTON_X;
			ImageViewUtils.setImage(imageView, imgPath);
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
			deviceTable.refresh();
		});

		addDeviceBtn.setOnAction(event -> {
			appContext.getSceneManager().switchTo(SceneType.DEVICE_SETUP);
		});
	}

	@Override
	public void onLocaleChanged() {
		refreshStatusBtn.setText(I18n.get("button.refreshStatus"));
		refreshDevicesBtn.setText(I18n.get("button.refreshDevices"));
		addDeviceBtn.setText(I18n.get("button.addDevice"));

		// Actualizar las columnas de la tabla
		for (TableColumn<ConnectionData, ?> column : deviceTable.getColumns()) {
			String id = column.getText();
			if (id != null) {
				switch (id) {
				case "Eliminar", "Delete" -> column.setText(I18n.get("table.column.delete"));
				case "Número", "Number" -> column.setText(I18n.get("table.column.number"));
				case "Nombre", "Name" -> column.setText(I18n.get("table.column.name"));
				case "Local" -> column.setText(I18n.get("table.column.local"));
				case "Remoto", "Remote" -> column.setText(I18n.get("table.column.remote"));
				}
			}
		}
		deviceTable.refresh();
	}

	private void setToolTips() {
		// Botones principales
		ToolTipUtil.applyTooltip(refreshStatusBtn, "Vuelve a comprobar el estado de la red y la conectividad.");
		ToolTipUtil.applyTooltip(refreshDevicesBtn,
				"Recarga la lista de dispositivos guardados desde la configuración.");
		ToolTipUtil.applyTooltip(addDeviceBtn, "Abre el formulario para agregar un nuevo dispositivo a la lista.");

		// Íconos de estado
		ToolTipUtil.applyTooltip(imgDriverStatus,
				"Estado del controlador de red: verifica si hay una interfaz de red activa.");
		ToolTipUtil.applyTooltip(imgLANStatus, "Acceso a red local: indica si hay conexión a la red local.");
		ToolTipUtil.applyTooltip(imgInternetStatus,
				"Conectividad a Internet: comprueba si hay acceso a la red global.");
		ToolTipUtil.applyTooltip(imgIpv6Status, "IPv6 global: muestra si tienes conectividad IPv6 pública disponible.");
	}

	@Override
	public void dispose() {
		LocaleChangeNotifier.removeListener(this);
		if (deviceTable != null) {
			deviceTable.getItems().clear();
			deviceTable.getColumns().clear();
		}

		if (simulatorListener != null) {
			appContext.getSimulator().activeProperty().removeListener(simulatorListener);
			simulatorListener = null;
		}

		data = null;
		connectingAlert = null;

		dataLogger.logInfo("Selector de dispositivos cerrado exitosamente.");
	}

}
