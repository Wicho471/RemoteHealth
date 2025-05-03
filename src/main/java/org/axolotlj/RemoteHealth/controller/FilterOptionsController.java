package org.axolotlj.RemoteHealth.controller;

import java.util.Optional;

import org.axolotlj.RemoteHealth.app.ui.AlertUtil;
import org.axolotlj.RemoteHealth.config.files.AnalysisFiltersConfig;
import org.axolotlj.RemoteHealth.config.files.RealTimeFiltersConfig;
import org.axolotlj.RemoteHealth.filters.WaveletDenoiser;

import javafx.fxml.FXML;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;

public class FilterOptionsController {

	public enum FilterTypeOption {
	    ECG_ANALYSIS("ECG Analisis"),
	    PLETH_ANALYSIS("Pleth Analisis"),
	    ECG_REAL_TIME("ECG para tiempo real"),
	    PLETH_REAL_TIME("Pleth para tiempo real");

	    private final String displayName;

	    private FilterTypeOption(String displayName) {
	        this.displayName = displayName;
	    }

	    public String getDisplayName() {
	        return displayName;
	    }
	}

	
	private FilterTypeOption filterTypeOption;
	private RealTimeFiltersConfig realTimeFiltersConfig;
	private AnalysisFiltersConfig analysisFiltersConfig;
	
    // --- Pasa bandas ---
    @FXML private TextField filterBandPassHighField;
    @FXML private TextField filterBandPassLowField;
    @FXML private TextField filterBandPassOrderField;
    @FXML private CheckBox filterBandPassEnabledField;

    // --- Rechaza bandas ---
    @FXML private TextField filterBandStopHighField;
    @FXML private TextField filterBandStopLowField;
    @FXML private TextField filterBandStopOrderField;
    @FXML private CheckBox filterBandStopEnabledField;

    // --- Transformada de Wavelet ---
    @FXML private ComboBox<String> filterWaveletTypeField;
    @FXML private TextField filterWaveletLevelField;
    @FXML private CheckBox filterWaveletSmothField;
    @FXML private TextField filterWaveletThresholdField;
    @FXML private CheckBox filterWaveletEnabledField;

    // --- Savitzky-Golay ---
    @FXML private TextField filterSavitzkyWindowField;
    @FXML private TextField filterSavitzkyPolyField;
    @FXML private CheckBox filterSavitzkyEnabledField;
    
    @FXML private TitledPane bandPassPanel;
    
    @FXML private TitledPane bandStopPanel;
    
    @FXML private TitledPane waveletPanel;
     
    @FXML private TitledPane smoothPanel;

    // --- Métodos de inicialización (opcional) ---
    @FXML
    public void initialize() {
    	this.realTimeFiltersConfig = new RealTimeFiltersConfig();
    	this.analysisFiltersConfig = new AnalysisFiltersConfig();
    	filterWaveletTypeField.getItems().addAll(WaveletDenoiser.AVAILABLE_WAVELETS);
    }
    
    @FXML
    private void restoreHandle() {
    	if (filterTypeOption == null) return;
    	Optional<ButtonType> result = AlertUtil.showConfirmationAlert("Confirmacion", filterTypeOption.getDisplayName(), "Estas seguro de que deseas restaurar los filtros de fabrica?");
    	if (result.isPresent() && result.get() == ButtonType.OK) {
        	switch (filterTypeOption) {
    		case ECG_ANALYSIS:
    			if (analysisFiltersConfig != null) {
					analysisFiltersConfig.restoreEcgDefaults();
					setFromAnalysisEcg();
				}
    			break;
    		case PLETH_ANALYSIS:
    			if (analysisFiltersConfig != null) {
					analysisFiltersConfig.restorePlethDefaults();
					setFromAnalysisPleth();
				}
    			break;
    		case ECG_REAL_TIME:
    			if (realTimeFiltersConfig != null) {
					realTimeFiltersConfig.restoreEcgDefaults();
					setFromRealTimeEcg();
				}
    			break;
    		case PLETH_REAL_TIME:
    			if (realTimeFiltersConfig != null) {
					realTimeFiltersConfig.restorePlethDefaults();
					setFromRealTimePleth();
				}
    			break;
    		default:
    			System.err.println("Enumeracion no reconocida");
    			break;
    		}
    	}
    }
    
    @FXML
    private void applyHandle() {
    	if(filterTypeOption == null) return;
    	switch (filterTypeOption) {
		case ECG_ANALYSIS:
			saveToAnalysisEcg();
			setFromAnalysisEcg();
			break;
		case PLETH_ANALYSIS:
			saveToAnalysisPleth();
			setFromAnalysisPleth();
			break;
		case ECG_REAL_TIME:
			saveToRealTimeEcg();
			setFromRealTimeEcg();
			break;
		case PLETH_REAL_TIME:
			saveToRealTimePleth();
			setFromRealTimePleth();
			break;
		default:
			System.err.println("Enumeracion no reconocida");
			AlertUtil.showInformationAlert("Error", null, "No se pudieron aplicar los cambios");
			return;
		}
    	AlertUtil.showInformationAlert("Exito", null, "Cambios aplicados exitosamente");
	}
    
