package org.processmining.precision.models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EscapingEdgesPrecisionState {

	public static String EPSILON = "epsilon";

	private List<EscapingEdgesPrecisionEdge> edges;
	// state prefix is made up of a sequence of transitions of the model
	// transitions are represented by their stringified LocalNodeIDs
	private List<String> prefix;
	private double weight;
	// whether it is an executed state 
	private boolean executed;

	public EscapingEdgesPrecisionState(List<String> prefix) {
		this.prefix = prefix;
		this.edges = new ArrayList<EscapingEdgesPrecisionEdge>();
		// default weight 
		this.weight = 0.0;
		this.executed = false;
	}

	public EscapingEdgesPrecisionState(List<String> prefix, double weight, boolean isExecuted) {
		this.prefix = prefix;
		this.edges = new ArrayList<EscapingEdgesPrecisionEdge>();
		this.weight = weight;
		this.executed = isExecuted;
	}

	public void addEdge(EscapingEdgesPrecisionEdge edge) {
		if (this.edges.contains(edge))
			return;

		this.edges.add(edge);
	}

	public boolean containsEdge(EscapingEdgesPrecisionEdge edge) {
		return this.edges.contains(edge);
	}

	public EscapingEdgesPrecisionEdge getEdge(int index) {
		return this.edges.get(index);
	}

	public EscapingEdgesPrecisionEdge removeEdge(int index) {
		return this.edges.remove(index);
	}

	public void removeEdge(EscapingEdgesPrecisionEdge edge) {
		this.edges.remove(edge);
	}

	public int getEdgeCount() {
		return this.edges.size();
	}

	public List<String> getPrefix() {
		return this.prefix;
	}

	public String getPrefixString() {
		String string = "";
		for (String label : prefix) {
			string += label;
		}
		return string;
	}

	public double getWeight() {
		return this.weight;
	}

	public void setWeight(double weight) {
		this.weight = weight;
	}

	public boolean isExecuted() {
		return this.executed;
	}

	public void setExecuted(boolean isExecuted) {
		this.executed = isExecuted;
	}

	public String toString() {
		return "(" + prefix + ", " + weight + ")";
	}

	public int hashCode() {
		String concat = "";
		for (String label : this.prefix) {
			concat += label;
		}
		return concat.hashCode();
	}

	public boolean equals(Object other) {
		if (!(other instanceof EscapingEdgesPrecisionState)) {
			return false;
		}

		EscapingEdgesPrecisionState v = (EscapingEdgesPrecisionState) other;
		return this.prefix.equals(v.prefix);
	}

	public List<EscapingEdgesPrecisionEdge> getEdges() {
		return new ArrayList<>(this.edges);
	}

	public static void main(String[] args) {
		String label1 = "a";
		String label11 = "a";
		String label2 = "b";
		String label21 = "b";

		String[] prefix1 = { label1, label2 };
		String[] prefix11 = { label11, label21 };
		String[] prefix2 = { label2, label1 };

		EscapingEdgesPrecisionState state1 = new EscapingEdgesPrecisionState(Arrays.asList(prefix1));
		EscapingEdgesPrecisionState state2 = new EscapingEdgesPrecisionState(Arrays.asList(prefix11));

		System.out.println("state 1 == state 2: " + (state1.equals(state2)));
		System.out.println("hashcode state 1 == state 2: " + (state1.hashCode() == state2.hashCode()));

		EscapingEdgesPrecisionState state3 = new EscapingEdgesPrecisionState(Arrays.asList(prefix2));

		System.out.println("state 1 == state 3: " + (state2.equals(state3)));
		System.out.println("hashcode state 1 == state 3: " + (state2.hashCode() == state3.hashCode()));
	}
}
