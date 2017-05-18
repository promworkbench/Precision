package org.processmining.precision.models;

public class EventBasedPrecision {

	private long nofEvents;
	private double sumPrecision;
	
	public EventBasedPrecision() {
		nofEvents = 0;
		sumPrecision = 0.0;
	}
	
	public double getPrecision() {
		return nofEvents == 0 ? 0.0 : sumPrecision / nofEvents;
	}
	
	public double getSumPrecision() {
		return sumPrecision;
	}

	public void addSumPrecision(double eventPrecision) {
		this.sumPrecision += eventPrecision;
	}

	public long getNofEvents() {
		return nofEvents;
	}

	public void addNofEvents(long nofEvents) {
		this.nofEvents += nofEvents;
	}
	
	
}
