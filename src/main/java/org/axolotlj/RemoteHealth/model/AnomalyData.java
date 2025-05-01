package org.axolotlj.RemoteHealth.model;


public class AnomalyData {
	private final long time;
	private final String anomaly;
	private final SeverityLevel severityLevel; 
	private final String detail;

	public AnomalyData(long time, String anomaly, SeverityLevel severityLevel, String detail) {
		this.time = time;
		this.anomaly = anomaly;
		this.severityLevel = severityLevel;
		this.detail = detail;
	}
	
	public String getAnomaly() {
		return anomaly;
	}
	
	public String getDetail() {
		return detail;
	}
	
	public SeverityLevel getSeverityLevel() {
		return severityLevel;
	}
	
	public long getTime() {
		return time;
	}
	
	public enum SeverityLevel {
		LOW("Low", "Minor deviation, not immediately concerning"),
		MEDIUM("Medium", "Moderate abnormality, requires observation"),
		HIGH("High", "Significant issue, potentially risky"),
		CRITICAL("Critical", "Urgent attention required, possibly life-threatening");
		
		private final String label;
		private final String description;
		
		SeverityLevel(String label, String description) {
			this.label = label;
			this.description = description;
		}
		
		public String getLabel() {
			return label;
		}
		
		public String getDescription() {
			return description;
		}
		
		@Override
		public String toString() {
			return label;
		}
	}
}

