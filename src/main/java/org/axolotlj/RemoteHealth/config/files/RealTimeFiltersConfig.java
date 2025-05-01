package org.axolotlj.RemoteHealth.config.files;

import org.axolotlj.RemoteHealth.config.ConfigFileHelper;
import org.axolotlj.RemoteHealth.config.util.BaseFiltersConfig;
import org.axolotlj.RemoteHealth.config.util.ConfigProperty;

public class RealTimeFiltersConfig extends BaseFiltersConfig {

	public RealTimeFiltersConfig() {
		super(ConfigFileHelper.getConfigDir().resolve("real_time_filters.properties"));
		loadProperties();
	}

	@ConfigProperty("ecg.bandpass.order")
	private int ecgBandpassOrder = 4;
	@ConfigProperty("ecg.bandpass.low")
	private double ecgBandpassLow = 20.0;
	@ConfigProperty("ecg.bandpass.high")
	private double ecgBandpassHigh = 39.0;
	@ConfigProperty("ecg.bandpass.enabled")
	private boolean ecgBandpassEnabled = true;

	@ConfigProperty("ecg.bandstop.order")
	private int ecgBandstopOrder = 2;
	@ConfigProperty("ecg.bandstop.low")
	private double ecgBandstopLow = 4.0;
	@ConfigProperty("ecg.bandstop.high")
	private double ecgBandstopHigh = 60.0;
	@ConfigProperty("ecg.bandstop.enabled")
	private boolean ecgBandstopEnabled = true;

	@ConfigProperty("pleth.bandpass.order")
	private int plethBandpassOrder = 2;
	@ConfigProperty("pleth.bandpass.low")
	private double plethBandpassLow = 2.5;
	@ConfigProperty("pleth.bandpass.high")
	private double plethBandpassHigh = 4.5;
	@ConfigProperty("pleth.bandpass.enabled")
	private boolean plethBandpassEnabled = true;

	@ConfigProperty("pleth.bandstop.order")
	private int plethBandstopOrder = 2;
	@ConfigProperty("pleth.bandstop.low")
	private double plethBandstopLow = 4.0;
	@ConfigProperty("pleth.bandstop.high")
	private double plethBandstopHigh = 60.0;
	@ConfigProperty("pleth.bandstop.enabled")
	private boolean plethBandstopEnabled = true;

	public void restoreEcgDefaults() {
		this.ecgBandpassOrder = 4;
		this.ecgBandpassLow = 20.0;
		this.ecgBandpassHigh = 39.0;
		this.ecgBandpassEnabled = true;

		this.ecgBandstopOrder = 2;
		this.ecgBandstopLow = 4.0;
		this.ecgBandstopHigh = 60.0;
		this.ecgBandstopEnabled = true;
		saveProperties();
	}

	public void restorePlethDefaults() {
		this.plethBandpassOrder = 2;
		this.plethBandpassLow = 2.5;
		this.plethBandpassHigh = 4.5;
		this.plethBandpassEnabled = true;

		this.plethBandstopOrder = 2;
		this.plethBandstopLow = 4.0;
		this.plethBandstopHigh = 60.0;
		this.plethBandstopEnabled = true;
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
