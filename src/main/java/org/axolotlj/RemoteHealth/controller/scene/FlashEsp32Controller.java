package org.axolotlj.RemoteHealth.controller.scene;

import org.axolotlj.RemoteHealth.app.SceneType;
import org.axolotlj.RemoteHealth.app.ui.AlertUtil;
import org.axolotlj.RemoteHealth.core.AppContext;
import org.axolotlj.RemoteHealth.core.AppContext.ContextAware;
import org.axolotlj.RemoteHealth.core.AppContext.DisposableController;
import org.axolotlj.RemoteHealth.service.serial.SerialMonitorService;

import com.fazecast.jSerialComm.SerialPort;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;

public class FlashEsp32Controller implements ContextAware, DisposableController{
	private AppContext appContext;
	private SerialMonitorService monitorService;

	
    @FXML
    private TextArea outputSerialTextArea;

    @FXML
    private TextArea debugTextArea;

    @FXML
    private Label statusLabel;

    @FXML
    private ImageView imgEsp32;

    @FXML
    private Button flashBtn;

    @FXML
    private Button cleanSerialBtn;
    
    @FXML
    private Button cleanDebugbtn;
    
    @FXML
    private Button backBtn;

    @FXML
    private ProgressBar progressbar;
    
    @FXML
    private ComboBox<SerialPort> deviceSelector;

    @FXML
    public void initialize() {
    	initComboBox();
    	outputSerialTextArea.setOpacity(1.0);
    	debugTextArea.setOpacity(1.0);
        statusLabel.setText("Esperando conexion USB...");
        progressbar.setProgress(0.0);
    }

    @FXML
    private void handleFlashButton() {
    	AlertUtil.buildingModule();
    }

    @FXML
    private void handleBackButton() {
    	monitorService.stopMonitoring();
    	appContext.getSceneManager().switchTo(SceneType.DEVICE_SELECTOR);
    }

    @FXML
    private void cleanSerialhandle() {
    	outputSerialTextArea.clear();
	}
    
    @FXML
    private void cleanDebugHandle() {
    	debugTextArea.clear();
	}
    
	@Override
	public void setAppContext(AppContext context) {
		this.appContext = context;
	}

	private void initComboBox() {
    	monitorService = new SerialMonitorService(deviceSelector, flashBtn, imgEsp32, outputSerialTextArea);
    	deviceSelector.setConverter(new javafx.util.StringConverter<SerialPort>() {
    	    @Override
    	    public String toString(SerialPort port) {
    	        if (port == null) return "";
    	        return port.getDescriptivePortName();
    	    }

    	    @Override
    	    public SerialPort fromString(String string) {
    	        return null;
    	    }
    	});
    	
    	deviceSelector.getSelectionModel().selectedItemProperty().addListener((obs, oldPort, newPort) -> {
    	    if (newPort != null && !newPort.equals(oldPort)) {
    	        if (oldPort != null && oldPort.isOpen()) {
    	            oldPort.closePort(); 
    	        }
    	        monitorService.connectToPort(newPort); 
    	    }
    	});

    	monitorService.startMonitoring();
	}

	@Override
	public void dispose() {
		if(monitorService != null) {
			monitorService.stopMonitoring();
			monitorService = null;
		}
	}
	

}
