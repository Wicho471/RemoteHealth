package org.axolotlj.RemoteHealth.controller.include;

import java.util.Optional;
import java.util.function.Consumer;

import org.axolotlj.RemoteHealth.app.ui.AlertUtil;
import org.axolotlj.RemoteHealth.config.filt.AnalysisFiltersConfig;
import org.axolotlj.RemoteHealth.config.filt.RealTimeFiltersConfig;
import org.axolotlj.RemoteHealth.filters.core.WaveletDenoiser;
import org.axolotlj.RemoteHealth.validations.FilterValidation;

import javafx.fxml.FXML;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;

public class FilterOptionsController {
	
	private FilterTypeOption filterTypeOption;
	private RealTimeFiltersConfig realTimeFiltersConfig;
	private AnalysisFiltersConfig analysisFiltersConfig;
	private Consumer<FilterTypeOption> applyChanges;
	
	// --- Campos de texto ---
	@FXML private TextField filterBandPassHighField, filterBandPassLowField, filterBandPassOrderField, filterBandPassCoefField, filterBandPassTransitionField,
	                        filterBandStopHighField, filterBandStopLowField, filterBandStopOrderField, filterBandStopCoefField, filterBandStopTransitionField,
	                        filterWaveletLevelField, filterWaveletThresholdField,
	                        filterSavitzkyWindowField, filterSavitzkyPolyField;

	// --- CheckBoxes ---
	@FXML private CheckBox filterBandPassEnabledField, filterBandStopEnabledField,
	                      filterWaveletSmothField, filterWaveletEnabledField,
	                      filterSavitzkyEnabledField;

	// --- ComboBox ---
	@FXML private ComboBox<String> filterWaveletTypeField;

	// --- RadioButtons ---
	@FXML private RadioButton filterBandPassIIRRadioBtn, filterBandPassFIRRadioBtn,
	                         filterBandStopIIRRadioBtn, filterBandStopFIRRadioBtn;

	// --- TitledPanes ---
	@FXML private TitledPane bandPassPanel, bandStopPanel, waveletPanel, smoothPanel;

	// --- StackPanes ---
	@FXML private StackPane BandPassStackPane, BandStopStackPane;

	// --- HBoxes ---
	@FXML private HBox bandPassFirBox, bandPassIirBox, bandStopFirBox, bandStopIirBox;

	// --- ToggleGroups ---
	private ToggleGroup bandPassGroup, bandStopGroup;

    @FXML
    public void initialize() {
    	filterWaveletTypeField.getItems().addAll(WaveletDenoiser.AVAILABLE_WAVELETS);
        bandPassGroup = new ToggleGroup();
        filterBandPassIIRRadioBtn.setToggleGroup(bandPassGroup);
        filterBandPassFIRRadioBtn.setToggleGroup(bandPassGroup);

        bandStopGroup = new ToggleGroup();
        filterBandStopIIRRadioBtn.setToggleGroup(bandStopGroup);
        filterBandStopFIRRadioBtn.setToggleGroup(bandStopGroup);
    	
    	initRadioButtons();
    }
    
