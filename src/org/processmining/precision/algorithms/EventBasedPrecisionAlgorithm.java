package org.processmining.precision.algorithms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
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

	/*
	 * Mapping from #hist(e) to enL(e)
	 */
	private Map<List<String>, Set<String>> enL;
	/*
	 * Mapping from #hist(e) to enM(e)
	 */
	private Map<List<String>, Set<String>> enM;
	/*
	 * Mapping from state in the net to set of enabled activities. Used as
	 * cache.
	 */
	private Map<Marking, Set<String>> enA;
	/*
	 * The resulting precision.
	 */
	private EventBasedPrecision precision;

	public EventBasedPrecisionAlgorithm() {
		/*
		 * Initialize.
		 */
		enL = new HashMap<List<String>, Set<String>>();
		enM = new HashMap<List<String>, Set<String>>();
		enA = new HashMap<Marking, Set<String>>();
		precision = new EventBasedPrecision();
	}

	/*
	 * Get the event-based precision given the alignments and the net (apn).
	 * ssumption is that the net was used to create the alignments (the
	 * transitions in the alignments should be transitions from this net).
	 */
	public EventBasedPrecision apply(PluginContext context, PNRepResult alignments, AcceptingPetriNet apn,
			EventBasedPrecisionParameters parameters) throws IllegalTransitionException {
		/*
		 * First, construct enL en enM.
		 */
		for (SyncReplayResult alignment : alignments) {
			apply(context, alignment, apn, parameters);
		}
		/*
		 * Second, compute precision based on constructed enL en enM.
		 */
		for (SyncReplayResult alignment : alignments) {
			apply(context, alignment);
		}
		/*
		 * Return precision.
		 */
		return precision;
	}

	/*
	 * Extend enL en enM with the given alignment.
	 */
	private void apply(PluginContext context, SyncReplayResult alignment, AcceptingPetriNet apn,
			EventBasedPrecisionParameters parameters) throws IllegalTransitionException {
		/*
		 * Current state in the net.
		 */
		Marking state = apn.getInitialMarking();
		/*
		 * Current history in alignment. Initially empty.
		 */
		List<String> hist = new ArrayList<String>();

		for (int i = 0; i < alignment.getStepTypes().size(); i++) {
			/*
			 * Get current step type an dnode instance.
			 */
			StepTypes stepType = alignment.getStepTypes().get(i);
			Object nodeInstance = alignment.getNodeInstance().get(i);
			/*
			 * Make sure history is in enL and enM.
			 */
			if (!enL.containsKey(hist)) {
				enL.put(new ArrayList<String>(hist), new HashSet<String>());
				enM.put(new ArrayList<String>(hist), new HashSet<String>());
			}

			switch (stepType) {
				case MREAL : {
					Transition transition = (Transition) nodeInstance;
					/*
					 * From the current history, the log can do the activity
					 * associated with this transition.
					 */
					enL.get(hist).add(transition.getLabel());
					/*
					 * Find all enabled activities from the current state.
					 */
					enM.get(hist).addAll(getEnabledActivities(state, new HashSet<Transition>(), parameters));
					/*
					 * Update state by executing transition.
					 */
					parameters.getSemantics().setCurrentState(state);
					parameters.getSemantics().executeExecutableTransition(transition);
					state = new Marking(parameters.getSemantics().getCurrentState());
					break;
				}
				case LMGOOD : {
					Transition transition = (Transition) nodeInstance;
					/*
					 * From the current history, the log can do the activity
					 * associated with this transition.
					 */
					enL.get(hist).add(transition.getLabel());
					/*
					 * Find all enabled activities from the current state.
					 */
					enM.get(hist).addAll(getEnabledActivities(state, new HashSet<Transition>(), parameters));
					/*
					 * Update state by executing transition.
					 */
					parameters.getSemantics().setCurrentState(state);
					parameters.getSemantics().executeExecutableTransition(transition);
					state = new Marking(parameters.getSemantics().getCurrentState());
					/*
					 * Update history.
					 */
					hist.add(transition.getLabel());
					break;
				}
				case MINVI : {
					Transition transition = (Transition) nodeInstance;
					/*
					 * Find all enabled activities from the current state.
					 */
					enM.get(hist).addAll(getEnabledActivities(state, new HashSet<Transition>(), parameters));
					/*
					 * Update state by executing transition.
					 */
					parameters.getSemantics().setCurrentState(state);
					parameters.getSemantics().executeExecutableTransition(transition);
					state = new Marking(parameters.getSemantics().getCurrentState());
					break;
				}
				case L : {
					/*
					 * Update history.
					 */
					XEventClass activity = (XEventClass) nodeInstance;
					hist.add(activity.getId());
					break;
				}
				default :
			}
		}
	}

	/*
	 * Get the enabled activities form the given state.
	 */
	private Set<String> getEnabledActivities(Marking state, Set<Transition> transitions,
			EventBasedPrecisionParameters parameters) throws IllegalTransitionException {
		/*
		 * Check cache.
		 */
		if (enA.containsKey(state)) {
			/*
			 * Already in cache. Do not recompute.
			 */
			return enA.get(state);
		}
		/*
		 * Not in cache. Compute. Start with empty set of activities.
		 */
		Set<String> activities = new HashSet<String>();
		/*
		 * Set current state.
		 */
		parameters.getSemantics().setCurrentState(state);
		/*
		 * Get transitions enabled in current state.
		 */
		Set<Transition> executableTransitions = new HashSet<Transition>(parameters.getSemantics()
				.getExecutableTransitions());
		/*
		 * Execute these transitions one after the other.
		 */
		for (Transition transition : executableTransitions) {
			/*
			 * Cycle check
			 */
			if (!transitions.contains(transitions)) {
				transitions.add(transition);
				if (transition.isInvisible()) {
					/*
					 * Silent transition. Execute and look further.
					 */
					parameters.getSemantics().executeExecutableTransition(transition);
					/*
					 * Add all activities enabled from the state after this
					 * silent transition has been executed.
					 */
					activities.addAll(getEnabledActivities(new Marking(parameters.getSemantics().getCurrentState()),
							transitions, parameters));
					/*
					 * Reset the current state for the next transition.
					 */
					parameters.getSemantics().setCurrentState(state);
				} else {
					/*
					 * Visible transition. Add corresponding activity.
					 */
					activities.add(transition.getLabel());
				}
			}
		}
		precision.addInfo("State = " + state + ", activities = " + activities);
		/*
		 * Cache the result.
		 */
		enA.put(state, activities);
		/*
		 * Return the result.
		 */
		return activities;
	}

	/*
	 * Given enL and enM, extend the precision with the given alignment.
	 */
	private void apply(PluginContext context, SyncReplayResult alignment) {
		/*
		 * History is initally empty.
		 */
		List<String> hist = new ArrayList<String>();
		/*
		 * Alignment corresponds to this many traces in event log.
		 */
		int n = alignment.getTraceIndex().size();

		for (int i = 0; i < alignment.getStepTypes().size(); i++) {
			/*
			 * Get step type an dnode instance.
			 */
			StepTypes stepType = alignment.getStepTypes().get(i);
			Object nodeInstance = alignment.getNodeInstance().get(i);

			/*
			 * Check whether we're at some event, and whether it makes sense.
			 */
			if ((stepType == StepTypes.L || stepType == StepTypes.LMGOOD) && enM.get(hist).size() > 0) {
				/*
				 * Add as amny events to the precision as there were traces
				 * corresponding to this alignment.
				 */
				precision.addNofEvents(n);
				/*
				 * Extend the precision in a similar way. By definition of the
				 * precision.
				 */
				double eventPrecision = n * (((double) enL.get(hist).size()) / enM.get(hist).size());
				precision.addSumPrecision(eventPrecision);
				/*
				 * Output mismatches, that is, if precision drops. Could be
				 * useful diagnostic information.
				 */
				if (!enL.get(hist).equals(enM.get(hist))) {
					precision.addInfo("History = " + hist + ", enL = " + enL.get(hist) + ", enM = " + enM.get(hist));
					precision.addInfo("Number of Events = " + n + ", Precision for Events = " + eventPrecision);
				}
			}

			/*
			 * Update the history.
			 */
			switch (stepType) {
				case MREAL : {
					break;
				}
				case LMGOOD : {
					Transition transition = (Transition) nodeInstance;
					hist.add(transition.getLabel());
					break;
				}
				case MINVI : {
					break;
				}
				case L : {
					XEventClass activity = (XEventClass) nodeInstance;
					hist.add(activity.getId());
					break;
				}
				default :
			}
		}
	}

}
