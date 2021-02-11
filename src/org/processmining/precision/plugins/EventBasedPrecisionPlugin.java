package org.processmining.precision.plugins;

import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginCategory;
import org.processmining.framework.plugin.annotations.PluginLevel;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.models.semantics.IllegalTransitionException;
import org.processmining.plugins.petrinet.replayresult.PNRepResult;
import org.processmining.precision.algorithms.EventBasedPrecisionAlgorithm;
import org.processmining.precision.help.EventBasedPrecisionHelp;
import org.processmining.precision.models.EventBasedPrecision;
import org.processmining.precision.parameters.EventBasedPrecisionParameters;

@Plugin(name = "Check Event-based Precision", categories = { PluginCategory.ConformanceChecking }, level = PluginLevel.PeerReviewed, parameterLabels = {
		"Alignments", "Accepting Petri net", "Parameters" }, returnLabels = { "Precision" }, returnTypes = { EventBasedPrecision.class }, help = EventBasedPrecisionHelp.TEXT)
public class EventBasedPrecisionPlugin extends EventBasedPrecisionAlgorithm {

	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "H.M.W. Verbeek", email = "h.m.w.verbeek@tue.nl")
	@PluginVariant(variantLabel = "Check Event-based Precision, Default", requiredParameterLabels = { 0, 1 })
	public EventBasedPrecision runDefault(PluginContext context, PNRepResult alignments, AcceptingPetriNet net) throws IllegalTransitionException {
		EventBasedPrecisionParameters parameters = new EventBasedPrecisionParameters(net);
		return runParameters(context, alignments, net, parameters);
	}

	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "H.M.W. Verbeek", email = "h.m.w.verbeek@tue.nl")
	@PluginVariant(variantLabel = "Check Event-based Precision, No info", requiredParameterLabels = { 0, 1 })
	public EventBasedPrecision runNoInfo(PluginContext context, PNRepResult alignments, AcceptingPetriNet net) throws IllegalTransitionException {
		EventBasedPrecisionParameters parameters = new EventBasedPrecisionParameters(net);
		parameters.setShowInfo(false);
		return runParameters(context, alignments, net, parameters);
	}

	public EventBasedPrecision runParameters(PluginContext context, PNRepResult alignments, AcceptingPetriNet net, EventBasedPrecisionParameters parameters) throws IllegalTransitionException {
		return apply(context, alignments, net, parameters);
	}
}
