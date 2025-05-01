package org.axolotlj.RemoteHealth.util;

import java.util.ArrayList;

import org.apache.commons.lang3.tuple.MutablePair;

public class MutablePairHandler {
	public static long[] extractTimestamps(ArrayList<MutablePair<Long, Double>> pairs) {
	    return pairs.stream()
	                .mapToLong(MutablePair::getLeft)
	                .toArray();
	}
	
	public static double[] extractValues(ArrayList<MutablePair<Long, Double>> pairs) {
	    return pairs.stream()
	                .mapToDouble(MutablePair::getRight)
	                .toArray();
	}

	public static void assignValuesToPairs(ArrayList<MutablePair<Long, Double>> pairs, double[] values) {
	    if (pairs.size() != values.length) {
	    	System.err.println("La cantidad de valores no coincide con el número de pares.");
	    	System.out.println(values.length);
	    	System.out.println(pairs.size());
	    	throw new IllegalArgumentException("La cantidad de valores no coincide con el número de pares.");
	    }

	    for (int i = 0; i < pairs.size(); i++) {
	        if (!Double.isNaN(values[i]) && !Double.isInfinite(values[i])) {
	            pairs.get(i).setRight(values[i]);
	        } else {
	            System.err.println("Valor inválido en índice " + i + ": " + values[i]);
	        }
	    }
	}

	public static ArrayList<MutablePair<Long, Double>> createPairs(long[] timestamps, double[] values) {
	    if (timestamps.length != values.length) {
	        throw new IllegalArgumentException("La longitud de los timestamps y los valores no coincide.");
	    }

	    ArrayList<MutablePair<Long, Double>> pairs = new ArrayList<>();

	    for (int i = 0; i < timestamps.length; i++) {
	        pairs.add(MutablePair.of(timestamps[i], values[i]));
	    }

	    return pairs;
	}


}
