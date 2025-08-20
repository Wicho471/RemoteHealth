package org.axolotlj.remotehealth.mobile.controller;

import java.util.ArrayList;

import org.axolotlj.remotehealth.core.AppContext;
import org.axolotlj.remotehealth.core.AppContext.ContextAware;
import org.axolotlj.remotehealth.core.AppContext.DisposableController;
import org.axolotlj.remotehealth.core.config.files.ConnectionsHandler;
import org.axolotlj.remotehealth.core.model.ConnectionData;
import org.axolotlj.remotehealth.core.service.DataProcessor;
import org.axolotlj.remotehealth.mobile.navigation.ViewManager;
import org.axolotlj.remotehealth.mobile.ui.LoadingOverlayManager;
import org.axolotlj.remotehealth.mobile.ui.MobileAlertUtil;

import com.gluonhq.charm.glisten.application.AppManager;
import com.gluonhq.charm.glisten.control.AppBar;
import com.gluonhq.charm.glisten.control.CharmListCell;
import com.gluonhq.charm.glisten.control.CharmListView;
import com.gluonhq.charm.glisten.control.FloatingActionButton;
import com.gluonhq.charm.glisten.control.TextField;
import com.gluonhq.charm.glisten.mvc.View;
import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

public class HomeController implements ContextAware , DisposableController{

	private AppContext appContext = AppContext.getInstance();

	@FXML
	private View homeView;

	@FXML
	private CharmListView<ConnectionData, String> litsView;

	@FXML
	private TextField searchTextField;

	@FXML
	public void initialize() {
	    homeView.setOnShowing(e -> {
	        configureDefaultAppBar();
	        addFloatingButton();
	        loadConnections();
	    });
	}
	
	public static void configureDefaultAppBar() {
	    AppBar appBar = AppManager.getInstance().getAppBar();
	    appBar.setTitleText("RemoteHealth");
	    appBar.setNavIcon(MaterialDesignIcon.MENU.button(e -> {}));
	    appBar.getActionItems().clear();
	    appBar.getActionItems().add(MaterialDesignIcon.INFO.button(e -> {}));
	}

	private void addFloatingButton() {
		FloatingActionButton fab = new FloatingActionButton();
		fab.setOnAction(e -> ViewManager.showScannerView());
		fab.showOn(homeView);
	}

	private void loadConnections() {
		ArrayList<ConnectionData> connections = ConnectionsHandler.load();
		ObservableList<ConnectionData> items = FXCollections.observableArrayList(connections);

		litsView.setItems(items);
		litsView.setCellFactory(param -> new CharmListCell<>() {
			@Override
			public void updateItem(ConnectionData connection, boolean empty) {
				super.updateItem(connection, empty);

				if (empty || connection == null) {
					setGraphic(null);
					return;
				}

				// Botón de configuración
				Button btnConfig = new Button("⚙");
				btnConfig.setOnAction(e -> {
					MobileAlertUtil.buildingModule();
					System.out.println("[Remote Health] Configurar: " + connection);
				});

				// Etiqueta con el nombre
				Label lblName = new Label(connection.getName());

				// Botón para establecer conexión
				Button btnConnect = new Button("Conectar");
				btnConnect.setOnAction(e -> {
					startConnection(connection);
				});

				HBox row = new HBox(10, btnConfig, lblName, btnConnect);
				row.setAlignment(Pos.CENTER_LEFT);

				setGraphic(row);
			}
		});
	}

	private void startConnection(ConnectionData data) {
		System.out.println("[Remote Health] Intentando establecer conexion con -> " + data.toString());
		LoadingOverlayManager.showLoading();
		appContext.getWsManager().connect(this::onConnectionSuccess, this::onConnectionFailure, data, data.getIpV6() != null);
	}

	private void onConnectionSuccess() {
		var messageQueue = appContext.getMessageQueue();
		var processedQueue = appContext.getProcessedQueue();

		DataProcessor processor = new DataProcessor(messageQueue, processedQueue);
		appContext.setDataProcessor(processor);
		processor.startProcessing();

		LoadingOverlayManager.hideLoading();
		ViewManager.showMonitorView();
	}

	private void onConnectionFailure() {
		LoadingOverlayManager.hideLoading();
		MobileAlertUtil.showErrorAlert("Error", "No se pudo conectar");
	}

	@Override
	public void setAppContext(AppContext context) {
		this.appContext = context;
	}

	@Override
	public void dispose() {
		
	}

}
