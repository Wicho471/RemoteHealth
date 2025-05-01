package org.axolotlj.RemoteHealth.filters;

import com.github.psambit9791.jdsp.signal.Convolution;
import com.github.psambit9791.jdsp.filter.Butterworth;

public class ButterworthFilter {
	
    // Método para generar un notch filter básico tipo FIR para 60 Hz
    public static double []  applyNotchFilter(double [] ecgValues, int FS) {
    	
        // Diseñamos un notch FIR básico (band-stop) centrado en 60 Hz
        int N = 101;  // longitud del filtro, debe ser impar
        double notchFreq = 60.0;
        double f1 = (notchFreq - 1.0) / (FS / 2.0); // límite inferior
        double f2 = (notchFreq + 1.0) / (FS / 2.0); // límite superior

        double[] h = new double[N];  // coeficientes del filtro
        int M = (N - 1) / 2;
        
        for (int n = 0; n < N; n++) {
            if (n == M) {
                h[n] = 1 - (f2 - f1);
            } else {
                double val = (Math.sin(Math.PI * (n - M) * f1) - Math.sin(Math.PI * (n - M) * f2)) 
                             / (Math.PI * (n - M));
                h[n] = val;
            }
            // Aplicar ventana Hamming
            h[n] *= (0.54 - 0.46 * Math.cos(2 * Math.PI * n / (N - 1)));
        }

        // Aplicamos convolución (filtrado) con la señal ECG
        Convolution conv = new Convolution(ecgValues, h);
        double[] filtered = conv.convolve();
        
        return filtered;
    }
    
    /**
     * Tipos de filtro compatibles con la clase Butterworth que mostraste.
     */
    public enum FilterType {
        LOWPASS, HIGHPASS, BANDPASS, BANDSTOP
    }

    /**
     * Aplica un filtro Butterworth a la señal ECG dada.
     * <p>
     * Ejemplo de uso (rechaza-banda 59-61 Hz, order=4, Fs=250):
     * <pre>
     *   applyButterworthFilter(ecgData, 250.0, 4, FilterType.BANDSTOP, 59.0, 61.0);
     * </pre>
     * 
     * @param ecgData   Lista de (amplitud, tiempo).
     * @param fs        Frecuencia de muestreo en Hz.
     * @param order     Orden del filtro (ej: 4, 8, 10, etc.).
     * @param filter    Tipo de filtro (LOWPASS, HIGHPASS, BANDPASS, BANDSTOP).
     * @param cutoffHz  Frecuencia(s) de corte en Hz.
     *                  - Si el filtro es LOWPASS/HIGHPASS, usar 1 valor: cutoffHz[0].
     *                  - Si el filtro es BANDPASS/BANDSTOP, usar 2 valores: [lowCut, highCut].
     * @return          Nueva lista con (amplitud filtrada, tiempo). 
     *                  La lista original se modifica in-place.
     */
    public static double [] applyButterworthFilter(
    		double[] signal,
            double fs,
            int order,
            FilterType filter,
            double... cutoffHz
    ) {

        // 2) Instanciar la clase Butterworth con la Fs
        Butterworth bw = new Butterworth(fs);

        // 3) Variable para guardar la señal filtrada
        double[] filteredSignal = null;

        // 4) Dependiendo del tipo, llamamos al método adecuado
        switch (filter) {
            case LOWPASS:
                // cutoffHz[0] => frec de corte en Hz
                filteredSignal = bw.lowPassFilter(signal, order, cutoffHz[0]);
                break;

            case HIGHPASS:
                // cutoffHz[0] => frec de corte en Hz
                filteredSignal = bw.highPassFilter(signal, order, cutoffHz[0]);
                break;

            case BANDPASS:
                // cutoffHz[0] => lowCut, cutoffHz[1] => highCut
                if (cutoffHz.length < 2) {
                    throw new IllegalArgumentException("Se requieren 2 frecuencias para un filtro band-pass");
                }
                filteredSignal = bw.bandPassFilter(signal, order, cutoffHz[0], cutoffHz[1]);
                break;

            case BANDSTOP:
                // cutoffHz[0] => lowCut, cutoffHz[1] => highCut
                if (cutoffHz.length < 2) {
                    throw new IllegalArgumentException("Se requieren 2 frecuencias para un filtro band-stop");
                }
                filteredSignal = bw.bandStopFilter(signal, order, cutoffHz[0], cutoffHz[1]);
                break;

            default:
                throw new UnsupportedOperationException("Tipo de filtro desconocido: " + filter);
        }

        return filteredSignal;
    }
    
    
}
