package org.processmining.precision.dialogs;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import javax.swing.JPanel;

import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.plugins.petrinet.replayresult.PNRepResult;
import org.processmining.precision.parameters.EscapingEdgesPrecisionParameters;

public class EscapingEdgesPrecisionDialog extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 9221170896562732670L;

	public JPanel getPanel(PluginContext context, PNRepResult alignments, AcceptingPetriNet net,
			final EscapingEdgesPrecisionParameters parameters, int n) {
		removeAll();

		if (n == 0) {
			double size[][] = { { TableLayoutConstants.FILL }, { TableLayoutConstants.FILL } };
			setLayout(new TableLayout(size));

			setOpaque(false);

		}

		return this;
	}

}
