package org.axolotlj.RemoteHealth.analysis.test;

import org.apache.commons.math3.stat.regression.SimpleRegression;

public class SpO2Calibrator {
    private final SimpleRegression regression;

    public SpO2Calibrator() {
        this.regression = new SimpleRegression(true); // incluye intercepto
    }

    // Agrega un par (R, SpO2 real)
    public void addTrainingData(double rRatio, double realSpO2) {
        regression.addData(rRatio, realSpO2);
    }

    // Devuelve el SpO2 calibrado para un nuevo valor de R
    public double estimateSpO2(double rRatio) {
        return regression.predict(rRatio);
    }

    public boolean isReady() {
        return regression.getN() >= 5; // mínimo recomendado para estabilidad
    }

    public void printModel() {
        System.out.println("Modelo SpO2: y = " + regression.getIntercept() +
                " + " + regression.getSlope() + " * R");
    }
}
