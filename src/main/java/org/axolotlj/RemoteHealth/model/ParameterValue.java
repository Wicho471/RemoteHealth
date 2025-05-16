package org.axolotlj.RemoteHealth.model;

public class ParameterValue {
	private final long timeStamp;
	private double spO2;
	private int bpm;
	private double temp;
	private double mov;
	
	public ParameterValue(long timeStamp) {
		this.timeStamp = timeStamp;
	}
	
	public void setBpm(int bpm) {
		this.bpm = bpm;
	}
	
	public void setMov(double mov) {
		this.mov = mov;
	}
	
	public void setSpO2(double spO2) {
		this.spO2 = spO2;
	}
	
	public void setTemp(double temp) {
		this.temp = temp;
	}
	
	public int getBpm() {
		return bpm;
	}
	
	public double getMov() {
		return mov;
	}
	
	public double getSpO2() {
		return spO2;
	}
	
	public double getTemp() {
		return temp;
	}
	
	public long getTimeStamp() {
		return timeStamp;
	}
}
