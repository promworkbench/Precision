package org.processmining.precision.models;

public class EscapingEdgesPrecisionResult {

	private double precision;
	private EscapingEdgesPrecisionAutomaton automaton;
	private double weightedExecuted;
	private double weightedAvailable;

	public double getWeigtedExecuted() {
		return weightedExecuted;
	}

	public void setWeightedExecuted(double weightedExecuted) {
		this.weightedExecuted = weightedExecuted;
	}

	public double getWeightedAvailable() {
		return weightedAvailable;
	}

	public void setWeightedAvailable(double weightedAvailable) {
		this.weightedAvailable = weightedAvailable;
	}

	public double getPrecision() {
		return precision;
	}

	public void setPrecision(double precision) {
		this.precision = precision;
	}

	public EscapingEdgesPrecisionAutomaton getAutomaton() {
		return automaton;
	}

	public void setAutomaton(EscapingEdgesPrecisionAutomaton automaton) {
		this.automaton = automaton;
	}

}
