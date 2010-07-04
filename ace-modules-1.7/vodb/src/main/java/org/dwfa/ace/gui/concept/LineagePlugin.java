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
package org.dwfa.ace.gui.concept;

import java.awt.Color;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import org.dwfa.ace.ACE;
import org.dwfa.ace.SmallProgressPanel;
import org.dwfa.ace.api.I_HostConceptPlugins;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_HostConceptPlugins.TOGGLES;
import org.dwfa.ace.dnd.TerminologyTransferHandler;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.tree.JTreeWithDragImage;
import org.dwfa.ace.tree.LineageTreeCellRenderer;
import org.dwfa.vodb.types.ConceptBean;

public class LineagePlugin extends AbstractPlugin {

    private static final long serialVersionUID = 1L;
    private static final int dataVersion = 1;

    private transient JTreeWithDragImage lineageTree;
    private transient JComponent lineagePanel;
    private transient LineageTreeCellRenderer lineageRenderer;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == dataVersion) {
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

    public LineagePlugin(boolean shownByDefault, int sequence) {
        super(shownByDefault, sequence);
    }

    @Override
    protected ImageIcon getImageIcon() {
        return new ImageIcon(ACE.class.getResource("/24x24/plain/nav_up_right_green.png"));
    }

    @Override
    public void update() throws IOException {
        if (getHost() != null) {
            updateLineageModel();
        }
    }

    public JComponent getComponent(I_HostConceptPlugins host) {
        if (lineagePanel == null) {
            setHost(host);
            try {
                lineagePanel = getLineagePanel(host);
                host.addPropertyChangeListener(I_HostConceptPlugins.TERM_COMPONENT, this);
                host.addPropertyChangeListener("commit", this);
                updateLineageModel();
            } catch (IOException e) {
                AceLog.getAppLog().alertAndLog(null, Level.SEVERE, "Database Exception: " + e.getLocalizedMessage(), e);
            }
        }
        return lineagePanel;
    }

    private JComponent getLineagePanel(I_HostConceptPlugins host) throws IOException {
        setHost(host);
        JPanel lineagePanel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.WEST;
        c.gridx = 0;
        c.gridy = 0;
        c.fill = GridBagConstraints.NONE;
        c.gridwidth = 2;
        JLabel lineageLabel = new JLabel("Lineage:");
        lineageLabel.setBorder(BorderFactory.createEmptyBorder(3, 5, 3, 0));
        lineagePanel.add(lineageLabel, c);

        SmallProgressPanel lineageProgress = new SmallProgressPanel();
        lineageProgress.setVisible(false);
        c.gridwidth = 1;
        c.anchor = GridBagConstraints.SOUTHEAST;
        c.gridx++;
        lineagePanel.add(lineageProgress, c);

        lineageTree = new JTreeWithDragImage(host.getConfig());
        lineageTree.putClientProperty("JTree.lineStyle", "Angled");
        // lineageTree.putClientProperty("JTree.lineStyle", "None");
        lineageTree.setLargeModel(true);
        lineageTree.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        lineageTree.setTransferHandler(new TerminologyTransferHandler(lineageTree));
        lineageTree.setDragEnabled(true);
        lineageRenderer = new LineageTreeCellRenderer(host.getConfig());
        lineageTree.setCellRenderer(lineageRenderer);
        lineageTree.setRootVisible(false);
        lineageTree.setShowsRootHandles(false);
        updateLineageModel();

        c.anchor = GridBagConstraints.EAST;
        c.gridx = 0;
        c.gridy++;
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 0.0;
        c.weighty = 0.0;
        c.gridwidth = 1;
        JPanel filler = new JPanel();
        filler.setMaximumSize(new Dimension(40, 20));
        filler.setMinimumSize(new Dimension(40, 20));
        filler.setPreferredSize(new Dimension(40, 20));
        lineagePanel.add(filler, c);
        c.gridx++;
        c.weightx = 1.0;
        c.weighty = 0.0;
        lineagePanel.add(lineageTree, c);
        lineageTree.setBorder(BorderFactory.createMatteBorder(1, 1, 0, 0, Color.GRAY));

        JPanel filler2 = new JPanel();
        filler2.setMaximumSize(new Dimension(40, 20));
        filler2.setMinimumSize(new Dimension(40, 20));
        filler2.setPreferredSize(new Dimension(40, 20));
        filler2.setBackground(Color.white);
        filler2.setOpaque(true);
        c.weightx = 0.0;
        c.gridx++;
        filler2.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.GRAY));
        lineagePanel.add(filler2, c);

        lineagePanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(1, 1, 1, 3),
            BorderFactory.createLineBorder(Color.GRAY)));
        return lineagePanel;
    }

    private void updateLineageModel() throws IOException {
        DefaultTreeModel model = (DefaultTreeModel) lineageTree.getModel();
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("ROOT");
        model.setRoot(root);

        ConceptBean bean = (ConceptBean) getHost().getTermComponent();
        if (bean != null) {
            lineageRenderer.setFocusBean(bean);
            List<List<ConceptBean>> lineage = getLineage(bean, 0);
            if (AceLog.getAppLog().isLoggable(Level.FINE)) {
                StringBuffer buf = new StringBuffer();
                buf.append("Lineage for: " + bean);
                for (List<ConceptBean> parentLine : lineage) {
                    buf.append("\n");
                    buf.append(parentLine);
                }
                AceLog.getAppLog().fine(buf.toString());
            }
            addLineageToNode(lineage, root);
            model.nodeStructureChanged(root);
            for (int i = 0; i < 100; i++) {
                lineageTree.expandRow(i);
            }
        } else {
            root.add(new DefaultMutableTreeNode(" "));
            model.nodeStructureChanged(root);
        }
    }

    private void addLineageToNode(List<List<ConceptBean>> lineage, DefaultMutableTreeNode root) {
        Map<ConceptBean, DefaultMutableTreeNode> childrenNodes = new HashMap<ConceptBean, DefaultMutableTreeNode>();
        Map<ConceptBean, List<List<ConceptBean>>> childrenLineage = new HashMap<ConceptBean, List<List<ConceptBean>>>();
        for (List<ConceptBean> parentLine : lineage) {
            childrenNodes.put(parentLine.get(0), new DefaultMutableTreeNode(parentLine.get(0)));
            if (childrenLineage.get(parentLine.get(0)) == null) {
                childrenLineage.put(parentLine.get(0), new ArrayList<List<ConceptBean>>());
            }
            if (parentLine.size() > 1) {
                List<ConceptBean> shortenedLineage = new ArrayList<ConceptBean>(parentLine);
                shortenedLineage.remove(0);
                childrenLineage.get(parentLine.get(0)).add(shortenedLineage);
            }
        }
        for (ConceptBean childBean : childrenNodes.keySet()) {
            DefaultMutableTreeNode childNode = childrenNodes.get(childBean);
            root.add(childNode);
            if (childrenLineage.get(childBean).size() > 0) {
                addLineageToNode(childrenLineage.get(childBean), childNode);
            }
        }
    }

    private List<List<ConceptBean>> getLineage(ConceptBean bean, int depth) throws IOException {
        List<List<ConceptBean>> lineage = new ArrayList<List<ConceptBean>>();

        List<I_RelTuple> sourceRelTuples = bean.getSourceRelTuples(getHost().getConfig().getAllowedStatus(),
            getHost().getConfig().getDestRelTypes(), getHost().getConfig().getViewPositionSet(), false);
        if ((sourceRelTuples.size() > 0) && (depth < 40)) {
            for (I_RelTuple rel : sourceRelTuples) {
                ConceptBean parent = ConceptBean.get(rel.getC2Id());
                List<List<ConceptBean>> parentLineage = getLineage(parent, depth + 1);
                for (List<ConceptBean> parentLine : parentLineage) {
                    parentLine.add(bean);
                    lineage.add(parentLine);
                }
            }
        } else {
            lineage.add(new ArrayList<ConceptBean>(Arrays.asList(new ConceptBean[] { bean })));
        }
        return lineage;
    }

    @Override
    protected String getToolTipText() {
        return "show/hide the lineage of this concept";
    }

    @Override
    protected int getComponentId() {
        return Integer.MIN_VALUE;
    }

    public UUID getId() {
        return TOGGLES.LINEAGE.getPluginId();
    }

}
