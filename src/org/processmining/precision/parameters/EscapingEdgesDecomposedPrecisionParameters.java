package org.processmining.precision.parameters;

import java.util.HashSet;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.plugins.petrinet.replayresult.PNRepResult;

public class EscapingEdgesDecomposedPrecisionParameters extends  EscapingEdgesPrecisionParameters {

	/*
	 * Collection of activities that may not be on the border of any subnet.
	 * If empty, the decomposition will be maximal. 
	 */
	private Set<XEventClass> unsplittableActivities;

	public EscapingEdgesDecomposedPrecisionParameters(PNRepResult alignments, AcceptingPetriNet net) {
		super(alignments, net);
		setUnsplittableActivities(new HashSet<XEventClass>());
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

}