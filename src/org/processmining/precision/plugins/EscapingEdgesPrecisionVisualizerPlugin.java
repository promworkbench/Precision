package org.processmining.precision.plugins;

import javax.swing.JComponent;
import javax.swing.JLabel;

import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.precision.models.EscapingEdgesPrecisionResult;

@Plugin(name = "Visualize Escaping Arcs Precision Result", returnLabels = { "Visualize Escaping Arcs Precision Result" }, returnTypes = { JComponent.class }, parameterLabels = { "Escaping Arcs Precision Result" }, userAccessible = true)
@Visualizer
public class EscapingEdgesPrecisionVisualizerPlugin {

	@PluginVariant(requiredParameterLabels = { 0 })
	public static JComponent visualize(UIPluginContext context, EscapingEdgesPrecisionResult result) {
		return new JLabel(result.getPrecision() + "");
	}
}
