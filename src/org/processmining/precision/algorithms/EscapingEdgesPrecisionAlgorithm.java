package org.processmining.precision.algorithms;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.util.Pair;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.semantics.IllegalTransitionException;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.models.semantics.petrinet.PetrinetSemantics;
import org.processmining.models.semantics.petrinet.impl.PetrinetSemanticsFactory;
import org.processmining.plugins.petrinet.replayresult.PNRepResult;
import org.processmining.plugins.petrinet.replayresult.StepTypes;
import org.processmining.plugins.replayer.replayresult.SyncReplayResult;
import org.processmining.precision.models.EscapingEdgesPrecisionAutomaton;
import org.processmining.precision.models.EscapingEdgesPrecisionEdge;
import org.processmining.precision.models.EscapingEdgesPrecisionResult;
import org.processmining.precision.models.EscapingEdgesPrecisionState;
import org.processmining.precision.parameters.EscapingEdgesPrecisionParameters;

public class EscapingEdgesPrecisionAlgorithm {

	// precision results
	EscapingEdgesPrecisionResult result;
	
	public List<String> getModelTrace(SyncReplayResult traceAlignment) {
		List<String> modelTrace = new ArrayList<>();
		for (int i = 0; i < traceAlignment.getStepTypes().size(); i++) {
			// iterate through all the moves of the alignment and take only 
			// synchronous moves, model moves and invisible moves to get a 
			// complete firing sequence of the net as a representation of
			// the log trace
			
			StepTypes stepType = traceAlignment.getStepTypes().get(i);
			/**
			System.out.println("[EscapingEdgesPrecisionAlgorithm] Transition: " + transition.getLabel() + ", localID: " + transition.getLocalID() + 
					", steptype: " + stepType.toString());
			**/
			if (stepType.equals(StepTypes.LMGOOD) || stepType.equals(StepTypes.MINVI) ||
					stepType.equals(StepTypes.MREAL)) {
				// add transition
				Transition transition = (Transition) traceAlignment.getNodeInstance().get(i);
				modelTrace.add(transition.getLocalID().toString());
			}
		}
		return modelTrace;
	}
	
	/**
	 * Adding prefix of state to automaton
	 * @param automaton
	 * @param curState: current automaton state
	 * @param action: new action to add to prefix
	 */
	public EscapingEdgesPrecisionState addPrefix(EscapingEdgesPrecisionAutomaton automaton, EscapingEdgesPrecisionState curState, String action) {
		// check if automaton already contains prefix
		String prefixString = "";
		for (String previousAction: curState.getPrefix()) {
			prefixString += previousAction;
		}
		prefixString += action;
		
		EscapingEdgesPrecisionState state = automaton.getState(prefixString);
		if (state == null) {
			// state does not exist, create it
			List<String> statePrefix = new ArrayList<String>(curState.getPrefix());
			statePrefix.add(action);
			state = new EscapingEdgesPrecisionState(statePrefix, 1.0, true);
			automaton.addState(state, false);
			
			// add edge between current state and new state
			automaton.addEdge(curState, state, action);
		} else {
			// increment state weight by 1
			state.setWeight(state.getWeight() + 1);
		}
		
		// System.out.println("[EscapingEdgesPrecisionAlgorithm] Adding (" + state.getPrefixString() + "), weight: " + state.getWeight());
		
		return state;
	}
	
	/**
	 * Add the states of the model trace to automaton
	 * @param modelTrace: a list of localId of transitions
	 * @param automaton: automaton to build onto
	 */
	public void buildAutomaton(List<String> modelTrace, EscapingEdgesPrecisionAutomaton automaton) {
		if (automaton == null) {
			return;
		}
		
		// check for root state, if it does not exist, add it
		EscapingEdgesPrecisionState rootState = automaton.getRoot();
		if (rootState == null) {
			List<String> rootPrefix = new ArrayList<String>();
			rootPrefix.add(EscapingEdgesPrecisionState.EPSILON);
			rootState = new EscapingEdgesPrecisionState(rootPrefix, 0.0, true);
			
			automaton.addState(rootState, false);
			automaton.setRoot(rootState);
		} 
		
		// increment root state weight
		rootState.setWeight(rootState.getWeight() + 1);
		
		EscapingEdgesPrecisionState curState = rootState;
		for (int i = 0; i < modelTrace.size(); i++) {
			// an executed transition as an action
			String action = modelTrace.get(i);
			curState = addPrefix(automaton, curState, action);
		}
	}

