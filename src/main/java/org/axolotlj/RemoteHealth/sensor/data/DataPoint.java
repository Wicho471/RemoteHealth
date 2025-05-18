package org.axolotlj.RemoteHealth.sensor.data;

public class DataPoint {

	private final long timeStamp;
	private SensorValue<Short> ecg;
	private SensorValue<Float> accel;
	private SensorValue<Float> temp;
	private SensorValue<Integer> ir;
	private SensorValue<Integer> red;

	public DataPoint(long timeStamp, SensorValue<Short> ecg, SensorValue<Float> accel, SensorValue<Float> temp,
			SensorValue<Integer> ir, SensorValue<Integer> red) {
		this.timeStamp = timeStamp;
		this.ecg = ecg;
		this.accel = accel;
		this.temp = temp;
		this.ir = ir;
		this.red = red;
	}

	public long getTimeStamp() {
		return timeStamp;
	}

	public SensorValue<Short> getEcg() {
		return ecg;
	}

	public SensorValue<Float> getAccel() {
		return accel;
	}

	public SensorValue<Float> getTemp() {
		return temp;
	}

	public SensorValue<Integer> getIr() {
		return ir;
	}

	public SensorValue<Integer> getRed() {
		return red;
	}

	public void setAccel(SensorValue<Float> accel) {
		this.accel = accel;
	}

	public void setEcg(SensorValue<Short> ecg) {
		this.ecg = ecg;
	}

	public void setIr(SensorValue<Integer> ir) {
		this.ir = ir;
	}

	public void setRed(SensorValue<Integer> red) {
		this.red = red;
	}

	public void setTemp(SensorValue<Float> temp) {
		this.temp = temp;
	}

	@Override
	public String toString() {
		return Long.toString(timeStamp) + "," + ecg + "," + accel + "," + temp + "," + ir + "," + red;
	}

	public String toCsvLine() {
		String ts = Long.toHexString(timeStamp).toUpperCase();
		String ecg = (this.ecg != null && this.ecg.isValid()) ? Integer.toHexString(this.ecg.getValue()).toUpperCase()
				: "NR";
		String accel = (this.accel != null && this.accel.isValid())
				? Float.toHexString(this.accel.getValue()).toUpperCase()
				: "NR";
		String temp = (this.temp != null && this.temp.isValid()) ? Float.toHexString(this.temp.getValue()).toUpperCase()
				: "NR";
		String ir = (this.ir != null && this.ir.isValid()) ? Integer.toHexString(this.ir.getValue()).toUpperCase()
				: "NR";
		String red = (this.red != null && this.red.isValid()) ? Integer.toHexString(this.red.getValue()).toUpperCase()
				: "NR";

		return String.join(",", ts, ecg, accel, temp, ir, red);
	}

}
