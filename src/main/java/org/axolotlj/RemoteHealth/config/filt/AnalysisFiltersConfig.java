package org.axolotlj.RemoteHealth.config.filt;

import org.axolotlj.RemoteHealth.config.ConfigFileHelper;
import org.axolotlj.RemoteHealth.config.filt.base.BaseFiltersConfig;
import org.axolotlj.RemoteHealth.config.filt.base.ConfigProperty;
import org.axolotlj.RemoteHealth.config.filt.defaults.DefaultAnalysisEcg;
import org.axolotlj.RemoteHealth.config.filt.defaults.DefaultAnalysisPleth;

public class AnalysisFiltersConfig extends BaseFiltersConfig {

	public AnalysisFiltersConfig() {
		super(ConfigFileHelper.getConfigDir().resolve("analysis_filters.properties"));
		loadProperties();
	}

	// --- ECG Bandpass ---
	@ConfigProperty("ecg.bandpass.order")
	private int ecgBandpassOrder = DefaultAnalysisEcg.ECG_BANDPASS_ORDER;
	@ConfigProperty("ecg.bandpass.low")
	private double ecgBandpassLow = DefaultAnalysisEcg.ECG_BANDPASS_LOW;
	@ConfigProperty("ecg.bandpass.high")
	private double ecgBandpassHigh = DefaultAnalysisEcg.ECG_BANDPASS_HIGH;
	@ConfigProperty("ecg.bandpass.enabled")
	private boolean ecgBandpassEnabled = DefaultAnalysisEcg.ECG_BANDPASS_ENABLED;

	// --- ECG Bandstop ---
	@ConfigProperty("ecg.bandstop.order")
	private int ecgBandstopOrder = DefaultAnalysisEcg.ECG_BANDSTOP_ORDER;
	@ConfigProperty("ecg.bandstop.low")
	private double ecgBandstopLow = DefaultAnalysisEcg.ECG_BANDSTOP_LOW;
	@ConfigProperty("ecg.bandstop.high")
	private double ecgBandstopHigh = DefaultAnalysisEcg.ECG_BANDSTOP_HIGH;
	@ConfigProperty("ecg.bandstop.enabled")
	private boolean ecgBandstopEnabled = DefaultAnalysisEcg.ECG_BANDSTOP_ENABLED;

	// --- ECG Wavelet ---
	@ConfigProperty("ecg.wavelet.type")
	private String ecgWaveletType = DefaultAnalysisEcg.ECG_WAVELET_TYPE;
	@ConfigProperty("ecg.wavelet.level")
	private int ecgWaveletLevel = DefaultAnalysisEcg.ECG_WAVELET_LEVEL;
	@ConfigProperty("ecg.wavelet.threshold")
	private double ecgWaveletThreshold = DefaultAnalysisEcg.ECG_WAVELET_THRESHOLD;
	@ConfigProperty("ecg.wavelet.soft")
	private boolean ecgWaveletSoft = DefaultAnalysisEcg.ECG_WAVELET_SOFT;
	@ConfigProperty("ecg.wavelet.enabled")
	private boolean ecgWaveletEnabled = DefaultAnalysisEcg.ECG_WAVELET_ENABLED;

	// --- ECG Savitzky-Golay ---
	@ConfigProperty("ecg.sg.window")
	private int ecgSGWindow = DefaultAnalysisEcg.ECG_SG_WINDOW;
	@ConfigProperty("ecg.sg.poly")
	private int ecgSGPoly = DefaultAnalysisEcg.ECG_SG_POLY;
	@ConfigProperty("ecg.sg.enabled")
	private boolean ecgSGEnabled = DefaultAnalysisEcg.ECG_SG_ENABLED;

	// --- Pleth Bandpass ---
	@ConfigProperty("pleth.bandpass.order")
	private int plethBandpassOrder = DefaultAnalysisPleth.PLETH_BANDPASS_ORDER;
	@ConfigProperty("pleth.bandpass.low")
	private double plethBandpassLow = DefaultAnalysisPleth.PLETH_BANDPASS_LOW;
	@ConfigProperty("pleth.bandpass.high")
	private double plethBandpassHigh = DefaultAnalysisPleth.PLETH_BANDPASS_HIGH;
	@ConfigProperty("pleth.bandpass.enabled")
	private boolean plethBandpassEnabled = DefaultAnalysisPleth.PLETH_BANDPASS_ENABLED;