    public void setType(FilterTypeOption filterTypeOption) {
    	this.filterTypeOption = filterTypeOption;
    	switch (filterTypeOption) {
		case ECG_ANALYSIS:
			setFromAnalysisEcg();
			break;

		case PLETH_ANALYSIS:
			setFromAnalysisPleth();
			break;
		case ECG_REAL_TIME:
			setFromRealTimeEcg();
			break;
		case PLETH_REAL_TIME:
			setFromRealTimePleth();
			break;
		default:
			System.err.println("Enumeracion no reconocida");
			break;
		}
	}
	
	private void setFromAnalysisEcg() {
		analysisFiltersConfig.loadProperties();
	    this.filterBandPassOrderField.setText(String.valueOf(analysisFiltersConfig.getEcgBandpassOrder()));
	    this.filterBandPassLowField.setText(String.valueOf(analysisFiltersConfig.getEcgBandpassLow()));
	    this.filterBandPassHighField.setText(String.valueOf(analysisFiltersConfig.getEcgBandpassHigh()));
	    this.filterBandPassEnabledField.setSelected(analysisFiltersConfig.isEcgBandpassEnabled());

	    this.filterBandStopOrderField.setText(String.valueOf(analysisFiltersConfig.getEcgBandstopOrder()));
	    this.filterBandStopLowField.setText(String.valueOf(analysisFiltersConfig.getEcgBandstopLow()));
	    this.filterBandStopHighField.setText(String.valueOf(analysisFiltersConfig.getEcgBandstopHigh()));
	    this.filterBandStopEnabledField.setSelected(analysisFiltersConfig.isEcgBandstopEnabled());
	    
	    this.filterWaveletTypeField.getSelectionModel().select(analysisFiltersConfig.getEcgWaveletType());
	    this.filterWaveletLevelField.setText(String.valueOf(analysisFiltersConfig.getEcgWaveletLevel()));
	    this.filterWaveletThresholdField.setText(String.valueOf(analysisFiltersConfig.getEcgWaveletThreshold()));
	    this.filterWaveletSmothField.setSelected(analysisFiltersConfig.isEcgWaveletSoft());
	    this.filterWaveletEnabledField.setSelected(analysisFiltersConfig.isEcgWaveletEnabled());
	    
	    this.filterSavitzkyWindowField.setText(String.valueOf(analysisFiltersConfig.getEcgSGWindow()));
	    this.filterSavitzkyPolyField.setText(String.valueOf(analysisFiltersConfig.getEcgSGPoly()));
	    this.filterSavitzkyEnabledField.setSelected(analysisFiltersConfig.isEcgSGEnabled());
	}

	private void setFromAnalysisPleth() {
		analysisFiltersConfig.loadProperties();
	    this.filterBandPassOrderField.setText(String.valueOf(analysisFiltersConfig.getPlethBandpassOrder()));
	    this.filterBandPassLowField.setText(String.valueOf(analysisFiltersConfig.getPlethBandpassLow()));
	    this.filterBandPassHighField.setText(String.valueOf(analysisFiltersConfig.getPlethBandpassHigh()));
	    this.filterBandPassEnabledField.setSelected(analysisFiltersConfig.isPlethBandpassEnabled());

	    this.filterBandStopOrderField.setText(String.valueOf(analysisFiltersConfig.getPlethBandstopOrder()));
	    this.filterBandStopLowField.setText(String.valueOf(analysisFiltersConfig.getPlethBandstopLow()));
	    this.filterBandStopHighField.setText(String.valueOf(analysisFiltersConfig.getPlethBandstopHigh()));
	    this.filterBandStopEnabledField.setSelected(analysisFiltersConfig.isPlethBandstopEnabled());

	    this.filterWaveletTypeField.getSelectionModel().select(analysisFiltersConfig.getPlethWaveletType());
	    this.filterWaveletLevelField.setText(String.valueOf(analysisFiltersConfig.getPlethWaveletLevel()));
	    this.filterWaveletThresholdField.setText(String.valueOf(analysisFiltersConfig.getPlethWaveletThreshold()));
	    this.filterWaveletSmothField.setSelected(analysisFiltersConfig.isPlethWaveletSoft());
	    this.filterWaveletEnabledField.setSelected(analysisFiltersConfig.isPlethWaveletEnabled());

	    this.filterSavitzkyWindowField.setText(String.valueOf(analysisFiltersConfig.getPlethSGWindow()));
	    this.filterSavitzkyPolyField.setText(String.valueOf(analysisFiltersConfig.getPlethSGPoly()));
	    this.filterSavitzkyEnabledField.setSelected(analysisFiltersConfig.isPlethSGEnabled());
	}


