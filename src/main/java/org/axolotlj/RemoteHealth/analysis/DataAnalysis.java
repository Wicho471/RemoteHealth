package org.axolotlj.RemoteHealth.analysis;

import java.util.ArrayList;

import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.math3.analysis.interpolation.LinearInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;
import org.axolotlj.RemoteHealth.model.ParameterValue;
import org.axolotlj.RemoteHealth.model.SensorValue;
import org.axolotlj.RemoteHealth.model.StructureData;
import org.axolotlj.RemoteHealth.util.DataHandler;
import org.axolotlj.RemoteHealth.util.DataHandler.SensorField;

public class DataAnalysis {

	private ArrayList<ParameterValue> parameterValues;
	private final ArrayList<StructureData> originalData;
	private final ArrayList<StructureData> processedData;

	public DataAnalysis(ArrayList<StructureData> dataList) {
		this.originalData = dataList;
		this.processedData = new ArrayList<>();
		this.parameterValues = new ArrayList<>();
		process();
	}

	private void process() {
		if (originalData == null || originalData.isEmpty()) {
			System.err.println("DataAnalysis -> Lista vacía o nula");
			return;
		}

		// Paso 1: Interpolación
		System.out.println("Interpolando TEMP...");
		ArrayList<MutablePair<Long, Double>> tempPairs = extractValidPairs(originalData, SensorField.TEMP);
		PolynomialSplineFunction tempInterpolator = createInterpolator(tempPairs);

		System.out.println("Interpolando MOV...");
		ArrayList<MutablePair<Long, Double>> movPairs = extractValidPairs(originalData, SensorField.ACCEL);
		PolynomialSplineFunction movInterpolator = createInterpolator(movPairs);

		// Paso 2: Reconstruir StructureData interpolando donde es necesario
		for (StructureData data : originalData) {
			long ts = data.getTimeStamp();

			SensorValue<Float> temp = data.getTemp();
			if (!temp.isValid()) {
				if (tempInterpolator != null) {
					double min = tempInterpolator.getKnots()[0];
					double max = tempInterpolator.getKnots()[tempInterpolator.getN() - 1];
					if (ts >= min && ts <= max) {
						double interpolatedTemp = tempInterpolator.value(ts);
						temp = SensorValue.valid((float) interpolatedTemp);
					}
				}
			}

			SensorValue<Float> mov = data.getAccel();
			if (!mov.isValid()) {
				if (movInterpolator != null) {
					double min = movInterpolator.getKnots()[0];
					double max = movInterpolator.getKnots()[movInterpolator.getN() - 1];
					if (ts >= min && ts <= max) {
						double interpolatedMov = movInterpolator.value(ts);
						mov = SensorValue.valid((float) interpolatedMov);
					}
				}
			}

			// Mantiene los otros datos igual
			StructureData rebuilt = new StructureData(data.getTimeStamp(), data.getEcg(), mov, temp, data.getIr(),
					data.getRed());
			processedData.add(rebuilt);
		}

		// Paso 3: Calcular BPM y SpO2
		calculateParameters();
	}

	private ArrayList<MutablePair<Long, Double>> extractValidPairs(ArrayList<StructureData> dataList,
			SensorField field) {
		ArrayList<MutablePair<Long, Double>> pairs = new ArrayList<>();
		for (StructureData data : dataList) {
			long ts = data.getTimeStamp();
			Double value = getValue(data, field);
			if (value != null && !Double.isNaN(value)) {
				pairs.add(MutablePair.of(ts, value));
			}
		}
		return pairs;
	}

	private PolynomialSplineFunction createInterpolator(ArrayList<MutablePair<Long, Double>> pairs) {
		if (pairs.size() < 2) {
			System.out.println("No hay suficientes datos válidos para interpolar.");
			return null;
		}

		double[] x = new double[pairs.size()];
		double[] y = new double[pairs.size()];

		for (int i = 0; i < pairs.size(); i++) {
			x[i] = pairs.get(i).getLeft();
			y[i] = pairs.get(i).getRight();
		}

		try {
			LinearInterpolator interpolator = new LinearInterpolator();
			return interpolator.interpolate(x, y);
		} catch (Exception e) {
			System.err.println("Error creando interpolador: " + e.getMessage());
			return null;
		}
	}