    @FXML
    private void restoreHandle() {
    	if (filterTypeOption == null) return;
    	Optional<ButtonType> result = AlertUtil.showConfirmationAlert("Confirmacion", filterTypeOption.getDisplayName(), "Estas seguro de que deseas restaurar los filtros de predeterminados?");
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
			AlertUtil.showInformationAlert("Error", null, "No se pudieron aplicar los cambios", true);
			return;
		}
    	applyChanges.accept(filterTypeOption);;
	}
    
	public void setType(FilterTypeOption filterTypeOption, AnalysisFiltersConfig analysisFiltersConfig,
			RealTimeFiltersConfig realTimeFiltersConfig, Consumer<FilterTypeOption> applyChanges) {
		this.applyChanges = applyChanges;
		this.analysisFiltersConfig = analysisFiltersConfig;
		this.realTimeFiltersConfig = realTimeFiltersConfig;
		this.filterTypeOption = filterTypeOption;

		switch (filterTypeOption) {
		case ECG_ANALYSIS -> setFromAnalysisEcg();
		case PLETH_ANALYSIS -> setFromAnalysisPleth();
		case ECG_REAL_TIME -> setFromRealTimeEcg();
		case PLETH_REAL_TIME -> setFromRealTimePleth();
		default -> System.err.println("Enumeracion no reconocida");
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
	


	private void saveToAnalysisEcg() {
	    String bpOrder = filterBandPassOrderField.getText();
	    String bpLow = filterBandPassLowField.getText();
	    String bpHigh = filterBandPassHighField.getText();
	    boolean bpEnabled = filterBandPassEnabledField.isSelected();

	    String bsOrder = filterBandStopOrderField.getText();
	    String bsLow = filterBandStopLowField.getText();
	    String bsHigh = filterBandStopHighField.getText();
	    boolean bsEnabled = filterBandStopEnabledField.isSelected();

	    String waveletType = filterWaveletTypeField.getValue();
	    String waveletLevel = filterWaveletLevelField.getText();
	    String waveletThreshold = filterWaveletThresholdField.getText();
	    boolean waveletSoft = filterWaveletSmothField.isSelected();
	    boolean waveletEnabled = filterWaveletEnabledField.isSelected();

	    String sgWindow = filterSavitzkyWindowField.getText();
	    String sgPoly = filterSavitzkyPolyField.getText();
	    boolean sgEnabled = filterSavitzkyEnabledField.isSelected();

	    if (FilterValidation.handleValidation(FilterValidation.validateOrder(bpOrder))) return;
	    if (FilterValidation.handleValidation(FilterValidation.validateFrequency(bpLow))) return;
	    if (FilterValidation.handleValidation(FilterValidation.validateFrequency(bpHigh))) return;
	    if (FilterValidation.handleValidation(FilterValidation.validateInferiorLessThanSuperior(bpLow, bpHigh))) return;

	    if (FilterValidation.handleValidation(FilterValidation.validateOrder(bsOrder))) return;
	    if (FilterValidation.handleValidation(FilterValidation.validateFrequency(bsLow))) return;
	    if (FilterValidation.handleValidation(FilterValidation.validateFrequency(bsHigh))) return;
	    if (FilterValidation.handleValidation(FilterValidation.validateInferiorLessThanSuperior(bsLow, bsHigh))) return;

	    if (FilterValidation.handleValidation(FilterValidation.validateWaveletLevel(waveletLevel))) return;
	    if (FilterValidation.handleValidation(FilterValidation.validateThreshold(waveletThreshold))) return;
	    if (FilterValidation.handleValidation(FilterValidation.validateWindowSize(sgWindow))) return;
	    if (FilterValidation.handleValidation(FilterValidation.validatePolynomial(sgPoly, sgWindow))) return;

	    analysisFiltersConfig.setEcgBandpassOrder(Integer.parseInt(bpOrder));
	    analysisFiltersConfig.setEcgBandpassLow(Double.parseDouble(bpLow));
	    analysisFiltersConfig.setEcgBandpassHigh(Double.parseDouble(bpHigh));
	    analysisFiltersConfig.setEcgBandpassEnabled(bpEnabled);

	    analysisFiltersConfig.setEcgBandstopOrder(Integer.parseInt(bsOrder));
	    analysisFiltersConfig.setEcgBandstopLow(Double.parseDouble(bsLow));
	    analysisFiltersConfig.setEcgBandstopHigh(Double.parseDouble(bsHigh));
	    analysisFiltersConfig.setEcgBandstopEnabled(bsEnabled);

	    analysisFiltersConfig.setEcgWaveletType(waveletType);
	    analysisFiltersConfig.setEcgWaveletLevel(Integer.parseInt(waveletLevel));
	    analysisFiltersConfig.setEcgWaveletThreshold(Double.parseDouble(waveletThreshold));
	    analysisFiltersConfig.setEcgWaveletSoft(waveletSoft);
	    analysisFiltersConfig.setEcgWaveletEnabled(waveletEnabled);

	    analysisFiltersConfig.setEcgSGWindow(Integer.parseInt(sgWindow));
	    analysisFiltersConfig.setEcgSGPoly(Integer.parseInt(sgPoly));
	    analysisFiltersConfig.setEcgSGEnabled(sgEnabled);

	    analysisFiltersConfig.saveProperties();
	}


	private void saveToAnalysisPleth() {
	    String bpOrder = filterBandPassOrderField.getText();
	    String bpLow = filterBandPassLowField.getText();
	    String bpHigh = filterBandPassHighField.getText();
	    boolean bpEnabled = filterBandPassEnabledField.isSelected();

	    String bsOrder = filterBandStopOrderField.getText();
	    String bsLow = filterBandStopLowField.getText();
	    String bsHigh = filterBandStopHighField.getText();
	    boolean bsEnabled = filterBandStopEnabledField.isSelected();

	    String waveletType = filterWaveletTypeField.getValue();
	    String waveletLevel = filterWaveletLevelField.getText();
	    String waveletThreshold = filterWaveletThresholdField.getText();
	    boolean waveletSoft = filterWaveletSmothField.isSelected();
	    boolean waveletEnabled = filterWaveletEnabledField.isSelected();

	    String sgWindow = filterSavitzkyWindowField.getText();
	    String sgPoly = filterSavitzkyPolyField.getText();
	    boolean sgEnabled = filterSavitzkyEnabledField.isSelected();

	    if (FilterValidation.handleValidation(FilterValidation.validateOrder(bpOrder))) return;
	    if (FilterValidation.handleValidation(FilterValidation.validateFrequency(bpLow))) return;
	    if (FilterValidation.handleValidation(FilterValidation.validateFrequency(bpHigh))) return;
	    if (FilterValidation.handleValidation(FilterValidation.validateInferiorLessThanSuperior(bpLow, bpHigh))) return;

	    if (FilterValidation.handleValidation(FilterValidation.validateOrder(bsOrder))) return;
	    if (FilterValidation.handleValidation(FilterValidation.validateFrequency(bsLow))) return;
	    if (FilterValidation.handleValidation(FilterValidation.validateFrequency(bsHigh))) return;
	    if (FilterValidation.handleValidation(FilterValidation.validateInferiorLessThanSuperior(bsLow, bsHigh))) return;

	    if (FilterValidation.handleValidation(FilterValidation.validateWaveletLevel(waveletLevel))) return;
	    if (FilterValidation.handleValidation(FilterValidation.validateThreshold(waveletThreshold))) return;
	    if (FilterValidation.handleValidation(FilterValidation.validateWindowSize(sgWindow))) return;
	    if (FilterValidation.handleValidation(FilterValidation.validatePolynomial(sgPoly, sgWindow))) return;

	    analysisFiltersConfig.setPlethBandpassOrder(Integer.parseInt(bpOrder));
	    analysisFiltersConfig.setPlethBandpassLow(Double.parseDouble(bpLow));
	    analysisFiltersConfig.setPlethBandpassHigh(Double.parseDouble(bpHigh));
	    analysisFiltersConfig.setPlethBandpassEnabled(bpEnabled);

	    analysisFiltersConfig.setPlethBandstopOrder(Integer.parseInt(bsOrder));
	    analysisFiltersConfig.setPlethBandstopLow(Double.parseDouble(bsLow));
	    analysisFiltersConfig.setPlethBandstopHigh(Double.parseDouble(bsHigh));
	    analysisFiltersConfig.setPlethBandstopEnabled(bsEnabled);

	    analysisFiltersConfig.setPlethWaveletType(waveletType);
	    analysisFiltersConfig.setPlethWaveletLevel(Integer.parseInt(waveletLevel));
	    analysisFiltersConfig.setPlethWaveletThreshold(Double.parseDouble(waveletThreshold));
	    analysisFiltersConfig.setPlethWaveletSoft(waveletSoft);
	    analysisFiltersConfig.setPlethWaveletEnabled(waveletEnabled);

	    analysisFiltersConfig.setPlethSGWindow(Integer.parseInt(sgWindow));
	    analysisFiltersConfig.setPlethSGPoly(Integer.parseInt(sgPoly));
	    analysisFiltersConfig.setPlethSGEnabled(sgEnabled);

	    analysisFiltersConfig.saveProperties();
	}
	
	private void saveToRealTimeEcg() {
	    String bpOrder = filterBandPassOrderField.getText();
	    String bpLow = filterBandPassLowField.getText();
	    String bpHigh = filterBandPassHighField.getText();
	    boolean bpEnabled = filterBandPassEnabledField.isSelected();

	    String bsOrder = filterBandStopOrderField.getText();
	    String bsLow = filterBandStopLowField.getText();
	    String bsHigh = filterBandStopHighField.getText();
	    boolean bsEnabled = filterBandStopEnabledField.isSelected();

	    if (FilterValidation.handleValidation(FilterValidation.validateOrder(bpOrder))) return;
	    if (FilterValidation.handleValidation(FilterValidation.validateFrequency(bpLow))) return;
	    if (FilterValidation.handleValidation(FilterValidation.validateFrequency(bpHigh))) return;
	    if (FilterValidation.handleValidation(FilterValidation.validateInferiorLessThanSuperior(bpLow, bpHigh))) return;

	    if (FilterValidation.handleValidation(FilterValidation.validateOrder(bsOrder))) return;
	    if (FilterValidation.handleValidation(FilterValidation.validateFrequency(bsLow))) return;
	    if (FilterValidation.handleValidation(FilterValidation.validateFrequency(bsHigh))) return;
	    if (FilterValidation.handleValidation(FilterValidation.validateInferiorLessThanSuperior(bsLow, bsHigh))) return;

	    realTimeFiltersConfig.setEcgBandpassOrder(Integer.parseInt(bpOrder));
	    realTimeFiltersConfig.setEcgBandpassLow(Double.parseDouble(bpLow));
	    realTimeFiltersConfig.setEcgBandpassHigh(Double.parseDouble(bpHigh));
	    realTimeFiltersConfig.setEcgBandpassEnabled(bpEnabled);

	    realTimeFiltersConfig.setEcgBandstopOrder(Integer.parseInt(bsOrder));
	    realTimeFiltersConfig.setEcgBandstopLow(Double.parseDouble(bsLow));
	    realTimeFiltersConfig.setEcgBandstopHigh(Double.parseDouble(bsHigh));
	    realTimeFiltersConfig.setEcgBandstopEnabled(bsEnabled);

	    realTimeFiltersConfig.saveProperties();
	}

	private void saveToRealTimePleth() {
	    String bpOrder = filterBandPassOrderField.getText();
	    String bpLow = filterBandPassLowField.getText();
	    String bpHigh = filterBandPassHighField.getText();
	    boolean bpEnabled = filterBandPassEnabledField.isSelected();

	    String bsOrder = filterBandStopOrderField.getText();
	    String bsLow = filterBandStopLowField.getText();
	    String bsHigh = filterBandStopHighField.getText();
	    boolean bsEnabled = filterBandStopEnabledField.isSelected();

	    if (FilterValidation.handleValidation(FilterValidation.validateOrder(bpOrder))) return;
	    if (FilterValidation.handleValidation(FilterValidation.validateFrequency(bpLow))) return;
	    if (FilterValidation.handleValidation(FilterValidation.validateFrequency(bpHigh))) return;
	    if (FilterValidation.handleValidation(FilterValidation.validateInferiorLessThanSuperior(bpLow, bpHigh))) return;

	    if (FilterValidation.handleValidation(FilterValidation.validateOrder(bsOrder))) return;
	    if (FilterValidation.handleValidation(FilterValidation.validateFrequency(bsLow))) return;
	    if (FilterValidation.handleValidation(FilterValidation.validateFrequency(bsHigh))) return;
	    if (FilterValidation.handleValidation(FilterValidation.validateInferiorLessThanSuperior(bsLow, bsHigh))) return;

	    realTimeFiltersConfig.setPlethBandpassOrder(Integer.parseInt(bpOrder));
	    realTimeFiltersConfig.setPlethBandpassLow(Double.parseDouble(bpLow));
	    realTimeFiltersConfig.setPlethBandpassHigh(Double.parseDouble(bpHigh));
	    realTimeFiltersConfig.setPlethBandpassEnabled(bpEnabled);

	    realTimeFiltersConfig.setPlethBandstopOrder(Integer.parseInt(bsOrder));
	    realTimeFiltersConfig.setPlethBandstopLow(Double.parseDouble(bsLow));
	    realTimeFiltersConfig.setPlethBandstopHigh(Double.parseDouble(bsHigh));
	    realTimeFiltersConfig.setPlethBandstopEnabled(bsEnabled);

	    realTimeFiltersConfig.saveProperties();
	}
	
	private void initRadioButtons() {
	    filterBandPassFIRRadioBtn.setOnAction(e -> updateFilterVisibility());
	    filterBandPassIIRRadioBtn.setOnAction(e -> updateFilterVisibility());
	    filterBandStopFIRRadioBtn.setOnAction(e -> updateFilterVisibility());
	    filterBandStopIIRRadioBtn.setOnAction(e -> updateFilterVisibility());
	    
	    updateFilterVisibility();

	}
	
	private void updateFilterVisibility() {
	    // Pasa bandas
	    bandPassFirBox.setVisible(filterBandPassFIRRadioBtn.isSelected());
	    bandPassIirBox.setVisible(filterBandPassIIRRadioBtn.isSelected());

	    // Rechaza bandas
	    bandStopFirBox.setVisible(filterBandStopFIRRadioBtn.isSelected());
	    bandStopIirBox.setVisible(filterBandStopIIRRadioBtn.isSelected());
	}
}
