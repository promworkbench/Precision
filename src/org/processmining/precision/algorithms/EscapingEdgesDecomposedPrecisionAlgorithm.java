package org.processmining.precision.algorithms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNetArray;
import org.processmining.acceptingpetrinetdecomposer.algorithms.DecomposeAcceptingPetriNetUsingActivityClusterArrayAlgorithm;
import org.processmining.acceptingpetrinetdecomposer.parameters.DecomposeAcceptingPetriNetUsingActivityClusterArrayParameters;
import org.processmining.acceptingpetrinetdecomposer.strategies.impl.DecompositionGenericHundredStrategy;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.log.utils.XUtils;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.semantics.IllegalTransitionException;
import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;
import org.processmining.plugins.petrinet.replayresult.PNRepResult;
import org.processmining.plugins.petrinet.replayresult.PNRepResultImpl;
import org.processmining.plugins.petrinet.replayresult.StepTypes;
import org.processmining.plugins.replayer.replayresult.SyncReplayResult;
import org.processmining.precision.models.EscapingEdgesDecomposedPrecisionResult;
import org.processmining.precision.models.EscapingEdgesPrecisionResult;
import org.processmining.precision.parameters.EscapingEdgesDecomposedPrecisionParameters;

public class EscapingEdgesDecomposedPrecisionAlgorithm {

	public EscapingEdgesDecomposedPrecisionResult apply(PluginContext context, PNRepResult alignments, AcceptingPetriNet net,
			EscapingEdgesDecomposedPrecisionParameters parameters) throws IllegalTransitionException {
		/*
		 * Create a list of maps to store the relation between the main net and the subnets.
		 */
		List<Map<PetrinetNode, PetrinetNode>> parentChildMaps = new ArrayList<Map<PetrinetNode, PetrinetNode>>();
		/*
		 * Decompose the nets, and fill the map just created as a side effect.
		 */
		AcceptingPetriNetArray nets = getNets(context, alignments, net, parameters, parentChildMaps);
		context.getProvidedObjectManager().createProvidedObject("Precision Decomposition", nets, AcceptingPetriNetArray.class, context);
		/*
		 * Decompose the alignments using the map just created.
		 */
		List<PNRepResult> alignmentArray = getAlignments(alignments, nets, parameters, parentChildMaps);
		/*
		 * Compute the precision metric for every subnet-subalignment combination.
		 */
		EscapingEdgesDecomposedPrecisionResult result = new EscapingEdgesDecomposedPrecisionResult();
		EscapingEdgesPrecisionAlgorithm precisionAlgorithm = new EscapingEdgesPrecisionAlgorithm();
		double weightedExecuted = 0.0;
		double weightedAvailable = 0.0;
		for (int i = 0; i < nets.getSize(); i++) {
			EscapingEdgesPrecisionResult subResults = precisionAlgorithm.apply(context, alignmentArray.get(i), nets.getNet(i), parameters);
			weightedExecuted += subResults.getWeightedExecuted();
			weightedAvailable += subResults.getWeightedAvailable();
			System.out.println("EscapingEdgesDecomposedPrecisionAlgorithm] Precision = " + subResults.getPrecision());
		}
		/*
		 * Set the results for the main net and the main alignment obtained through decomposed computation of precision.
		 */
		result.setWeightedExecuted(weightedExecuted);
		result.setWeightedAvailable(weightedAvailable);
		result.setPrecision(weightedExecuted/weightedAvailable);
		return result;
	}