	private void calculateParameters() {
		// Extraemos IR y RED
		ArrayList<MutablePair<Long, Double>> irPairs = DataHandler.extractValidPairs(processedData, SensorField.IR);
		ArrayList<MutablePair<Long, Double>> redPairs = DataHandler.extractValidPairs(processedData, SensorField.RED);

		// Recorrer todos los datos y calcular los parámetros
		for (int i = 0; i < processedData.size(); i++) {
			StructureData data = processedData.get(i);
			long ts = data.getTimeStamp();

			double temp = data.getTemp().isValid() ? data.getTemp().getValue() : Double.NaN;
			double mov = data.getAccel().isValid() ? data.getAccel().getValue() : Double.NaN;

			double bpm = estimateBPM(i, irPairs);
			double spo2 = calculateSpO2(i, irPairs, redPairs);

			ParameterValue pv = new ParameterValue(ts);
			pv.setTemp(temp);
			pv.setMov(mov);
			pv.setBpm((int) bpm);
			pv.setSpO2(spo2);

			parameterValues.add(pv);
		}
	}

	private Double getValue(StructureData data, SensorField field) {
		switch (field) {
		case TEMP:
			return data.getTemp().isValid() ? (double) data.getTemp().getValue() : null;
		case ACCEL:
			return data.getAccel().isValid() ? (double) data.getAccel().getValue() : null;
		default:
			return null;
		}
	}

	private double estimateBPM(int index, ArrayList<MutablePair<Long, Double>> irPairs) {
		if (irPairs == null || irPairs.size() < 3)
			return 0;

		int windowSize = 100; // ajusta según sampling rate
		int start = Math.max(0, index - windowSize / 2);
		int end = Math.min(irPairs.size() - 1, index + windowSize / 2);

		ArrayList<Double> window = new ArrayList<>();
		for (int i = start; i <= end; i++) {
			window.add(irPairs.get(i).getRight());
		}

		return countPeaks(window) * (60.0 / 10.0); // aproximado para 10s
	}

	private int countPeaks(ArrayList<Double> data) {
		if (data.size() < 3)
			return 0;
		int peaks = 0;
		for (int i = 1; i < data.size() - 1; i++) {
			double prev = data.get(i - 1);
			double curr = data.get(i);
			double next = data.get(i + 1);
			if (curr > prev && curr > next) {
				peaks++;
			}
		}
		return peaks;
	}

	private double calculateSpO2(int index, ArrayList<MutablePair<Long, Double>> irPairs,
			ArrayList<MutablePair<Long, Double>> redPairs) {
		if (irPairs == null || redPairs == null || irPairs.size() != redPairs.size())
			return Double.NaN;

		int windowSize = 10;
		int start = Math.max(0, index - windowSize);
		int end = Math.min(irPairs.size() - 1, index + windowSize);

		ArrayList<Double> irVals = new ArrayList<>();
		ArrayList<Double> redVals = new ArrayList<>();

		for (int i = start; i <= end; i++) {
			irVals.add(irPairs.get(i).getRight());
			redVals.add(redPairs.get(i).getRight());
		}

		double irMean = irVals.stream().mapToDouble(Double::doubleValue).average().orElse(0);
		double redMean = redVals.stream().mapToDouble(Double::doubleValue).average().orElse(0);

		double irAC = irVals.stream().mapToDouble(v -> Math.abs(v - irMean)).sum() / irVals.size();
		double redAC = redVals.stream().mapToDouble(v -> Math.abs(v - redMean)).sum() / redVals.size();

		if (irMean == 0 || redMean == 0)
			return Double.NaN;
		double ratio = (redAC / redMean) / (irAC / irMean);
		return 110 - 25 * ratio;
	}

	public ArrayList<ParameterValue> getParameterValues() {
		return parameterValues;
	}

	public ArrayList<StructureData> getProcessedData() {
		return processedData;
	}
}