	// --- Pleth Bandstop ---
	@ConfigProperty("pleth.bandstop.order")
	private int plethBandstopOrder = DefaultAnalysisPleth.PLETH_BANDSTOP_ORDER;
	@ConfigProperty("pleth.bandstop.low")
	private double plethBandstopLow = DefaultAnalysisPleth.PLETH_BANDSTOP_LOW;
	@ConfigProperty("pleth.bandstop.high")
	private double plethBandstopHigh = DefaultAnalysisPleth.PLETH_BANDSTOP_HIGH;
	@ConfigProperty("pleth.bandstop.enabled")
	private boolean plethBandstopEnabled = DefaultAnalysisPleth.PLETH_BANDSTOP_ENABLED;

	// --- Pleth Wavelet ---
	@ConfigProperty("pleth.wavelet.type")
	private String plethWaveletType = DefaultAnalysisPleth.PLETH_WAVELET_TYPE;
	@ConfigProperty("pleth.wavelet.level")
	private int plethWaveletLevel = DefaultAnalysisPleth.PLETH_WAVELET_LEVEL;
	@ConfigProperty("pleth.wavelet.threshold")
	private double plethWaveletThreshold = DefaultAnalysisPleth.PLETH_WAVELET_THRESHOLD;
	@ConfigProperty("pleth.wavelet.soft")
	private boolean plethWaveletSoft = DefaultAnalysisPleth.PLETH_WAVELET_SOFT;
	@ConfigProperty("pleth.wavelet.enabled")
	private boolean plethWaveletEnabled = DefaultAnalysisPleth.PLETH_WAVELET_ENABLED;

	// --- Pleth Savitzky-Golay ---
	@ConfigProperty("pleth.sg.window")
	private int plethSGWindow = DefaultAnalysisPleth.PLETH_SG_WINDOW;
	@ConfigProperty("pleth.sg.poly")
	private int plethSGPoly = DefaultAnalysisPleth.PLETH_SG_POLY;
	@ConfigProperty("pleth.sg.enabled")
	private boolean plethSGEnabled = DefaultAnalysisPleth.PLETH_SG_ENABLED;

	// --- Métodos de Restauración ---

	public void restoreEcgDefaults() {
		ecgBandpassOrder = DefaultAnalysisEcg.ECG_BANDPASS_ORDER;
		ecgBandpassLow = DefaultAnalysisEcg.ECG_BANDPASS_LOW;
		ecgBandpassHigh = DefaultAnalysisEcg.ECG_BANDPASS_HIGH;
		ecgBandpassEnabled = DefaultAnalysisEcg.ECG_BANDPASS_ENABLED;

		ecgBandstopOrder = DefaultAnalysisEcg.ECG_BANDSTOP_ORDER;
		ecgBandstopLow = DefaultAnalysisEcg.ECG_BANDSTOP_LOW;
		ecgBandstopHigh = DefaultAnalysisEcg.ECG_BANDSTOP_HIGH;
		ecgBandstopEnabled = DefaultAnalysisEcg.ECG_BANDSTOP_ENABLED;

		ecgWaveletType = DefaultAnalysisEcg.ECG_WAVELET_TYPE;
		ecgWaveletLevel = DefaultAnalysisEcg.ECG_WAVELET_LEVEL;
		ecgWaveletThreshold = DefaultAnalysisEcg.ECG_WAVELET_THRESHOLD;
		ecgWaveletSoft = DefaultAnalysisEcg.ECG_WAVELET_SOFT;
		ecgWaveletEnabled = DefaultAnalysisEcg.ECG_WAVELET_ENABLED;

		ecgSGWindow = DefaultAnalysisEcg.ECG_SG_WINDOW;
		ecgSGPoly = DefaultAnalysisEcg.ECG_SG_POLY;
		ecgSGEnabled = DefaultAnalysisEcg.ECG_SG_ENABLED;

		saveProperties();
	}

