package org.axolotlj.RemoteHealth.model;

public class StructureData {

    private final long timeStamp;
    private final SensorValue<Short> ecg;
    private final SensorValue<Float> accel;
    private final SensorValue<Float> temp;
    private final SensorValue<Integer> ir;
    private final SensorValue<Integer> red;

    public StructureData(long timeStamp, SensorValue<Short> ecg, SensorValue<Float> accel,
                         SensorValue<Float> temp, SensorValue<Integer> ir, SensorValue<Integer> red) {
        this.timeStamp = timeStamp;
        this.ecg = ecg;
        this.accel = accel;
        this.temp = temp;
        this.ir = ir;
        this.red = red;
    }

    public long getTimeStamp() { return timeStamp; }
    public SensorValue<Short> getEcg() { return ecg; }
    public SensorValue<Float> getAccel() { return accel; }
    public SensorValue<Float> getTemp() { return temp; }
    public SensorValue<Integer> getIr() { return ir; }
    public SensorValue<Integer> getRed() { return red; }

    @Override
    public String toString() {
        return Long.toString(timeStamp)+","+ecg+","+accel+","+temp+","+ir+","+red;
    }
    
    public String toCsvLine() {
        return String.format("%d,%s,%s,%s,%s,%s",
            getTimeStamp(),
            getEcg().getValue(),
            getAccel().getValue(),
            getTemp().getValue(),
            getIr().getValue(),
            getRed().getValue()
        );
    }

}
