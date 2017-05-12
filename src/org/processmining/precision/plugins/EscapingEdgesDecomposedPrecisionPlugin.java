package org.processmining.precision.plugins;

import org.deckfour.uitopia.api.event.TaskListener.InteractionResult;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginCategory;
import org.processmining.framework.plugin.annotations.PluginLevel;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.models.semantics.IllegalTransitionException;
import org.processmining.plugins.petrinet.replayresult.PNRepResult;
import org.processmining.precision.algorithms.EscapingEdgesDecomposedPrecisionAlgorithm;
import org.processmining.precision.dialogs.EscapingEdgesDecomposedPrecisionDialog;
import org.processmining.precision.help.EscapingEdgesPrecisionHelp;
import org.processmining.precision.models.EscapingEdgesDecomposedPrecisionResult;
import org.processmining.precision.models.EscapingEdgesPrecisionResult;
import org.processmining.precision.parameters.EscapingEdgesDecomposedPrecisionParameters;

@Plugin(name = "Check Decomposed Precision using Escaping Edges", 
categories = { PluginCategory.ConformanceChecking }, 
level = PluginLevel.PeerReviewed, 
parameterLabels = { "Alignments", "Accepting Petri net","Parameters" }, 
returnLabels = { "EscapingEdges Decomposed Precision Result" }, 
returnTypes = { EscapingEdgesPrecisionResult.class }, 
help = EscapingEdgesPrecisionHelp.TEXT)
public class EscapingEdgesDecomposedPrecisionPlugin extends EscapingEdgesDecomposedPrecisionAlgorithm {

	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "H.M.W. Verbeek", email = "h.m.w.verbeek@tue.nl")
	@PluginVariant(variantLabel = "Check Precision using Escaping Arcs, UI", requiredParameterLabels = { 0, 1 })
	public EscapingEdgesDecomposedPrecisionResult runUI(UIPluginContext context, PNRepResult alignments, AcceptingPetriNet net) throws IllegalTransitionException {
		EscapingEdgesDecomposedPrecisionParameters parameters = new EscapingEdgesDecomposedPrecisionParameters(alignments, net);
		EscapingEdgesDecomposedPrecisionDialog dialog = new EscapingEdgesDecomposedPrecisionDialog();
		int n = 0;
		String[] title = { "Dialog 0" };
		InteractionResult result = InteractionResult.NEXT;
		while (result != InteractionResult.FINISHED) {
			result = context.showWizard(title[n], n == 0, n == 0, dialog.getPanel(context, alignments, net, parameters, n));
			if (result == InteractionResult.NEXT) {
				n++;
			} else if (result == InteractionResult.PREV) {
				n--;
			} else if (result == InteractionResult.FINISHED) {
			} else {
				return null;
			}
		}
		return runParameters(context, alignments, net, parameters);
	}
	
	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "H.M.W. Verbeek", email = "h.m.w.verbeek@tue.nl")
	@PluginVariant(variantLabel = "Check Precision using Escaping Arcs, Default", requiredParameterLabels = { 0, 1 })
	public EscapingEdgesDecomposedPrecisionResult runDefault(PluginContext context, PNRepResult alignments, AcceptingPetriNet net) throws IllegalTransitionException {
		EscapingEdgesDecomposedPrecisionParameters parameters = new EscapingEdgesDecomposedPrecisionParameters(alignments, net);
		return runParameters(context, alignments, net, parameters);
	}

	public EscapingEdgesDecomposedPrecisionResult runParameters(PluginContext context, PNRepResult alignments, AcceptingPetriNet net, EscapingEdgesDecomposedPrecisionParameters parameters) throws IllegalTransitionException {
		return apply(context, alignments, net, parameters);
	}
}