	public void restorePlethDefaults() {
		plethBandpassOrder = DefaultAnalysisPleth.PLETH_BANDPASS_ORDER;
		plethBandpassLow = DefaultAnalysisPleth.PLETH_BANDPASS_LOW;
		plethBandpassHigh = DefaultAnalysisPleth.PLETH_BANDPASS_HIGH;
		plethBandpassEnabled = DefaultAnalysisPleth.PLETH_BANDPASS_ENABLED;

		plethBandstopOrder = DefaultAnalysisPleth.PLETH_BANDSTOP_ORDER;
		plethBandstopLow = DefaultAnalysisPleth.PLETH_BANDSTOP_LOW;
		plethBandstopHigh = DefaultAnalysisPleth.PLETH_BANDSTOP_HIGH;
		plethBandstopEnabled = DefaultAnalysisPleth.PLETH_BANDSTOP_ENABLED;

		plethWaveletType = DefaultAnalysisPleth.PLETH_WAVELET_TYPE;
		plethWaveletLevel = DefaultAnalysisPleth.PLETH_WAVELET_LEVEL;
		plethWaveletThreshold = DefaultAnalysisPleth.PLETH_WAVELET_THRESHOLD;
		plethWaveletSoft = DefaultAnalysisPleth.PLETH_WAVELET_SOFT;
		plethWaveletEnabled = DefaultAnalysisPleth.PLETH_WAVELET_ENABLED;

		plethSGWindow = DefaultAnalysisPleth.PLETH_SG_WINDOW;
		plethSGPoly = DefaultAnalysisPleth.PLETH_SG_POLY;
		plethSGEnabled = DefaultAnalysisPleth.PLETH_SG_ENABLED;

		saveProperties();
	}

	// --- Getters (solo importantes para mostrar/leer) ---

	public int getEcgBandpassOrder() {
		return ecgBandpassOrder;
	}

	public double getEcgBandpassLow() {
		return ecgBandpassLow;
	}

	public double getEcgBandpassHigh() {
		return ecgBandpassHigh;
	}

	public boolean isEcgBandpassEnabled() {
		return ecgBandpassEnabled;
	}

	public int getEcgBandstopOrder() {
		return ecgBandstopOrder;
	}

	public double getEcgBandstopLow() {
		return ecgBandstopLow;
	}

	public double getEcgBandstopHigh() {
		return ecgBandstopHigh;
	}

	public boolean isEcgBandstopEnabled() {
		return ecgBandstopEnabled;
	}

	public String getEcgWaveletType() {
		return ecgWaveletType;
	}

	public int getEcgWaveletLevel() {
		return ecgWaveletLevel;
	}

	public double getEcgWaveletThreshold() {
		return ecgWaveletThreshold;
	}

	public boolean isEcgWaveletSoft() {
		return ecgWaveletSoft;
	}

	public boolean isEcgWaveletEnabled() {
		return ecgWaveletEnabled;
	}

	public int getEcgSGWindow() {
		return ecgSGWindow;
	}

	public int getEcgSGPoly() {
		return ecgSGPoly;
	}

	public boolean isEcgSGEnabled() {
		return ecgSGEnabled;
	}

	public int getPlethBandpassOrder() {
		return plethBandpassOrder;
	}

	public double getPlethBandpassLow() {
		return plethBandpassLow;
	}

	public double getPlethBandpassHigh() {
		return plethBandpassHigh;
	}

	public boolean isPlethBandpassEnabled() {
		return plethBandpassEnabled;
	}

	public int getPlethBandstopOrder() {
		return plethBandstopOrder;
	}

	public double getPlethBandstopLow() {
		return plethBandstopLow;
	}

	public double getPlethBandstopHigh() {
		return plethBandstopHigh;
	}

	public boolean isPlethBandstopEnabled() {
		return plethBandstopEnabled;
	}

	public String getPlethWaveletType() {
		return plethWaveletType;
	}

