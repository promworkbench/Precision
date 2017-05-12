package org.processmining.precision.models;

public class EscapingEdgesPrecisionEdge {

	private EscapingEdgesPrecisionState source, destination;
	private String action;
	
	public EscapingEdgesPrecisionEdge(EscapingEdgesPrecisionState source, EscapingEdgesPrecisionState destination, String action) {
		this.source = source;
		this.destination = destination;
		this.action = action;
	}
	
	public EscapingEdgesPrecisionState getNeighbor(EscapingEdgesPrecisionState v) {
		if (!(v.equals(source) || v.equals(destination))) {
			// not source nor destination 
			return null;
		}
		
		// return opposite vertex
		return (v.equals(source)) ? destination : source;
	}
	
	public EscapingEdgesPrecisionState getSource() {
		return this.source;
	}
	
	public EscapingEdgesPrecisionState getDestination() {
		return this.destination;
	}
	
	public String getAction() {
		return this.action;
	}
	
    @Override
    public String toString() {
            return "([" + source.getPrefix() + ", " + destination.getPrefix() + "], " + action + ")";
    }

    /**
     * Hashcode of action edge is hashcode of the concatenated transition labels of the source state 
     * prefix and the destination state prefix
     * 
     */
	@Override
	public int hashCode() {
		String concat = source.getPrefixString() + destination.getPrefixString();
		return concat.hashCode();
	}
	
	public boolean equals(Object other) {
		if (!(other instanceof EscapingEdgesPrecisionEdge)) 
			return false;
		
		EscapingEdgesPrecisionEdge edge = (EscapingEdgesPrecisionEdge) other;
		return edge.source.equals(this.source) && edge.destination.equals(this.destination) 
				&& (edge.action == this.action);
	}
}
