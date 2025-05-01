package org.axolotlj.RemoteHealth.controller;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.axolotlj.RemoteHealth.core.AppContext;
import org.axolotlj.RemoteHealth.core.AppContext.ContextAware;
import org.axolotlj.RemoteHealth.service.websocket.WebSocketManager;
import org.axolotlj.RemoteHealth.util.cmd.CommandResponseListener;
import org.axolotlj.RemoteHealth.util.cmd.CommandHandler.CommandType;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class ESP32ToolsController implements ContextAware , CommandResponseListener {

	private AppContext appContext;
	private WebSocketManager webSocketManager;
	@FXML
	private Button closeButton;
	
	@FXML
	public void initialize() {
		if(webSocketManager == null) return;
		if (!webSocketManager.isConnected()) return;
		
	}

	   // Método que maneja el evento de cierre del modal
    @FXML
    private void closeHandle() {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }

    // Método para manejar el evento del botón "Configurar" de MMA8452Q
    @FXML
    private void configYzHandle() {
        showConfigMessage("MMA8452Q");
    }

    // Método para manejar el evento del botón "Configurar" de ESP32
    @FXML
    private void configEsp32Handle() {
        showConfigMessage("ESP32");
    }

    // Método para manejar el evento del botón "Configurar" de MAX30102
    @FXML
    private void configMaxHandle() {
        showConfigMessage("MAX30102");
    }

    // Método para manejar el evento del botón "Configurar" de AD8232
    @FXML
    private void configAdHandle() {
        showConfigMessage("AD8232");
    }

    // Método para manejar el evento del botón "Configurar" de MLX90614t
    @FXML
    private void configMlxHandle() {
        showConfigMessage("MLX90614t");
    }

    // Método común para mostrar un mensaje de configuración
    private void showConfigMessage(String deviceName) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Configuración");
        alert.setHeaderText("Configuración de " + deviceName);
        alert.setContentText("El dispositivo " + deviceName + " ha sido configurado correctamente.");
        alert.showAndWait();
    }
	
	@Override
	public void setAppContext(AppContext context) {
		this.appContext = context;
		this.webSocketManager = context.getWsManager();
	}

	@Override
	public void onCommandResponse(ImmutablePair<CommandType, String> response) {
		CommandType type = response.getLeft();
		String content = response.getRight();
		
	}

}
