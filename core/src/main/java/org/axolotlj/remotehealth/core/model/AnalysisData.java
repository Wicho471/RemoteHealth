package org.axolotlj.remotehealth.core.model;

import java.util.ArrayList;

import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.MutableTriple;
import org.axolotlj.remotehealth.core.analysis.bp.core.BPMonitor;
import org.axolotlj.remotehealth.core.analysis.hr.HrMonitor;
import org.axolotlj.remotehealth.core.analysis.spo2.Spo2Monitor;
import org.axolotlj.remotehealth.core.filters.AnalysisFilters;
import org.axolotlj.remotehealth.core.filters.base.Misc;
import org.axolotlj.remotehealth.core.logger.Log;
import org.axolotlj.remotehealth.core.logger.api.DataLogger;
import org.axolotlj.remotehealth.core.sensor.TuplaUtil;
import org.axolotlj.remotehealth.core.sensor.correction.DataCorrectionService;
import org.axolotlj.remotehealth.core.sensor.data.DataPoint;
import org.axolotlj.remotehealth.core.sensor.handle.DataExtractor;
import org.axolotlj.remotehealth.core.sensor.handle.SensorField;

public class AnalysisData {

	private final DataLogger dataLogger = Log.get();
	private final AnalysisFilters analysisFilters;

	private ArrayList<DataPoint> dataPoints;
	private ArrayList<MutablePair<Long, Double>> egc;
	private ArrayList<MutableTriple<Long, Double, Double>> pleth;

	private ArrayList<MutablePair<Long, Double>> temp;
	private ArrayList<MutablePair<Long, Double>> mov;

	private ArrayList<MutablePair<Long, Integer>> spo2;
	private ArrayList<MutablePair<Long, Integer>> hr;
	private ArrayList<MutableTriple<Long, Double, Double>> bp;

	private double fs;

	public AnalysisData(ArrayList<DataPoint> dataPoints) {
		dataLogger.logInfo("Analizando datos...");
		long start = System.currentTimeMillis();
		this.analysisFilters = new AnalysisFilters();
		correct(dataPoints);
		this.dataPoints = dataPoints;
		this.egc = analysisFilters.getFilteredEcgFromRaw(dataPoints);
		this.pleth = analysisFilters.getFilteredPlethFromRaw(dataPoints);
		
		this.temp = DataExtractor.extractValidValues(dataPoints, SensorField.TEMP);
		this.mov = DataExtractor.extractValidValues(dataPoints, SensorField.ACCEL);
		dataLogger.logDebug("Tomó " +(System.currentTimeMillis()-start)+" ms analizar los datos");
	}
	
	public void calculateSigns() {
		this.fs = Misc.calculateAverageSamplingRate(TuplaUtil.extractTimestamps(egc));
		this.hr = calculateHeartRate();
		this.spo2 = calculateSpo2();
		this.bp = calculateBp();
	}

	public ArrayList<MutablePair<Long, Double>> getEgc() {
		return egc;
	}

	public ArrayList<MutableTriple<Long, Double, Double>> getPleth() {
		return pleth;
	}

	public ArrayList<MutablePair<Long, Integer>> getHr() {
		return hr;
	}
	
	public ArrayList<MutablePair<Long, Integer>> getSpo2() {
		return spo2;
	}
	
	public ArrayList<MutablePair<Long, Double>> getMov() {
		return mov;
	}
	
	public ArrayList<MutablePair<Long, Double>> getTemp() {
		return temp;
	}
	
	public ArrayList<MutableTriple<Long, Double, Double>> getBp() {
		return bp;
	}
	
	private ArrayList<MutableTriple<Long, Double, Double>> calculateBp() {
		final long startTime = System.currentTimeMillis();
		final ArrayList<MutableTriple<Long, Double, Double>> result = new ArrayList<>();

		final BPMonitor bpMonitor = new BPMonitor(bp ->
				result.add(MutableTriple.of(bp.getLeft(), bp.getMiddle(), bp.getRight())));
		for (final DataPoint dataPoint : dataPoints) {
			bpMonitor.feedCsvLine(dataPoint.toCsvLine());
		}
		bpMonitor.stop();

		logExecutionTime("BP", startTime, result.size());
		return result;
	}


	private ArrayList<MutablePair<Long, Integer>> calculateSpo2() {
		final long startTime = System.currentTimeMillis();
		final ArrayList<MutablePair<Long, Integer>> result = new ArrayList<>();

		final Spo2Monitor spo2Monitor = new Spo2Monitor(104.0, 14.29, fs, 10.0,
				spo2 -> result.add(MutablePair.of(spo2.getLeft(), spo2.getRight())));
		for (final MutableTriple<Long, Double, Double> sample : pleth) {
			spo2Monitor.addPlethSample(sample.getMiddle(), sample.getRight(), sample.getLeft());
		}
		spo2Monitor.stop();

		logExecutionTime("Spo2", startTime, result.size());
		return result;
	}


	private ArrayList<MutablePair<Long, Integer>> calculateHeartRate() {
		final long startTime = System.currentTimeMillis();
		final ArrayList<MutablePair<Long, Integer>> result = new ArrayList<>();

		final HrMonitor hrMonitor = new HrMonitor(fs, 5, hr -> result.add(MutablePair.of(hr.getLeft(), hr.getRight())));
		for (final MutablePair<Long, Double> sample : egc) {
			hrMonitor.addEcgSample(sample.getRight(), sample.getLeft());
		}
		hrMonitor.stop();

		logExecutionTime("BPM", startTime, result.size());
		return result;
	}


	private void correct(ArrayList<DataPoint> dataPoints) {
		DataCorrectionService correctionService = new DataCorrectionService();
		correctionService.correct(dataPoints);
	}
	
	private void logExecutionTime(String label, long startTime, int resultSize) {
		long endTime = System.currentTimeMillis();
		dataLogger.logDebug("Calculados " + label + " " + resultSize + " en un tiempo de " + (endTime - startTime)
				+ " ms con una cantidad de " + this.egc.size() + " muestras");
	}

	public void clear() {
		if (dataPoints != null && !dataPoints.isEmpty()) {
			dataPoints.clear();
		}
		if (egc != null && !egc.isEmpty()) {
			egc.clear();
		}
		if (pleth != null && !pleth.isEmpty()) {
			pleth.clear();
		}

	}

}
