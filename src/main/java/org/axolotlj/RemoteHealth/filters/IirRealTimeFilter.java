package org.axolotlj.RemoteHealth.filters;

import org.axolotlj.RemoteHealth.config.filt.RealTimeFiltersConfig;

import uk.me.berndporr.iirj.Butterworth;

public class IirRealTimeFilter {

	private final Butterworth filterBandPassECG = new Butterworth();
	private final Butterworth filterBandstopECG = new Butterworth();
	private final Butterworth filterBandPassIr = new Butterworth();
	private final Butterworth filterBandstopIr = new Butterworth();
	private final Butterworth filterBandPassRed = new Butterworth();
	private final Butterworth filterBandstopRed = new Butterworth();

	private final boolean isEnabledEcgBandPass;
	private final boolean isEnabledEcgBandStop;

	private final boolean isEnabledPlethBandPass;
	private final boolean isEnabledPlethBandStop;
	
	public IirRealTimeFilter(double sampleRateECG, double sampleRatePleth) {
		RealTimeFiltersConfig cfg = new RealTimeFiltersConfig();
		cfg.loadProperties();
		isEnabledEcgBandPass = cfg.isEcgBandpassEnabled();
		isEnabledEcgBandStop = cfg.isEcgBandstopEnabled();
		
		isEnabledPlethBandPass = cfg.isPlethBandpassEnabled();
		isEnabledPlethBandStop = cfg.isPlethBandstopEnabled();
		
		filterBandPassECG.bandPass(cfg.getEcgBandpassOrder(), sampleRateECG, cfg.getEcgBandpassLow(),
				cfg.getEcgBandpassHigh());
		filterBandstopECG.bandStop(cfg.getEcgBandstopOrder(), sampleRateECG, cfg.getEcgBandstopHigh(), cfg.getEcgBandstopLow());

		filterBandPassIr.bandPass(cfg.getPlethBandpassOrder(), sampleRatePleth, cfg.getPlethBandpassLow(),
				cfg.getPlethBandpassHigh());
		filterBandstopIr.bandStop(cfg.getPlethBandstopOrder(), sampleRatePleth, cfg.getPlethBandstopHigh(), cfg.getPlethBandstopLow());

		filterBandPassRed.bandPass(cfg.getPlethBandpassOrder(), sampleRatePleth, cfg.getPlethBandpassLow(),
				cfg.getPlethBandpassHigh());
		filterBandstopRed.bandStop(cfg.getPlethBandstopOrder(), sampleRatePleth, cfg.getPlethBandstopHigh(), cfg.getPlethBandstopLow());
	}

	public double filterECG(double value) {
		double filteredValue = value;
		if(isEnabledEcgBandPass) {
			filteredValue = filterBandPassECG.filter(filteredValue);
		}
		
		if(isEnabledEcgBandStop) {
			filteredValue = filterBandstopECG.filter(filteredValue);
		}
		
		return filteredValue;
	}

	public double filterIr(double value) {
		double filteredValue = value;
		if(isEnabledPlethBandPass) {
			filteredValue = filterBandPassIr.filter(filteredValue);
		}
		
		if(isEnabledPlethBandStop) {
			filteredValue = filterBandstopIr.filter(filteredValue);
		}
		
		return filteredValue;
	}

	public double filterRed(double value) {
		double filteredValue = value;
		if(isEnabledPlethBandPass) {
			filteredValue = filterBandPassRed.filter(filteredValue);
		}
		
		if(isEnabledPlethBandStop) {
			filteredValue = filterBandstopRed.filter(filteredValue);
		}
		
		return filteredValue;
	}
}