	private void saveToAnalysisEcg() {
	    analysisFiltersConfig.setEcgBandpassOrder(Integer.parseInt(this.filterBandPassOrderField.getText()));
	    analysisFiltersConfig.setEcgBandpassLow(Double.parseDouble(this.filterBandPassLowField.getText()));
	    analysisFiltersConfig.setEcgBandpassHigh(Double.parseDouble(this.filterBandPassHighField.getText()));
	    analysisFiltersConfig.setEcgBandpassEnabled(this.filterBandPassEnabledField.isSelected());

	    analysisFiltersConfig.setEcgBandstopOrder(Integer.parseInt(this.filterBandStopOrderField.getText()));
	    analysisFiltersConfig.setEcgBandstopLow(Double.parseDouble(this.filterBandStopLowField.getText()));
	    analysisFiltersConfig.setEcgBandstopHigh(Double.parseDouble(this.filterBandStopHighField.getText()));

	    analysisFiltersConfig.setEcgWaveletType(this.filterWaveletTypeField.getValue());
	    analysisFiltersConfig.setEcgWaveletLevel(Integer.parseInt(this.filterWaveletLevelField.getText()));
	    analysisFiltersConfig.setEcgWaveletThreshold(Double.parseDouble(this.filterWaveletThresholdField.getText()));
	    analysisFiltersConfig.setEcgWaveletSoft(this.filterWaveletSmothField.isSelected());

	    analysisFiltersConfig.setEcgSGWindow(Integer.parseInt(this.filterSavitzkyWindowField.getText()));
	    analysisFiltersConfig.setEcgSGPoly(Integer.parseInt(this.filterSavitzkyPolyField.getText()));
	    analysisFiltersConfig.saveProperties();
	}


	private void saveToAnalysisPleth() {
	    analysisFiltersConfig.setPlethBandpassOrder(Integer.parseInt(this.filterBandPassOrderField.getText()));
	    analysisFiltersConfig.setPlethBandpassLow(Double.parseDouble(this.filterBandPassLowField.getText()));
	    analysisFiltersConfig.setPlethBandpassHigh(Double.parseDouble(this.filterBandPassHighField.getText()));
	    analysisFiltersConfig.setPlethBandpassEnabled(this.filterBandPassEnabledField.isSelected()); // Esto también falta

	    analysisFiltersConfig.setPlethBandstopOrder(Integer.parseInt(this.filterBandStopOrderField.getText()));
	    analysisFiltersConfig.setPlethBandstopLow(Double.parseDouble(this.filterBandStopLowField.getText()));
	    analysisFiltersConfig.setPlethBandstopHigh(Double.parseDouble(this.filterBandStopHighField.getText()));
	    analysisFiltersConfig.setPlethBandstopEnabled(filterBandStopEnabledField.isSelected());
	    
	    analysisFiltersConfig.setPlethWaveletType(this.filterWaveletTypeField.getValue());
	    analysisFiltersConfig.setPlethWaveletLevel(Integer.parseInt(this.filterWaveletLevelField.getText()));
	    analysisFiltersConfig.setPlethWaveletThreshold(Double.parseDouble(this.filterWaveletThresholdField.getText()));
	    analysisFiltersConfig.setPlethWaveletSoft(this.filterWaveletSmothField.isSelected());
	    analysisFiltersConfig.setPlethWaveletEnabled(filterWaveletEnabledField.isSelected());

	    analysisFiltersConfig.setPlethSGWindow(Integer.parseInt(this.filterSavitzkyWindowField.getText()));
	    analysisFiltersConfig.setPlethSGPoly(Integer.parseInt(this.filterSavitzkyPolyField.getText()));
	    analysisFiltersConfig.setPlethSGEnabled(filterSavitzkyEnabledField.isSelected());
	    analysisFiltersConfig.saveProperties();
	}

	
	private void setFromRealTimeEcg() {
		realTimeFiltersConfig.loadProperties();
	    filterBandPassOrderField.setText(String.valueOf(realTimeFiltersConfig.getEcgBandpassOrder()));
	    filterBandPassLowField.setText(String.valueOf(realTimeFiltersConfig.getEcgBandpassLow()));
	    filterBandPassHighField.setText(String.valueOf(realTimeFiltersConfig.getEcgBandpassHigh()));
	    filterBandPassEnabledField.setSelected(realTimeFiltersConfig.isEcgBandpassEnabled());

	    filterBandStopOrderField.setText(String.valueOf(realTimeFiltersConfig.getEcgBandstopOrder()));
	    filterBandStopLowField.setText(String.valueOf(realTimeFiltersConfig.getEcgBandstopLow()));
	    filterBandStopHighField.setText(String.valueOf(realTimeFiltersConfig.getEcgBandstopHigh()));
	    filterBandStopEnabledField.setSelected(realTimeFiltersConfig.isEcgBandstopEnabled());

	    filterWaveletTypeField.getSelectionModel().clearSelection();
	    filterWaveletLevelField.clear();
	    filterWaveletThresholdField.clear();
	    filterWaveletSmothField.setSelected(false);
	    filterWaveletEnabledField.setSelected(false);

	    filterSavitzkyWindowField.clear();
	    filterSavitzkyPolyField.clear();
	    filterSavitzkyEnabledField.setSelected(false);

	    waveletPanel.setDisable(true);
	    smoothPanel.setDisable(true);
	}


