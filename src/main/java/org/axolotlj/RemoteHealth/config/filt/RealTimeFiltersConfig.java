package org.axolotlj.RemoteHealth.config.filt;

import org.axolotlj.RemoteHealth.config.ConfigFileHelper;
import org.axolotlj.RemoteHealth.config.filt.base.BaseFiltersConfig;
import org.axolotlj.RemoteHealth.config.filt.base.ConfigProperty;
import org.axolotlj.RemoteHealth.config.filt.defaults.DefaultRealTimeEcg;
import org.axolotlj.RemoteHealth.config.filt.defaults.DefaultRealTimePleth;

public class RealTimeFiltersConfig extends BaseFiltersConfig {

	public RealTimeFiltersConfig() {
		super(ConfigFileHelper.getConfigDir().resolve("real_time_filters.properties"));
		loadProperties();
	}

	// --- ECG Bandpass ---
	@ConfigProperty("ecg.bandpass.order")
	private int ecgBandpassOrder = DefaultRealTimeEcg.ECG_BANDPASS_ORDER;
	@ConfigProperty("ecg.bandpass.low")
	private double ecgBandpassLow = DefaultRealTimeEcg.ECG_BANDPASS_LOW;
	@ConfigProperty("ecg.bandpass.high")
	private double ecgBandpassHigh = DefaultRealTimeEcg.ECG_BANDPASS_HIGH;
	@ConfigProperty("ecg.bandpass.enabled")
	private boolean ecgBandpassEnabled = DefaultRealTimeEcg.ECG_BANDPASS_ENABLED;

	// --- ECG Bandstop ---
	@ConfigProperty("ecg.bandstop.order")
	private int ecgBandstopOrder = DefaultRealTimeEcg.ECG_BANDSTOP_ORDER;
	@ConfigProperty("ecg.bandstop.low")
	private double ecgBandstopLow = DefaultRealTimeEcg.ECG_BANDSTOP_LOW;
	@ConfigProperty("ecg.bandstop.high")
	private double ecgBandstopHigh = DefaultRealTimeEcg.ECG_BANDSTOP_HIGH;
	@ConfigProperty("ecg.bandstop.enabled")
	private boolean ecgBandstopEnabled = DefaultRealTimeEcg.ECG_BANDSTOP_ENABLED;

	// --- Pleth Bandpass ---
	@ConfigProperty("pleth.bandpass.order")
	private int plethBandpassOrder = DefaultRealTimePleth.PLETH_BANDPASS_ORDER;
	@ConfigProperty("pleth.bandpass.low")
	private double plethBandpassLow = DefaultRealTimePleth.PLETH_BANDPASS_LOW;
	@ConfigProperty("pleth.bandpass.high")
	private double plethBandpassHigh = DefaultRealTimePleth.PLETH_BANDPASS_HIGH;
	@ConfigProperty("pleth.bandpass.enabled")
	private boolean plethBandpassEnabled = DefaultRealTimePleth.PLETH_BANDPASS_ENABLED;

	// --- Pleth Bandstop ---
	@ConfigProperty("pleth.bandstop.order")
	private int plethBandstopOrder = DefaultRealTimePleth.PLETH_BANDSTOP_ORDER;
	@ConfigProperty("pleth.bandstop.low")
	private double plethBandstopLow = DefaultRealTimePleth.PLETH_BANDSTOP_LOW;
	@ConfigProperty("pleth.bandstop.high")
	private double plethBandstopHigh = DefaultRealTimePleth.PLETH_BANDSTOP_HIGH;
	@ConfigProperty("pleth.bandstop.enabled")
	private boolean plethBandstopEnabled = DefaultRealTimePleth.PLETH_BANDSTOP_ENABLED;

	// --- Restaurar valores por defecto ---

	public void restoreEcgDefaults() {
		this.ecgBandpassOrder = DefaultRealTimeEcg.ECG_BANDPASS_ORDER;
		this.ecgBandpassLow = DefaultRealTimeEcg.ECG_BANDPASS_LOW;
		this.ecgBandpassHigh = DefaultRealTimeEcg.ECG_BANDPASS_HIGH;
		this.ecgBandpassEnabled = DefaultRealTimeEcg.ECG_BANDPASS_ENABLED;

		this.ecgBandstopOrder = DefaultRealTimeEcg.ECG_BANDSTOP_ORDER;
		this.ecgBandstopLow = DefaultRealTimeEcg.ECG_BANDSTOP_LOW;
		this.ecgBandstopHigh = DefaultRealTimeEcg.ECG_BANDSTOP_HIGH;
		this.ecgBandstopEnabled = DefaultRealTimeEcg.ECG_BANDSTOP_ENABLED;

		saveProperties();
	}

	public void restorePlethDefaults() {
		this.plethBandpassOrder = DefaultRealTimePleth.PLETH_BANDPASS_ORDER;
		this.plethBandpassLow = DefaultRealTimePleth.PLETH_BANDPASS_LOW;
		this.plethBandpassHigh = DefaultRealTimePleth.PLETH_BANDPASS_HIGH;
		this.plethBandpassEnabled = DefaultRealTimePleth.PLETH_BANDPASS_ENABLED;

		this.plethBandstopOrder = DefaultRealTimePleth.PLETH_BANDSTOP_ORDER;
		this.plethBandstopLow = DefaultRealTimePleth.PLETH_BANDSTOP_LOW;
		this.plethBandstopHigh = DefaultRealTimePleth.PLETH_BANDSTOP_HIGH;
		this.plethBandstopEnabled = DefaultRealTimePleth.PLETH_BANDSTOP_ENABLED;

		saveProperties();
	}

	// --- Getters ---

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

	// --- Setters ---

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
}
