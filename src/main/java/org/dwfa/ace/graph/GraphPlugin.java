package org.dwfa.ace.graph;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.dwfa.ace.ACE;
import org.dwfa.ace.SmallProgressPanel;
import org.dwfa.ace.api.I_HostConceptPlugins;
import org.dwfa.ace.gui.concept.AbstractPlugin;

public class GraphPlugin extends AbstractPlugin {


	public enum GRAPH_LAYOUTS {
		DAGLayout, FRLayout, FRLayout2, ISOMLayout, KKLayout, SpringLayout, SpringLayout2, AceGraphLayout
	};

	private I_HostConceptPlugins host;

	private JPanel graphWrapperPanel = new JPanel(new GridBagLayout());
	private GraphPanel graphPanel;
	private JPanel fillerPanel;
	private GridBagConstraints gbc = new GridBagConstraints();
	private boolean initialize = true;
	private GRAPH_LAYOUTS graphLayout = GRAPH_LAYOUTS.AceGraphLayout;

	public GraphPlugin() {
		super(true);
	}

	@Override
	protected ImageIcon getImageIcon() {
		return new ImageIcon(ACE.class
				.getResource("/24x24/plain/graph_edge_directed.png"));
	}

	@Override
	public void update() throws IOException {
		if (host != null) {
			if (fillerPanel != null) {
				graphWrapperPanel.remove(fillerPanel);
			}
			graphPanel = new GraphPanel(graphLayout, this, graphWrapperPanel.getSize());
			graphWrapperPanel.add(graphPanel, gbc);
			SwingUtilities.invokeLater(new Runnable() {

				public void run() {
					graphPanel.validate();
					graphWrapperPanel.validate();
					graphWrapperPanel.getParent().validate();
				}
				
			});
			fillerPanel = graphPanel;
		}
	}

	public JComponent getComponent(I_HostConceptPlugins host) {
		if (initialize) {
			gbc.anchor = GridBagConstraints.WEST;
			gbc.gridx = 0;
			gbc.gridy = 0;
			gbc.weightx = 0;
			gbc.weighty = 0;
			gbc.fill = GridBagConstraints.NONE;
			gbc.gridwidth = 2;
			JLabel lineageLabel = new JLabel("Lineage graph:");
			lineageLabel.setBorder(BorderFactory.createEmptyBorder(3, 5, 3, 0));
			graphWrapperPanel.add(lineageLabel, gbc);

			SmallProgressPanel lineageProgress = new SmallProgressPanel();
			lineageProgress.setVisible(false);
			gbc.gridwidth = 1;
			gbc.anchor = GridBagConstraints.SOUTHEAST;
			gbc.gridx++;
			graphWrapperPanel.add(lineageProgress, gbc);

			gbc.anchor = GridBagConstraints.EAST;
			gbc.gridx = 0;
			gbc.gridy++;
			gbc.fill = GridBagConstraints.BOTH;
			gbc.gridwidth = 2;
			gbc.weightx = 1.0;
			gbc.weighty = 1.0;
			gbc.fill = GridBagConstraints.BOTH;
			fillerPanel = new JPanel();
			graphWrapperPanel.add(fillerPanel, gbc);
			initialize = false;
		}
		if (host != this.host) {
			host.addPropertyChangeListener(I_HostConceptPlugins.TERM_COMPONENT,
					this);
			host.addPropertyChangeListener("commit", this);
			this.host = host;
		}
		return graphWrapperPanel;
	}

	@Override
	protected String getToolTipText() {
		return "show/hide the lineage graph of this concept";
	}

	@Override
	protected int getComponentId() {
		return Integer.MIN_VALUE;
	}

	public GRAPH_LAYOUTS getGraphLayout() {
		return graphLayout;
	}

	public void setGraphLayout(GRAPH_LAYOUTS graphLayout) {
		this.graphLayout = graphLayout;
	}

	public I_HostConceptPlugins getHost() {
		return host;
	}


}
