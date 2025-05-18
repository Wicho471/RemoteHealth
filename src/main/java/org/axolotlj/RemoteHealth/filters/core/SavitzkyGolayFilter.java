package org.axolotlj.RemoteHealth.filters.core;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.DecompositionSolver;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.SingularValueDecomposition;

public class SavitzkyGolayFilter {

    /**
     * Aplica el filtro Savitzky-Golay a una señal.
     *
     * @param signal      Señal original.
     * @param windowSize  Tamaño de ventana (debe ser impar).
     * @param polyOrder   Orden del polinomio (ej: 2, 3).
     * @return            Señal suavizada.
     */
    public static double[] filter(double[] signal, int windowSize, int polyOrder) {
        if (windowSize % 2 == 0 || windowSize < 1) {
            throw new IllegalArgumentException("windowSize debe ser impar y positivo.");
        }
        if (windowSize <= polyOrder) {
            throw new IllegalArgumentException("windowSize debe ser mayor que polyOrder.");
        }

        int halfWindow = windowSize / 2;
        int n = signal.length;
        double[] result = new double[n];

        // Construir la matriz de diseño A (tamaño: windowSize x (polyOrder + 1))
        RealMatrix A = new Array2DRowRealMatrix(windowSize, polyOrder + 1);
        for (int i = -halfWindow; i <= halfWindow; i++) {
            for (int j = 0; j <= polyOrder; j++) {
                A.setEntry(i + halfWindow, j, Math.pow(i, j));
            }
        }

        // Calcular la pseudoinversa de A (tamaño resultante: (polyOrder+1) x windowSize)
        RealMatrix At = A.transpose();              // (polyOrder+1) x windowSize
        RealMatrix ATA = At.multiply(A);           // (polyOrder+1) x (polyOrder+1)
        DecompositionSolver solver = new SingularValueDecomposition(ATA).getSolver();
        RealMatrix pseudoInv = solver.getInverse().multiply(At);  // (polyOrder+1) x windowSize

        // Tomar la fila 0 de la pseudoinversa para suavizar (0.ª derivada)
        double[] coeffs = pseudoInv.getRow(0);

        // Aplicar convolución centrada
        for (int i = 0; i < n; i++) {
            double sum = 0.0;
            for (int j = -halfWindow; j <= halfWindow; j++) {
                int idx = i + j;
                if (idx >= 0 && idx < n) {
                    sum += coeffs[j + halfWindow] * signal[idx];
                }
            }
            result[i] = sum;
        }

        return result;
    }
}
