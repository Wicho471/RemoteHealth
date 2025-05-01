package org.axolotlj.RemoteHealth.config.files;

import org.axolotlj.RemoteHealth.config.ConfigFileHelper;
import org.axolotlj.RemoteHealth.config.util.BaseFiltersConfig;
import org.axolotlj.RemoteHealth.config.util.ConfigProperty;

public class AnalysisFiltersConfig extends BaseFiltersConfig {

	public AnalysisFiltersConfig() {
		super(ConfigFileHelper.getConfigDir().resolve("analysis_filters.properties"));
		loadProperties();
	}

	// --- ECG Bandpass ---
	@ConfigProperty("ecg.bandpass.order")
	private int ecgBandpassOrder = 4;
	@ConfigProperty("ecg.bandpass.low")
	private double ecgBandpassLow = 0.5;
	@ConfigProperty("ecg.bandpass.high")
	private double ecgBandpassHigh = 40.0;
	@ConfigProperty("ecg.bandpass.enabled")
	private boolean ecgBandpassEnabled = true;

	// --- ECG Bandstop ---
	@ConfigProperty("ecg.bandstop.order")
	private int ecgBandstopOrder = 4;
	@ConfigProperty("ecg.bandstop.low")
	private double ecgBandstopLow = 59.0;
	@ConfigProperty("ecg.bandstop.high")
	private double ecgBandstopHigh = 61.0;
	@ConfigProperty("ecg.bandstop.enabled")
	private boolean ecgBandstopEnabled = true;

	// --- ECG Wavelet ---
	@ConfigProperty("ecg.wavelet.type")
	private String ecgWaveletType = "Daubechies 2";
	@ConfigProperty("ecg.wavelet.level")
	private int ecgWaveletLevel = 4;
	@ConfigProperty("ecg.wavelet.threshold")
	private double ecgWaveletThreshold = 0.2;
	@ConfigProperty("ecg.wavelet.soft")
	private boolean ecgWaveletSoft = true;
	@ConfigProperty("ecg.wavelet.enabled")
	private boolean ecgWaveletEnabled = true;

	// --- ECG Savitzky-Golay ---
	@ConfigProperty("ecg.sg.window")
	private int ecgSGWindow = 9;
	@ConfigProperty("ecg.sg.poly")
	private int ecgSGPoly = 3;
	@ConfigProperty("ecg.sg.enabled")
	private boolean ecgSGEnabled = true;

	// --- Pleth Bandpass ---
	@ConfigProperty("pleth.bandpass.order")
	private int plethBandpassOrder = 4;
	@ConfigProperty("pleth.bandpass.low")
	private double plethBandpassLow = 0.5;
	@ConfigProperty("pleth.bandpass.high")
	private double plethBandpassHigh = 5.0;
	@ConfigProperty("pleth.bandpass.enabled")
	private boolean plethBandpassEnabled = true;

	// --- Pleth Bandstop ---
	@ConfigProperty("pleth.bandstop.order")
	private int plethBandstopOrder = 4;
	@ConfigProperty("pleth.bandstop.low")
	private double plethBandstopLow = 59.0;
	@ConfigProperty("pleth.bandstop.high")
	private double plethBandstopHigh = 61.0;
	@ConfigProperty("pleth.bandstop.enabled")
	private boolean plethBandstopEnabled = true;

	// --- Pleth Wavelet ---
	@ConfigProperty("pleth.wavelet.type")
	private String plethWaveletType = "Daubechies 2";
	@ConfigProperty("pleth.wavelet.level")
	private int plethWaveletLevel = 4;
	@ConfigProperty("pleth.wavelet.threshold")
	private double plethWaveletThreshold = 0.1;
	@ConfigProperty("pleth.wavelet.soft")
	private boolean plethWaveletSoft = true;
	@ConfigProperty("pleth.wavelet.enabled")
	private boolean plethWaveletEnabled = true;

	// --- Pleth Savitzky-Golay ---
	@ConfigProperty("pleth.sg.window")
	private int plethSGWindow = 9;
	@ConfigProperty("pleth.sg.poly")
	private int plethSGPoly = 3;
	@ConfigProperty("pleth.sg.enabled")
	private boolean plethSGEnabled = true;

	// --- Métodos de Restauración ---

	public void restoreEcgDefaults() {
		ecgBandpassOrder = 4;
		ecgBandpassLow = 0.5;
		ecgBandpassHigh = 40.0;
		ecgBandpassEnabled = true;

		ecgBandstopOrder = 4;
		ecgBandstopLow = 59.0;
		ecgBandstopHigh = 61.0;
		ecgBandstopEnabled = true;

		ecgWaveletType = "Daubechies 2";
		ecgWaveletLevel = 4;
		ecgWaveletThreshold = 0.2;
		ecgWaveletSoft = true;
		ecgWaveletEnabled = true;

		ecgSGWindow = 9;
		ecgSGPoly = 3;
		ecgSGEnabled = true;

		saveProperties();
	}

	public void restorePlethDefaults() {
		plethBandpassOrder = 4;
		plethBandpassLow = 0.5;
		plethBandpassHigh = 5.0;
		plethBandpassEnabled = true;

		plethBandstopOrder = 4;
		plethBandstopLow = 59.0;
		plethBandstopHigh = 61.0;
		plethBandstopEnabled = true;

		plethWaveletType = "Daubechies 2";
		plethWaveletLevel = 4;
		plethWaveletThreshold = 0.1;
		plethWaveletSoft = true;
		plethWaveletEnabled = true;

		plethSGWindow = 9;
		plethSGPoly = 3;
		plethSGEnabled = true;

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
