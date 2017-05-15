package org.processmining.precision.parameters;

import java.util.HashSet;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.acceptingpetrinetdecomposer.strategies.impl.DecompositionGenericHundredStrategy;
import org.processmining.plugins.petrinet.replayresult.PNRepResult;

public class EscapingEdgesDecomposedPrecisionParameters extends  EscapingEdgesPrecisionParameters {

	/*
	 * Collection of activities that may not be on the border of any subnet.
	 * If empty, the decomposition will be maximal. 
	 */
	private Set<XEventClass> unsplittableActivities;
	private String strategy;

	public EscapingEdgesDecomposedPrecisionParameters(PNRepResult alignments, AcceptingPetriNet net) {
		super(alignments, net);
		setUnsplittableActivities(new HashSet<XEventClass>());
		setStrategy(DecompositionGenericHundredStrategy.NAME);
	}
	
	public EscapingEdgesDecomposedPrecisionParameters(EscapingEdgesDecomposedPrecisionParameters parameters) {
		super(parameters);
		setUnsplittableActivities(parameters.getUnsplittableActivities());
	}
	
	public Set<XEventClass> getUnsplittableActivities() {
		return unsplittableActivities;
	}

	public void setUnsplittableActivities(Set<XEventClass> unsplittableActivities) {
		this.unsplittableActivities = unsplittableActivities;
	}

	public String getStrategy() {
		return strategy;
	}

	public void setStrategy(String strategy) {
		this.strategy = strategy;
	}

}
