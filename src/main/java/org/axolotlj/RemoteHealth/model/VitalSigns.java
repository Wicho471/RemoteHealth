package org.axolotlj.RemoteHealth.model;

public class VitalSigns {
    private final long timeStamp;
    private double spO2;
    private int bpm;
    private int sbp;
    private int dbp;

    // Constructor básico
    public VitalSigns(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    // Constructor con todos los parámetros
    public VitalSigns(long timeStamp, double spO2, int bpm, int sbp, int dbp) {
        this.timeStamp = timeStamp;
        this.spO2 = spO2;
        this.bpm = bpm;
        this.sbp = sbp;
        this.dbp = dbp;
    }

    public void setSpO2(double spO2) {
        this.spO2 = spO2;
    }

    public void setBpm(int bpm) {
        this.bpm = bpm;
    }

    public void setSbp(int sbp) {
        this.sbp = sbp;
    }

    public void setDbp(int dbp) {
        this.dbp = dbp;
    }

    public double getSpO2() {
        return spO2;
    }

    public int getBpm() {
        return bpm;
    }

    public int getSbp() {
        return sbp;
    }

    public int getDbp() {
        return dbp;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    @Override
    public String toString() {
        return String.format(
            "Time: %d | SpO2: %.1f%% | BPM: %d | SBP: %d | DBP: %d",
            timeStamp, spO2, bpm, sbp, dbp
        );
    }
}