	private void setFromRealTimePleth() {
		realTimeFiltersConfig.loadProperties();
	    filterBandPassOrderField.setText(String.valueOf(realTimeFiltersConfig.getPlethBandpassOrder()));
	    filterBandPassLowField.setText(String.valueOf(realTimeFiltersConfig.getPlethBandpassLow()));
	    filterBandPassHighField.setText(String.valueOf(realTimeFiltersConfig.getPlethBandpassHigh()));
	    filterBandPassEnabledField.setSelected(realTimeFiltersConfig.isPlethBandpassEnabled());

	    filterBandStopOrderField.setText(String.valueOf(realTimeFiltersConfig.getPlethBandstopOrder()));
	    filterBandStopLowField.setText(String.valueOf(realTimeFiltersConfig.getPlethBandstopLow()));
	    filterBandStopHighField.setText(String.valueOf(realTimeFiltersConfig.getPlethBandstopHigh()));
	    filterBandStopEnabledField.setSelected(realTimeFiltersConfig.isPlethBandstopEnabled());

	    filterWaveletTypeField.getSelectionModel().clearSelection();
	    filterWaveletLevelField.clear();
	    filterWaveletThresholdField.clear();
	    filterWaveletSmothField.setSelected(false);
	    filterWaveletEnabledField.setSelected(false);

	    filterSavitzkyWindowField.clear();
	    filterSavitzkyPolyField.clear();
	    filterSavitzkyEnabledField.setSelected(false);

	    waveletPanel.setDisable(true);
	    smoothPanel.setDisable(true);
	}
	
	private void saveToRealTimeEcg() {
	    realTimeFiltersConfig.setEcgBandpassOrder(Integer.parseInt(filterBandPassOrderField.getText()));
	    realTimeFiltersConfig.setEcgBandpassLow(Double.parseDouble(filterBandPassLowField.getText()));
	    realTimeFiltersConfig.setEcgBandpassHigh(Double.parseDouble(filterBandPassHighField.getText()));
	    realTimeFiltersConfig.setEcgBandpassEnabled(filterBandPassEnabledField.isSelected());

	    realTimeFiltersConfig.setEcgBandstopOrder(Integer.parseInt(filterBandStopOrderField.getText()));
	    realTimeFiltersConfig.setEcgBandstopHigh(Double.parseDouble(filterBandStopHighField.getText()));
	    realTimeFiltersConfig.setEcgBandstopLow(Double.parseDouble(filterBandStopLowField.getText()));
	    realTimeFiltersConfig.setEcgBandstopEnabled(filterBandStopEnabledField.isSelected());
	    realTimeFiltersConfig.saveProperties();
	}
	
	private void saveToRealTimePleth() {
	    realTimeFiltersConfig.setPlethBandpassOrder(Integer.parseInt(filterBandPassOrderField.getText()));
	    realTimeFiltersConfig.setPlethBandpassLow(Double.parseDouble(filterBandPassLowField.getText()));
	    realTimeFiltersConfig.setPlethBandpassHigh(Double.parseDouble(filterBandPassHighField.getText()));
	    realTimeFiltersConfig.setPlethBandpassEnabled(filterBandPassEnabledField.isSelected());

	    realTimeFiltersConfig.setPlethBandstopOrder(Integer.parseInt(filterBandStopOrderField.getText()));
	    realTimeFiltersConfig.setPlethBandstopHigh(Double.parseDouble(filterBandStopHighField.getText()));
	    realTimeFiltersConfig.setPlethBandstopLow(Double.parseDouble(filterBandStopLowField.getText()));
	    realTimeFiltersConfig.setPlethBandstopEnabled(filterBandStopEnabledField.isSelected());
	    realTimeFiltersConfig.saveProperties();
	}
}