	/**
	 * Get the transition by string nodeId
	 * @param localId: local id of transition
	 * @param transitions: list of transitions
	 * @return Transition with nodeId or null
	 */
	public Transition getTransition(String localId, List<Transition> transitions) {
		for (Transition transition: transitions) {
			if (transition.getLocalID().toString().equals(localId)) {
				return transition;
			}
		}
		return null;
	}
	
	/**
	 * Replay petrinet semantics to the marking after transition is fired
	 * @param semantics: petrinet semantics
	 * @param transition: transition to be fired
	 * @param marking: marking in which transition is enabled
	 * @throws IllegalTransitionException 
	 * @return Marking after transition is fired
	 */
	public Marking replayNetToPrefix(PetrinetSemantics semantics, Transition transition, Marking marking) throws IllegalTransitionException {
		// update to marking
		semantics.setCurrentState(marking);
		semantics.executeExecutableTransition(transition);
		return semantics.getCurrentState();
	}
	
	/**
	 * Replay automaton on net to add escaping arcs 
	 * 
	 * @param automaton: automaton
	 * @param net: accepting petrinet
	 * @throws IllegalTransitionException 
	 */	
	public void addEscapingArcs(EscapingEdgesPrecisionAutomaton automaton, AcceptingPetriNet net) throws IllegalTransitionException {
		EscapingEdgesPrecisionState rootState = automaton.getRoot();
		if (rootState == null) {
			return;
		}
		
		PetrinetSemantics sem = PetrinetSemanticsFactory.regularPetrinetSemantics(Petrinet.class);
		sem.initialize(net.getNet().getTransitions(), net.getInitialMarking());
		List<Transition> transitions = new ArrayList<>(net.getNet().getTransitions());
		
		// create a state marking pair for root state
		// Pair<AutomatonState, Marking> initialStateMarking = new Pair<AutomatonState, Marking>(rootState, net.getInitialMarking());
		
		List<Pair<EscapingEdgesPrecisionState, Marking>> queue = new ArrayList<>();
		
		Pair<EscapingEdgesPrecisionState, Marking> curStateMarking = null;
		List<EscapingEdgesPrecisionEdge> connectingEdges = null;
		EscapingEdgesPrecisionState curState = null;
		// current marking is the initial marking, before firing of any transitions
		Marking curMarking = net.getInitialMarking();
		String curNodeId = null;
		Transition curTransition = null;
		
		connectingEdges = rootState.getEdges();
		for (EscapingEdgesPrecisionEdge edge: connectingEdges) {
			// the destination state of edge
			EscapingEdgesPrecisionState destState = edge.getDestination();
			if (!destState.equals(rootState)) {
				// add to queue with current marking
				Pair<EscapingEdgesPrecisionState, Marking> stateMarking = new Pair<>(destState, curMarking);
				queue.add(stateMarking);
			}
		}
		
		while (!queue.isEmpty()) {
			curStateMarking = queue.remove(0);
			curState = curStateMarking.getFirst();
			curMarking = curStateMarking.getSecond();
			
			// System.out.println("[EscapingEdgesPrecisionAlgorithm] Adding escaping arcs of (" + curState.getPrefixString() + ")");
			
			curNodeId = curState.getPrefix().get(curState.getPrefix().size() - 1);
			curTransition = getTransition(curNodeId, transitions);
			// replay the net semantics to the marking after current transition is fired
			// update current marking
			curMarking = replayNetToPrefix(sem, curTransition, curMarking);
			
			if (curMarking.equals(net.getFinalMarkings())) {
				// reached final marking, no more executable transitions
				continue;
			}
			
			// get available actions
			Iterator<Transition> executableTransitions = sem.getExecutableTransitions().iterator();
			
			while (executableTransitions.hasNext()) {
				Transition executableTransition = executableTransitions.next();
				String availableActionPrefixString = curState.getPrefixString() + executableTransition.getLocalID().toString();
				if (automaton.getState(availableActionPrefixString) == null) {
					// add as the state of an escaping arc
					List<String> availableActionPrefix = new ArrayList<>(curState.getPrefix());
					availableActionPrefix.add(executableTransition.getLocalID().toString());
					EscapingEdgesPrecisionState escapingArcState = new EscapingEdgesPrecisionState(availableActionPrefix, 0.0, false);
					automaton.addState(escapingArcState, false);
					
					// add edge connecting current state to available state
					automaton.addEdge(curState, escapingArcState, executableTransition.getLocalID().toString());
				}
			}

			// add all connecting states that are executed
			connectingEdges = curState.getEdges();
			for (EscapingEdgesPrecisionEdge edge: connectingEdges) {
				// the destination state of edge
				EscapingEdgesPrecisionState destState = edge.getDestination();
				// check that destination state is not the current state and is executed, i.e., not destination to an escaping arc
				if (!destState.equals(curState) && destState.isExecuted()) {
					// add to queue with current marking
					Pair<EscapingEdgesPrecisionState, Marking> stateMarking = new Pair<>(destState, curMarking);
					queue.add(stateMarking);
				}
			}
		}
	}
	
