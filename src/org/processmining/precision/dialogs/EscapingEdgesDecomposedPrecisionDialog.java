package org.processmining.precision.dialogs;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.DefaultListModel;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.acceptingpetrinetdecomposer.strategies.DecompositionStrategy;
import org.processmining.acceptingpetrinetdecomposer.strategies.DecompositionStrategyManager;
import org.processmining.acceptingpetrinetdecomposer.strategies.impl.DecompositionReplaceReduceStrategy;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.util.ui.widgets.ProMList;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.plugins.petrinet.replayresult.PNRepResult;
import org.processmining.plugins.replayer.replayresult.SyncReplayResult;
import org.processmining.precision.parameters.EscapingEdgesDecomposedPrecisionParameters;

public class EscapingEdgesDecomposedPrecisionDialog extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1362161023783365024L;

	public JPanel getPanel(PluginContext context, PNRepResult alignments, AcceptingPetriNet net,
			final EscapingEdgesDecomposedPrecisionParameters parameters, int n) {
		removeAll();

		if (n == 0) {
			double size[][] = { { TableLayoutConstants.FILL, TableLayoutConstants.FILL }, { TableLayoutConstants.FILL } };
			setLayout(new TableLayout(size));

			setOpaque(false);

			final Set<XEventClass> activities = getActivities(alignments);
			
			List<XEventClass> activityList = new ArrayList<XEventClass>();
			activityList.addAll(activities);
			Collections.sort(activityList);
			DefaultListModel<XEventClass> activityListModel = new DefaultListModel<XEventClass>();
			for (XEventClass activity : activityList) {
				activityListModel.addElement(activity);
			}
			final ProMList<XEventClass> aList = new ProMList<XEventClass>("Select decomposable activities", activityListModel);
			aList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

//			AcceptingPetriNet seseNet = (new SESEAlgorithm()).apply(context, net);
//			Collection<XEventClass> seseActivities = new HashSet<XEventClass>();
//			for (Transition transition : seseNet.getNet().getTransitions()) {
//				if (transition.getAttributeMap().get(AttributeMap.FILLCOLOR) == Color.GREEN
//						|| transition.getAttributeMap().get(AttributeMap.FILLCOLOR) == Color.RED) {
//					seseActivities.add(new XEventClass(transition.getLabel(), 0));
//				}
//			}

			// Preselect all, to retain old behavior if no action is taken here.
//			int[] indices = new int[seseActivities.size()];
			int[] indices = new int[activityListModel.getSize()];
			int j = 0;
			for (int i = 0; i < activityListModel.getSize(); i++) {
//				if (seseActivities.contains(listModel.get(i))) {
					indices[j++] = i;
				}
//			}
			aList.setSelectedIndices(indices);
			Set<XEventClass> unsplittableActivities = new HashSet<XEventClass>(activities);
			unsplittableActivities.removeAll(aList.getSelectedValuesList());
			parameters.setUnsplittableActivities(unsplittableActivities);
			aList.addListSelectionListener(new ListSelectionListener() {
				public void valueChanged(ListSelectionEvent e) {
					Set<XEventClass> unsplittableActivities = new HashSet<XEventClass>(activities);
					unsplittableActivities.removeAll(aList.getSelectedValuesList());
					System.out.println("[DecomposedPrecisionDialog] Unsplittable = " + unsplittableActivities);
					parameters.setUnsplittableActivities(unsplittableActivities);
				}
			});
			aList.setPreferredSize(new Dimension(100, 100));
			add(aList, "1, 0");

			DefaultListModel<String> listModel = new DefaultListModel<String>();
			for (DecompositionStrategy strategy : DecompositionStrategyManager.getInstance().getStrategies()) {
				/*
				 * Reduction strategy poses problems for this decomposed precision strategy. The main question is,
				 * which transitions to filter in while filtering a monolithic alignment onto a subnet. 
				 */
				if (!strategy.getName().equals(DecompositionReplaceReduceStrategy.NAME)) {
					listModel.addElement(strategy.getName());
				}
			}
			final ProMList<String> list = new ProMList<String>("Select decomposition strategy", listModel);
			list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			final String defaultStrategy = parameters.getStrategy();
			list.setSelection(defaultStrategy);
			list.addListSelectionListener(new ListSelectionListener() {
				public void valueChanged(ListSelectionEvent e) {
					List<String> selected = list.getSelectedValuesList();
					if (selected.size() == 1) {
						parameters.setStrategy(selected.get(0));
					} else {
						/*
						 * Nothing selected. Revert to selection of default
						 * classifier.
						 */
						list.setSelection(defaultStrategy);
						parameters.setStrategy(defaultStrategy);
					}
				}
			});
			list.setPreferredSize(new Dimension(100, 100));
			add(list, "0, 0");

		}

		return this;
	}

	private Set<XEventClass> getActivities(PNRepResult alignments) {
		Set<XEventClass> activities = new HashSet<XEventClass>();
		for (SyncReplayResult alignment : alignments) {
			for (int i = 0; i < alignment.getStepTypes().size(); i++) {
				switch (alignment.getStepTypes().get(i)) {
					case L: {
						activities.add((XEventClass) alignment.getNodeInstance().get(i));
						break;
					}
					case LMGOOD: {
						activities.add(new XEventClass(((Transition) alignment.getNodeInstance().get(i)).getLabel(), 0));
						break;
					}
					default: {
						break;
					}
				}
			}
		}
		return activities;
	}
}
