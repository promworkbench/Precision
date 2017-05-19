package org.processmining.precision.models;

import org.processmining.framework.util.HTMLToString;

public class EventBasedPrecision implements HTMLToString {

	private long nofEvents;
	private double sumPrecision;
	
	public EventBasedPrecision() {
		nofEvents = 0;
		sumPrecision = 0.0;
	}
	
	public double getPrecision() {
		return nofEvents == 0 ? 0.0 : sumPrecision / nofEvents;
	}
	
	public double getSumPrecision() {
		return sumPrecision;
	}

	public void addSumPrecision(double eventPrecision) {
		this.sumPrecision += eventPrecision;
	}

	public long getNofEvents() {
		return nofEvents;
	}

	public void addNofEvents(long nofEvents) {
		this.nofEvents += nofEvents;
	}

	public String toHTMLString(boolean includeHTMLTags) {
		StringBuffer buf = new StringBuffer();
		if (includeHTMLTags) {
			buf.append("<html>");
		}
		buf.append("<h1>Event-based Precision</h1>");
		buf.append("<p>" + getPrecision() + "</p>");
		buf.append("<h2>Number of Events (<i>E</i>)</h2>");
		buf.append("<p>" + nofEvents + "</p>");
		buf.append("<h2>Sum of Precision Fractions (&sum;<sub><i>e</i> &isin; <i>E</i></sub><i>en<sub>L</sub></i>(<i>e</i>)/<i>en<sub>M</sub></i>(<i>e</i>))</h2>");
		buf.append("<p>" + sumPrecision + "</p>");
		buf.append("<hr/>");
		if (includeHTMLTags) {
			buf.append("</html>");
		}
		return buf.toString();
	}
	
	
}
