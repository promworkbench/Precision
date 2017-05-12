package org.processmining.precision.models;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class EscapingEdgesPrecisionAutomaton {

	// an initial root node
		private EscapingEdgesPrecisionState root;
		// prefix of states is used as key
		private Map<String, EscapingEdgesPrecisionState> states;
		// hashcode of edges is used as key
		private Map<Integer, EscapingEdgesPrecisionEdge> edges;
		
		public EscapingEdgesPrecisionAutomaton(List<EscapingEdgesPrecisionState> states) {
			this.root = null;
			this.states = new HashMap<>();
			this.edges = new HashMap<>();
			
			for (EscapingEdgesPrecisionState state: states) {
				// the prefix of states should be unique, order of the transition 
				// labels of the prefix matters
				this.states.put(state.getPrefixString(), state);
			}
		}
		
		public EscapingEdgesPrecisionAutomaton() {
			this.root = null;
			this.states = new HashMap<>();
			this.edges = new HashMap<>();
		}
		
		public void setRoot(EscapingEdgesPrecisionState state) {
			if (!containsState(state)) {
				throw new IllegalArgumentException("Root state is not contained in automaton.");
			}
			this.root = state;
		}
		
		public EscapingEdgesPrecisionState getRoot() {
			return this.root;
		}
		
		public boolean addEdge(EscapingEdgesPrecisionState source, EscapingEdgesPrecisionState destination, String action) {
			if (source.equals(destination)) {
				return false;
			}
			
			// ensure edge is not in the automaton already
			EscapingEdgesPrecisionEdge edge = new EscapingEdgesPrecisionEdge(source, destination, action);
			if (edges.containsKey(edge.hashCode())) {
				return false;
			}
			
			// check that s1 does not already have an edge with s2
			if (source.containsEdge(edge) || destination.containsEdge(edge)) {
				return false;
			}
			
			// add edge to automaton and the two states
			edges.put(edge.hashCode(), edge);
			source.addEdge(edge);
			destination.addEdge(edge);
			
			// add the states if they are not contained in the automaton already
			if (!this.containsState(source))
				states.put(source.getPrefixString(), source);
			if (!this.containsState(destination))
				states.put(destination.getPrefixString(), destination);
			
			return true;
		}
		
		public boolean containsEdge(EscapingEdgesPrecisionEdge edge) {
			if (edge.getSource() == null || edge.getDestination() == null)
				return false;
			
			return edges.containsKey(edge.hashCode());
		}
		
		public EscapingEdgesPrecisionEdge removeEdge(EscapingEdgesPrecisionEdge edge) {
			edge.getSource().removeEdge(edge);
			edge.getDestination().removeEdge(edge);
			return this.edges.remove(edge.hashCode());
		}
		
		public boolean containsState(EscapingEdgesPrecisionState state) {
			return (states.get(state.getPrefixString()) != null);
		}
		
		public EscapingEdgesPrecisionState getState(String prefixString) {
			return states.get(prefixString);
		}
		
		public boolean addState(EscapingEdgesPrecisionState state, boolean overwritingExisting) {
			// check if automaton already has this state
			EscapingEdgesPrecisionState current = states.get(state.getPrefixString());
			
			if (current != null) {
				if (!overwritingExisting) {
					return false;
				}
				
				// need to remove the edges of the current state
				while (current.getEdgeCount() > 0) {
					this.removeEdge(current.getEdge(0));
				}
			}
			
			states.put(state.getPrefixString(), state);
			return true;
		}
		
		public EscapingEdgesPrecisionState removeState(String prefixString) {
			EscapingEdgesPrecisionState state = states.remove(prefixString);
			
			if (state != null) {
				while (state.getEdgeCount() > 0) {
					this.removeEdge(state.getEdge(0));
				}
			}
			
			return state;
		}
		
		public Set<String> getStateKeys() {
			return states.keySet();
		}
		
		public Set<EscapingEdgesPrecisionState> getStates() {
			return new HashSet<EscapingEdgesPrecisionState>(states.values());
		}
		
		public Set<EscapingEdgesPrecisionEdge> getEdges() {
			return new HashSet<EscapingEdgesPrecisionEdge>(edges.values());
		}}
