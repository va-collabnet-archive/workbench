/**
 * Copyright (c) 2009 International Health Terminology Standards Development
 * Organisation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dwfa.ace.graph;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.UUID;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.dwfa.ace.ACE;
import org.dwfa.ace.SmallProgressPanel;
import org.dwfa.ace.api.I_HostConceptPlugins;
import org.dwfa.ace.api.I_HostConceptPlugins.TOGGLES;
import org.dwfa.ace.gui.concept.AbstractPlugin;
import org.dwfa.tapi.TerminologyException;

public class GraphPlugin extends AbstractPlugin {

    public enum GRAPH_LAYOUTS {
        DAGLayout, FRLayout, ISOMLayout, KKLayout, SpringLayout, AceGraphLayout
    };

    private static final long serialVersionUID = 1L;
    private static final int dataVersion = 1;

    private GRAPH_LAYOUTS graphLayout = GRAPH_LAYOUTS.KKLayout;

    private transient JPanel graphWrapperPanel = new JPanel(new GridBagLayout());
    private transient JPanel graphPanel;
    private transient JPanel fillerPanel;
    private transient GridBagConstraints gbc = new GridBagConstraints();
    private transient boolean initialize = true;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(graphLayout);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == dataVersion) {
            graphLayout = (GRAPH_LAYOUTS) in.readObject();

            // transient
            graphWrapperPanel = new JPanel(new GridBagLayout());
            graphPanel = null;
            fillerPanel = null;
            gbc = new GridBagConstraints();
            initialize = true;
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

    public GraphPlugin(boolean shownByDefault, int sequence) {
        super(shownByDefault, sequence);
    }

    public UUID getId() {
        return TOGGLES.LINEAGE_GRAPH.getPluginId();
    }

    @Override
    protected ImageIcon getImageIcon() {
        return new ImageIcon(ACE.class.getResource("/24x24/plain/graph_edge_directed.png"));
    }

    @Override
    public void update() throws IOException, TerminologyException {
        if (getHost() != null) {
            if (fillerPanel != null) {
                graphWrapperPanel.remove(fillerPanel);
            }
            if (showComponent()) {
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
            } else {
                graphPanel = new JPanel();
            }
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
        if (host != getHost()) {
            host.addPropertyChangeListener(I_HostConceptPlugins.TERM_COMPONENT, this);
            host.addPropertyChangeListener("commit", this);
            setHost(host);
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
}