	private AcceptingPetriNetArray getNets(PluginContext context, PNRepResult alignments, AcceptingPetriNet net,
			EscapingEdgesDecomposedPrecisionParameters parameters, List<Map<PetrinetNode, PetrinetNode>> parentChildMaps) {

		/*
		 * To decompose the net, we need to have a set of activities and a tec-mapping. These are constructed first from the alignment.
		 * Note that we assume that a transition with label "X" is mapped onto an activity "X".
		 */
		Map<String, XEventClass> activities = new HashMap<String, XEventClass>();
		TransEvClassMapping mapping = new TransEvClassMapping(XUtils.STANDARDCLASSIFIER, XUtils.INVISIBLEACTIVITY);
		for (SyncReplayResult alignment : alignments) {
			for (int i = 0; i < alignment.getStepTypes().size(); i++) {
				switch (alignment.getStepTypes().get(i)) {
					case MREAL :
					case LMGOOD : {
						Transition transition = (Transition) alignment.getNodeInstance().get(i);
						if (!activities.containsKey(transition.getLabel())) {
							activities.put(transition.getLabel(), new XEventClass(transition.getLabel(), 0));
						}
						mapping.put(transition, activities.get(transition.getLabel()));
						break;
					}
					case MINVI : {
						Transition transition = (Transition) alignment.getNodeInstance().get(i);
						mapping.put(transition, XUtils.INVISIBLEACTIVITY);
						break;
					}
					default : {
						break;
					}
				}
			}
		}
		/*
		 * Create the decomposer, and set its parameters.
		 */
		DecomposeAcceptingPetriNetUsingActivityClusterArrayAlgorithm decomposer = new DecomposeAcceptingPetriNetUsingActivityClusterArrayAlgorithm();
		DecomposeAcceptingPetriNetUsingActivityClusterArrayParameters decomposerParameters = new DecomposeAcceptingPetriNetUsingActivityClusterArrayParameters(
				net, new HashSet<XEventClass>(activities.values()), XUtils.STANDARDCLASSIFIER);
		decomposerParameters.setStrategy(DecompositionGenericHundredStrategy.NAME);
		decomposerParameters.setInvisibleActivity(XUtils.INVISIBLEACTIVITY);
		decomposerParameters.setMapping(mapping);
		decomposerParameters.setUnsplittableActivities(parameters.getUnsplittableActivities());
		/*
		 * Run the decomposer with these parameters.
		 */
		return decomposer.apply(context, net, null, decomposerParameters, parentChildMaps);
	}

	private List<PNRepResult> getAlignments(PNRepResult alignments, AcceptingPetriNetArray nets,
			EscapingEdgesDecomposedPrecisionParameters parameters, List<Map<PetrinetNode, PetrinetNode>> parentChildMaps) {
		/*
		 * Create an empty list with subalignments.
		 */
		List<PNRepResult> subAlignmentArray = new ArrayList<PNRepResult>();
		for (int i = 0; i < nets.getSize(); i++) {
			/*
			 * Add subalignments to this list for every subnet.
			 */
			Map<PetrinetNode, PetrinetNode> parentChildMap = parentChildMaps.get(i);
			Set<SyncReplayResult> subAlignments = new HashSet<SyncReplayResult>();
			for (SyncReplayResult alignment : alignments) {
				/*
				 * Filter the alignment into a subalignment for this subnet.
				 */
				List<Object> subNodeInstances = new ArrayList<Object>();
				List<StepTypes> subStepTypes = new ArrayList<StepTypes>();
				for (int j = 0; j < alignment.getStepTypes().size(); j++) {
					switch (alignment.getStepTypes().get(j)) {
						case MREAL :
						case MINVI :
						case LMGOOD : {
							if (parentChildMap.containsKey(alignment.getNodeInstance().get(j))) {
								subStepTypes.add(alignment.getStepTypes().get(j));
								subNodeInstances.add(parentChildMap.get(alignment.getNodeInstance().get(j)));
							}
							break;
						}
						default : {
							break;
						}
					}
				}
				/*
				 * Add the subalignment as many times as there were traces linked ot the alignment.
				 */
				for (int traceIndex : alignment.getTraceIndex()) {
					SyncReplayResult subAlignment = new SyncReplayResult(subNodeInstances, subStepTypes, traceIndex);
					subAlignment.setReliable(alignment.isReliable());
					subAlignments.add(subAlignment);
				}
			}
			subAlignmentArray.add(new PNRepResultImpl(subAlignments));
		}
		return subAlignmentArray;
	}
}