	public int getPlethWaveletLevel() {
		return plethWaveletLevel;
	}

	public double getPlethWaveletThreshold() {
		return plethWaveletThreshold;
	}

	public boolean isPlethWaveletSoft() {
		return plethWaveletSoft;
	}

	public boolean isPlethWaveletEnabled() {
		return plethWaveletEnabled;
	}

	public int getPlethSGWindow() {
		return plethSGWindow;
	}

	public int getPlethSGPoly() {
		return plethSGPoly;
	}

	public boolean isPlethSGEnabled() {
		return plethSGEnabled;
	}

	// --- Setters ---

	// ECG Bandpass
	public void setEcgBandpassOrder(int value) {
		this.ecgBandpassOrder = value;
	}

	public void setEcgBandpassLow(double value) {
		this.ecgBandpassLow = value;
	}

	public void setEcgBandpassHigh(double value) {
		this.ecgBandpassHigh = value;
	}

	public void setEcgBandpassEnabled(boolean value) {
		this.ecgBandpassEnabled = value;
	}

	// ECG Bandstop
	public void setEcgBandstopOrder(int value) {
		this.ecgBandstopOrder = value;
	}

	public void setEcgBandstopLow(double value) {
		this.ecgBandstopLow = value;
	}

	public void setEcgBandstopHigh(double value) {
		this.ecgBandstopHigh = value;
	}

	public void setEcgBandstopEnabled(boolean value) {
		this.ecgBandstopEnabled = value;
	}

	// ECG Wavelet
	public void setEcgWaveletType(String value) {
		this.ecgWaveletType = value;
	}

	public void setEcgWaveletLevel(int value) {
		this.ecgWaveletLevel = value;
	}

	public void setEcgWaveletThreshold(double value) {
		this.ecgWaveletThreshold = value;
	}

	public void setEcgWaveletSoft(boolean value) {
		this.ecgWaveletSoft = value;
	}

	public void setEcgWaveletEnabled(boolean value) {
		this.ecgWaveletEnabled = value;
	}

	// ECG Savitzky-Golay
	public void setEcgSGWindow(int value) {
		this.ecgSGWindow = value;
	}

	public void setEcgSGPoly(int value) {
		this.ecgSGPoly = value;
	}

	public void setEcgSGEnabled(boolean value) {
		this.ecgSGEnabled = value;
	}

	// Pleth Bandpass
	public void setPlethBandpassOrder(int value) {
		this.plethBandpassOrder = value;
	}

	public void setPlethBandpassLow(double value) {
		this.plethBandpassLow = value;
	}

	public void setPlethBandpassHigh(double value) {
		this.plethBandpassHigh = value;
	}

	public void setPlethBandpassEnabled(boolean value) {
		this.plethBandpassEnabled = value;
	}

	// Pleth Bandstop
	public void setPlethBandstopOrder(int value) {
		this.plethBandstopOrder = value;
	}

	public void setPlethBandstopLow(double value) {
		this.plethBandstopLow = value;
	}

	public void setPlethBandstopHigh(double value) {
		this.plethBandstopHigh = value;
	}

	public void setPlethBandstopEnabled(boolean value) {
		this.plethBandstopEnabled = value;
	}

	// Pleth Wavelet
	public void setPlethWaveletType(String value) {
		this.plethWaveletType = value;
	}

	public void setPlethWaveletLevel(int value) {
		this.plethWaveletLevel = value;
	}

	public void setPlethWaveletThreshold(double value) {
		this.plethWaveletThreshold = value;
	}

	public void setPlethWaveletSoft(boolean value) {
		this.plethWaveletSoft = value;
	}

	public void setPlethWaveletEnabled(boolean value) {
		this.plethWaveletEnabled = value;
	}

	// Pleth Savitzky-Golay
	public void setPlethSGWindow(int value) {
		this.plethSGWindow = value;
	}

	public void setPlethSGPoly(int value) {
		this.plethSGPoly = value;
	}

	public void setPlethSGEnabled(boolean value) {
		this.plethSGEnabled = value;
	}

}