	/**
	 * Compute the escaping arcs precision using a set of executed states and available states considering their weights
	 * @param executedStates: a set of executed states
	 * @param availableStates: a set of available states
	 * @return: precision value
	 */
	public double computeEscapingArcsPrecision(EscapingEdgesPrecisionAutomaton automaton) {
		Set<String> stateKeys = automaton.getStateKeys();
		
		double weightedExecuted = 0.0;
		double weightedAvailable = 0.0;
		
		for (String stateKey: stateKeys) {
			EscapingEdgesPrecisionState state = automaton.getState(stateKey);
			double weight = state.getWeight();
			// number of connecting states that are executed, i.e., not escaping arcs
			int stateExecuted = 0;
			// number of connecting states that are available, this is the number of outEdges connecting to this state
			int stateAvailable = 0;
			for (int i = 0; i < state.getEdgeCount(); i++) {
				EscapingEdgesPrecisionEdge edge = state.getEdge(i);
				EscapingEdgesPrecisionState destState = edge.getDestination();
				if (!destState.equals(state)) {
					stateAvailable += 1;
					if (destState.isExecuted()) {
						// an executed state
						stateExecuted += 1;
					}
				}
			}
			/**
			System.out.println("[EscapingEdgesPrecisionAlgorithm] (" + stateKey + "), weight: " + weight + 
					", executed: " + stateExecuted + ", available: " + stateAvailable);
			**/
			// add to weighted executed
			weightedExecuted += (weight * stateExecuted);
			// add to weighted available
			weightedAvailable += (weight * stateAvailable);
		}
		
		System.out.println("[EscapingEdgesPrecisionAlgorithm] Weighted executed sum: " + weightedExecuted);
		System.out.println("[EscapingEdgesPrecisionAlgorithm] Weighted available sum: " + weightedAvailable);
		// number of executed states is a subset of available states
		assert weightedExecuted <= weightedAvailable;
		
		// save the weighted executed and available states
		result.setWeightedAvailable(weightedAvailable);
		result.setWeightedExecuted(weightedExecuted);
		
		return weightedExecuted / weightedAvailable;
	}
	
	public EscapingEdgesPrecisionResult apply(PluginContext context, PNRepResult alignments, AcceptingPetriNet net,
			EscapingEdgesPrecisionParameters parameters) throws IllegalTransitionException {
		EscapingEdgesPrecisionAutomaton automaton = new EscapingEdgesPrecisionAutomaton();
		
		System.out.println("[EscapingEdgesPrecisionAlgorithm] Building automaton with alignments...");
		for (SyncReplayResult traceAlignment: alignments) {
			if (traceAlignment.isReliable()) {
				// only go through alignments that are reliable
				List<String> modelTrace = getModelTrace(traceAlignment);
				buildAutomaton(modelTrace, automaton);
			}
		}
		
		System.out.println("[EscapingEdgesPrecisionAlgorithm] Adding escaping arcs to automaton...");
		addEscapingArcs(automaton, net);

		result = new EscapingEdgesPrecisionResult();
		
		double precision = computeEscapingArcsPrecision(automaton);
				
		result.setPrecision(precision);
		result.setAutomaton(automaton);
		
		System.out.println("[EscapingEdgesPrecisionAlgorithm] Number of states: " + automaton.getStateKeys().size());
		System.out.println("[EscapingEdgesPrecisionAlgorithm] Precision: " + precision);
		
		return result;
	}
}
