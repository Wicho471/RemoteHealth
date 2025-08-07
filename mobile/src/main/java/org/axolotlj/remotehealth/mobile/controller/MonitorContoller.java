package org.axolotlj.remotehealth.mobile.controller;

import org.axolotlj.remotehealth.core.controller.AbstractMonitorController;
import org.axolotlj.remotehealth.core.sensor.data.DataPoint;

import com.gluonhq.charm.glisten.mvc.View;

import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;

public class MonitorContoller extends AbstractMonitorController {

	@FXML
	private View homeView;

	@FXML
	private TextField pacientNameField;

	@FXML
	private LineChart<Number, Number> ECG;

	@FXML
	private LineChart<Number, Number> PLETH;

	@FXML
	private TextArea BPM;

	@FXML
	private TextArea SPO2;

	@FXML
	private TextArea BP;

	@FXML
	private TextArea TEMP1;

	@FXML
	private TextArea MOV;

	@FXML
	private ImageView statusBpm;

	@FXML
	private ImageView statusSpo2;

	@FXML
	private ImageView statusBp;

	@FXML
	private ImageView statusTemp;

	@FXML
	private ImageView statusMov;

	@FXML
	private ImageView imgRecordStatus;

	@FXML
	private Button handleRec;

	/**
	 * Inicializa los componentes cuando se carga la vista.
	 */
	@FXML
	public void initialize() {
		homeView.setOnShowing(e -> {
			
		});
	}

	@Override
	protected void applyToChart(DataPoint data) {
		// TODO Auto-generated method stub
		
	}

	@FXML
	@Override
	protected void handleClose() {
		// TODO Auto-generated method stub
		
	}

	@FXML
	@Override
	protected void handleRec() {
		// TODO Auto-generated method stub
		
	}
	

}
