package org.axolotlj.remotehealth.desktop.utils;

import java.util.ArrayList;

import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.MutableTriple;

public record FilterResult(ArrayList<MutablePair<Long, Double>> ecgFiltered,
		ArrayList<MutableTriple<Long, Double, Double>> plethFiltered) {
}