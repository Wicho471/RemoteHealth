package org.axolotlj.RemoteHealth.sensor;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.MutableTriple;

public class TuplaUtil {

	public static long[] extractTimestamps(ArrayList<MutablePair<Long, Double>> pairs) {
		return pairs.stream().mapToLong(MutablePair::getLeft).toArray();
	}

	public static long[] extractTimestamps(List<MutableTriple<Long, Double, Double>> triples) {
		return triples.stream().mapToLong(MutableTriple::getLeft).toArray();
	}

	public static double[] extractValues(ArrayList<MutablePair<Long, Double>> pairs) {
		return pairs.stream().mapToDouble(MutablePair::getRight).toArray();
	}

	public static double[] extractMiddleValues(ArrayList<MutableTriple<Long, Double, Double>> triples) {
		return triples.stream().mapToDouble(MutableTriple::getMiddle).toArray();
	}

	public static double[] extractRightValues(ArrayList<MutableTriple<Long, Double, Double>> triples) {
		return triples.stream().mapToDouble(MutableTriple::getRight).toArray();
	}

	public static void assignTuplaValues(ArrayList<MutablePair<Long, Double>> pairs, double[] values) {
		if (pairs.size() != values.length) {
			System.err.println("La cantidad de valores no coincide con el número de pares.");
			throw new IllegalArgumentException("La cantidad de valores no coincide con el número de pares.");
		}

		for (int i = 0; i < pairs.size(); i++) {
			if (checkDouble(values[i])) {
				pairs.get(i).setRight(values[i]);
			} else {
				System.err.println("Valor inválido en índice " + i + ": " + values[i]);
			}
		}
	}

	public static void assignTuplaValues(ArrayList<MutableTriple<Long, Double, Double>> triples, double[] middle,
			double[] right) {
		if (triples.size() != middle.length || triples.size() != right.length) {
			throw new IllegalArgumentException("La cantidad de valores no coincide con el número de tuplas.");
		}

		for (int i = 0; i < triples.size(); i++) {
			if (checkDouble(middle[i]) && checkDouble(right[i])) {
				triples.get(i).setMiddle(middle[i]);
				triples.get(i).setRight(right[i]);
			} else {
				System.err.println("Valor inválido en índice " + i + ": " + middle[i] + ", " + right[i]);
			}
		}
	}

	public static ArrayList<MutablePair<Long, Double>> createTupla(long[] timestamps, double[] values) {
		if (timestamps.length != values.length) {
			throw new IllegalArgumentException("La longitud de los timestamps y los valores no coincide.");
		}

		ArrayList<MutablePair<Long, Double>> pairs = new ArrayList<>();

		for (int i = 0; i < timestamps.length; i++) {
			pairs.add(MutablePair.of(timestamps[i], values[i]));
		}

		return pairs;
	}

	public static ArrayList<MutableTriple<Long, Double, Double>> createTupla(long[] timestamps, double[] middle,
			double[] right) {
		if (timestamps.length != middle.length || timestamps.length != right.length) {
			throw new IllegalArgumentException("La longitud de los timestamps y los valores no coincide.");
		}

		ArrayList<MutableTriple<Long, Double, Double>> triples = new ArrayList<>();

		for (int i = 0; i < timestamps.length; i++) {
			triples.add(MutableTriple.of(timestamps[i], middle[i], right[i]));
		}

		return triples;
	}

	private static boolean checkDouble(double value) {
		return !Double.isNaN(value) && !Double.isInfinite(value);
	}
}
