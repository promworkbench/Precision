package org.processmining.precision.algorithms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.semantics.IllegalTransitionException;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.petrinet.replayresult.PNRepResult;
import org.processmining.plugins.petrinet.replayresult.StepTypes;
import org.processmining.plugins.replayer.replayresult.SyncReplayResult;
import org.processmining.precision.models.EventBasedPrecision;
import org.processmining.precision.parameters.EventBasedPrecisionParameters;

public class EventBasedPrecisionAlgorithm {

	private Map<List<String>, Set<String>> enL;
	private Map<List<String>, Set<String>> enM;

	public EventBasedPrecisionAlgorithm() {
		enL = new HashMap<List<String>, Set<String>>();
		enM = new HashMap<List<String>, Set<String>>();
	}

	public EventBasedPrecision apply(PluginContext context, PNRepResult alignments, AcceptingPetriNet apn,
			EventBasedPrecisionParameters parameters) throws IllegalTransitionException {
		EventBasedPrecision precision = new EventBasedPrecision();
		for (SyncReplayResult alignment : alignments) {
			apply(context, alignment, apn, parameters);
		}
		for (SyncReplayResult alignment : alignments) {
			apply(context, alignment, apn, precision, parameters);
		}
		return precision;
	}

	private void apply(PluginContext context, SyncReplayResult alignment, AcceptingPetriNet apn,
			EventBasedPrecisionParameters parameters) throws IllegalTransitionException {
		Marking state = apn.getInitialMarking();
		List<String> hist = new ArrayList<String>();

		for (int i = 0; i < alignment.getStepTypes().size(); i++) {
			StepTypes stepType = alignment.getStepTypes().get(i);
			Object nodeInstance = alignment.getNodeInstance().get(i);
			if (!enL.containsKey(hist)) {
				enL.put(new ArrayList<String>(hist), new HashSet<String>());
				enM.put(new ArrayList<String>(hist), new HashSet<String>());
			}

			switch (stepType) {
				case MREAL : {
					Transition transition = (Transition) nodeInstance;
					enL.get(hist).add(transition.getLabel());
					enM.get(hist).addAll(getEnabledActivities(state, new HashSet<Transition>(), parameters));
					parameters.getSemantics().setCurrentState(state);
					parameters.getSemantics().executeExecutableTransition(transition);
					state = new Marking(parameters.getSemantics().getCurrentState());
					hist.add(transition.getLabel());
					break;
				}
				case LMGOOD : {
					Transition transition = (Transition) nodeInstance;
					enL.get(hist).add(transition.getLabel());
					enM.get(hist).addAll(getEnabledActivities(state, new HashSet<Transition>(), parameters));
					parameters.getSemantics().setCurrentState(state);
					parameters.getSemantics().executeExecutableTransition(transition);
					state = new Marking(parameters.getSemantics().getCurrentState());
					hist.add(transition.getLabel());
					break;
				}
				case MINVI : {
					Transition transition = (Transition) nodeInstance;
					parameters.getSemantics().setCurrentState(state);
					parameters.getSemantics().executeExecutableTransition(transition);
					state = new Marking(parameters.getSemantics().getCurrentState());
					break;
				}
				case L : {
//					XEventClass activity = (XEventClass) nodeInstance;
//					enL.get(hist).add(activity.getId());
//					enM.get(hist).addAll(getEnabledActivities(state, new HashSet<Transition>(), parameters));
//					hist.add(activity.getId());
					break;
				}
				default :
			}
		}
	}

	private Set<String> getEnabledActivities(Marking state, Set<Transition> transitions,
			EventBasedPrecisionParameters parameters) throws IllegalTransitionException {
		Set<String> activities = new HashSet<String>();
		parameters.getSemantics().setCurrentState(state);
		for (Transition transition : parameters.getSemantics().getExecutableTransitions()) {
			if (!transitions.contains(transitions)) {
				transitions.add(transition);
				if (transition.isInvisible()) {
					parameters.getSemantics().executeExecutableTransition(transition);
					activities.addAll(getEnabledActivities(new Marking(parameters.getSemantics().getCurrentState()),
							transitions, parameters));
					parameters.getSemantics().setCurrentState(state);
				} else {
					activities.add(transition.getLabel());
				}
			}
		}
		System.out.println("[EventBasedPrecisionAlgorithm] State = " + state + ", activities = " + activities);
		return activities;
	}

	private void apply(PluginContext context, SyncReplayResult alignment, AcceptingPetriNet apn,
			EventBasedPrecision precision, EventBasedPrecisionParameters parameters) throws IllegalTransitionException {
		Marking state = apn.getInitialMarking();
		List<String> hist = new ArrayList<String>();
		int n = alignment.getTraceIndex().size();

		for (int i = 0; i < alignment.getStepTypes().size(); i++) {
			StepTypes stepType = alignment.getStepTypes().get(i);
			Object nodeInstance = alignment.getNodeInstance().get(i);

			precision.addNofEvents(n);
			System.out.println("[EventBasedPrecisionAlgorithm] Hist = " + hist + ", enL = " + enL.get(hist) + ", enM = " + enM.get(hist));
			precision.addSumPrecision(n * (((double) enL.get(hist).size()) / enM.get(hist).size()));
			System.out.println("[EventBasedPrecisionAlgorithm] NofEvents = " + precision.getNofEvents() + ", SumPrecision = " + precision.getSumPrecision());

			switch (stepType) {
				case MREAL : {
					Transition transition = (Transition) nodeInstance;
					parameters.getSemantics().setCurrentState(state);
					parameters.getSemantics().executeExecutableTransition(transition);
					state = new Marking(parameters.getSemantics().getCurrentState());
					hist.add(transition.getLabel());
					break;
				}
				case LMGOOD : {
					Transition transition = (Transition) nodeInstance;
					parameters.getSemantics().setCurrentState(state);
					parameters.getSemantics().executeExecutableTransition(transition);
					state = new Marking(parameters.getSemantics().getCurrentState());
					hist.add(transition.getLabel());
					break;
				}
				case MINVI : {
					Transition transition = (Transition) nodeInstance;
					parameters.getSemantics().setCurrentState(state);
					parameters.getSemantics().executeExecutableTransition(transition);
					state = new Marking(parameters.getSemantics().getCurrentState());
					break;
				}
				case L : {
//					XEventClass activity = (XEventClass) nodeInstance;
//					hist.add(activity.getId());
					break;
				}
				default :
			}
		}
	}

}
