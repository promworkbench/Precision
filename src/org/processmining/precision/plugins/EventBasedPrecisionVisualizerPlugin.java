package org.processmining.precision.plugins;

import javax.swing.JComponent;
import javax.swing.JLabel;

import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.precision.models.EventBasedPrecision;

@Plugin(name = "Visualize Event-based Precision", returnLabels = { "Visualize Event-based Precision" }, returnTypes = { JComponent.class }, parameterLabels = { "Event-based Precision" }, userAccessible = true)
@Visualizer
public class EventBasedPrecisionVisualizerPlugin {

	@PluginVariant(requiredParameterLabels = { 0 })
	public static JComponent visualize(UIPluginContext context, EventBasedPrecision precision) {
		return new JLabel(precision.getPrecision() + "");
	}

}
