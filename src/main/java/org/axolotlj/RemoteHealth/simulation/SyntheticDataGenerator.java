package org.axolotlj.RemoteHealth.simulation;

import umontreal.ssj.randvar.NormalGen;
import umontreal.ssj.rng.MRG32k3a;

/**
 * Generador de datos sintéticos usando funciones matemáticas y ruido aleatorio.
 */
public class SyntheticDataGenerator implements DataPayloadGenerator {

    private static final MRG32k3a rng = new MRG32k3a();
    private static final NormalGen noiseGen = new NormalGen(rng, 0.0, 1.0);
    private static final double DT = 0.003;

    private double t = 0.0;

    @Override
    public String generatePayload() {
        long timeStamp = System.currentTimeMillis();
        String hexTimestamp = Long.toHexString(timeStamp).toUpperCase();

        double ecg = Math.sin(2 * Math.PI * 1 * t) + 0.5 * Math.sin(2 * Math.PI * 60 * t) + 0.2 * noiseGen.nextDouble();
        int ecgHex = (int) Math.round(ecg * 1000 + 2048);

        double irSignal = 2048 + 200 * Math.sin(2 * Math.PI * 1.2 * t) + 0.1 * noiseGen.nextDouble();
        int ir = (int) Math.round(irSignal);

        double redSignal = 2048 + 180 * Math.cos(2 * Math.PI * 0.8 * t) + 0.1 * noiseGen.nextDouble();
        int red = (int) Math.round(redSignal);

        t += DT;

        String hexEcg = Integer.toHexString(clamp(ecgHex, 0, 0xFFFF)).toUpperCase();
        String hexIr = Integer.toHexString(clamp(ir, 0, 0xFFFF)).toUpperCase();
        String hexRed = Integer.toHexString(clamp(red, 0, 0xFFFF)).toUpperCase();

        return hexTimestamp + "," + hexEcg + ",0.99,36.5," + hexIr + "," + hexRed;
    }


    private int clamp(int val, int min, int max) {
        return Math.max(min, Math.min(max, val));
    }
}
