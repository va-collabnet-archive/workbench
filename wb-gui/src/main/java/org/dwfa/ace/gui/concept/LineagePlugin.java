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
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import javax.swing.AbstractAction;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import org.dwfa.ace.ACE;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_HostConceptPlugins;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.I_HostConceptPlugins.TOGGLES;
import org.dwfa.ace.dnd.TerminologyTransferHandler;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.tree.JTreeWithDragImage;
import org.dwfa.ace.tree.LineageTreeCellRenderer;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.tk.api.RelAssertionType;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.util.swing.GuiUtil;

public class LineagePlugin extends AbstractPlugin implements HierarchyListener {

    private static final long serialVersionUID = 1L;
    private static final int dataVersion = 1;
    private transient JTreeWithDragImage lineageTree;
    private transient JComponent lineagePanel;
    private transient LineageTreeCellRenderer lineageRenderer;
    private JToggleButton statedInferredButton;
    private ViewCoordinate coordinate;

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
    private static ImageIcon statedViewIcon = null;
    private static ImageIcon inferredViewIcon = null;
    

    private ImageIcon getStatedIcon() {
        if (statedViewIcon == null) {
            statedViewIcon = new ImageIcon(LineagePlugin.class.getResource("/16x16/plain/graph_edge.png"));
        }
        return statedViewIcon;
    }
    private ImageIcon getInferredIcon() {
        if (inferredViewIcon == null) {
            inferredViewIcon = new ImageIcon(LineagePlugin.class.getResource("/16x16/plain/chrystal_ball.png"));
        }
        return inferredViewIcon;
    }
    public LineagePlugin(boolean shownByDefault, int sequence) {
        super(shownByDefault, sequence);
     }

    @Override
    protected ImageIcon getImageIcon() {
        return new ImageIcon(ACE.class.getResource("/24x24/plain/nav_up_right_green.png"));
    }

    @Override
    public void update() throws IOException, TerminologyException {
        if (getHost() != null) {
            updateLineageModel();
        }
    }

    @Override
    public JComponent getComponent(I_HostConceptPlugins host) throws TerminologyException {
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

    private JComponent getLineagePanel(I_HostConceptPlugins host) throws IOException, TerminologyException {
        setHost(host);
        JPanel newLineagePanel = new JPanel(new GridBagLayout());
        newLineagePanel.addHierarchyListener(this);
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.WEST;
        c.gridx = 0;
        c.gridy = 0;
        c.fill = GridBagConstraints.NONE;
        c.gridwidth = 2;
        JLabel lineageLabel = new JLabel("Lineage:");
        lineageLabel.setBorder(BorderFactory.createEmptyBorder(3, 5, 3, 0));
        newLineagePanel.add(lineageLabel, c);
        c.gridwidth = 1;

        statedInferredButton = new JToggleButton(new AbstractAction("", getStatedIcon()) {

            @Override
            public void actionPerformed(ActionEvent e) {
                JToggleButton button = (JToggleButton) e.getSource();
                if (button.isSelected()) {
                    button.setIcon(getStatedIcon());
                    button.setToolTipText("showing stated, toggle to show inferred...");
                    try {
                        update();
                    } catch (Exception ex) {
                        AceLog.getAppLog().alertAndLogException(ex);
                    }
                } else {
                    button.setIcon(getInferredIcon());
                    button.setToolTipText("showing inferred, toggle to show stated...");
                    try {
                        update();
                    } catch (Exception ex) {
                        AceLog.getAppLog().alertAndLogException(ex);
                    }
                }
            }
        });
        statedInferredButton.setSelected(true);
        statedInferredButton.setPreferredSize(new Dimension(21, 16));
        statedInferredButton.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        statedInferredButton.setOpaque(false);
        statedInferredButton.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));

        c.gridx++;
        c.gridx++;
        newLineagePanel.add(statedInferredButton, c);
        c.anchor = GridBagConstraints.SOUTHEAST;
        lineageTree = new JTreeWithDragImage(host.getConfig());
        lineageTree.putClientProperty("JTree.lineStyle", "Angled");
        lineageTree.setLargeModel(true);
        lineageTree.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        lineageTree.setTransferHandler(new TerminologyTransferHandler(lineageTree));
        lineageTree.setDragEnabled(false);
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
        newLineagePanel.add(filler, c);
        c.gridx++;
        c.weightx = 0.1;
        c.weighty = 0.0;
        newLineagePanel.add(lineageTree, c);
        lineageTree.setBorder(BorderFactory.createMatteBorder(1, 1, 0, 0, Color.GRAY));

        JPanel eastFiller = new JPanel();
        eastFiller.setMaximumSize(new Dimension(40, 20));
        eastFiller.setMinimumSize(new Dimension(40, 20));
        eastFiller.setPreferredSize(new Dimension(40, 20));
        eastFiller.setBackground(Color.white);
        eastFiller.setOpaque(true);
        c.weightx = 0.0;
        c.gridx++;
        eastFiller.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.GRAY));
        newLineagePanel.add(eastFiller, c);

        newLineagePanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(1, 1, 1, 3), BorderFactory.createLineBorder(Color.GRAY)));
        return newLineagePanel;
    }

    private void updateLineageModel() throws IOException, TerminologyException {
        DefaultTreeModel model = (DefaultTreeModel) lineageTree.getModel();
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("ROOT");
        model.setRoot(root);
        RelAssertionType relAssertionType = RelAssertionType.INFERRED;
        if (statedInferredButton.isSelected()) {
            relAssertionType = RelAssertionType.STATED;
        }
        coordinate = new ViewCoordinate(getHost().getConfig().getViewCoordinate());
        coordinate.setRelAssertionType(relAssertionType);

        I_GetConceptData bean = (I_GetConceptData) getHost().getTermComponent();
        if (bean != null) {
            lineageRenderer.setFocusBean(bean);
            List<List<I_GetConceptData>> lineage = getLineage(bean, 0);
            if (AceLog.getAppLog().isLoggable(Level.FINE)) {
                StringBuilder buf = new StringBuilder();
                buf.append("Lineage for: ").append(bean);
                for (List<I_GetConceptData> parentLine : lineage) {
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
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    GuiUtil.tickle(lineageTree);
                    GuiUtil.tickle(lineagePanel);
                }
            });
        } else {
            root.add(new DefaultMutableTreeNode(" "));
            model.nodeStructureChanged(root);
        }
    }

    private void addLineageToNode(List<List<I_GetConceptData>> lineage, DefaultMutableTreeNode root) {
        Map<I_GetConceptData, DefaultMutableTreeNode> childrenNodes =
                new HashMap<I_GetConceptData, DefaultMutableTreeNode>();
        Map<I_GetConceptData, List<List<I_GetConceptData>>> childrenLineage =
                new HashMap<I_GetConceptData, List<List<I_GetConceptData>>>();
        for (List<I_GetConceptData> parentLine : lineage) {
            childrenNodes.put(parentLine.get(0), new DefaultMutableTreeNode(parentLine.get(0)));
            if (childrenLineage.get(parentLine.get(0)) == null) {
                childrenLineage.put(parentLine.get(0), new ArrayList<List<I_GetConceptData>>());
            }
            if (parentLine.size() > 1) {
                List<I_GetConceptData> shortenedLineage = new ArrayList<I_GetConceptData>(parentLine);
                shortenedLineage.remove(0);
                childrenLineage.get(parentLine.get(0)).add(shortenedLineage);
            }
        }
        for (I_GetConceptData childBean : childrenNodes.keySet()) {
            DefaultMutableTreeNode childNode = childrenNodes.get(childBean);
            root.add(childNode);
            if (childrenLineage.get(childBean).size() > 0) {
                addLineageToNode(childrenLineage.get(childBean), childNode);
            }
        }
    }

    private List<List<I_GetConceptData>> getLineage(I_GetConceptData bean, int depth) throws IOException,
            TerminologyException {
        List<List<I_GetConceptData>> lineage = new ArrayList<List<I_GetConceptData>>();

        List<? extends I_RelTuple> sourceRelTuples =
                bean.getSourceRelTuples(getHost().getConfig().getAllowedStatus(), getHost().getConfig().getDestRelTypes(),
                getHost().getConfig().getViewPositionSetReadOnly(), getHost().getConfig().getPrecedence(),
                getHost().getConfig().getConflictResolutionStrategy(),
                coordinate.getClassifierNid(), coordinate.getRelAssertionType());
        if ((sourceRelTuples.size() > 0) && (depth < 40)) {
            for (I_RelTuple rel : sourceRelTuples) {
                I_GetConceptData parent = Terms.get().getConcept(rel.getC2Id());
                List<List<I_GetConceptData>> parentLineage = getLineage(parent, depth + 1);
                for (List<I_GetConceptData> parentLine : parentLineage) {
                    parentLine.add(bean);
                    lineage.add(parentLine);
                }
            }
        } else {
            lineage.add(new ArrayList<I_GetConceptData>(Arrays.asList(new I_GetConceptData[]{bean})));
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

    @Override
    public UUID getId() {
        return TOGGLES.LINEAGE.getPluginId();
    }

    @Override
    public void hierarchyChanged(HierarchyEvent e) {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                try {
                    updateLineageModel();
                } catch (IOException e) {
                    AceLog.getAppLog().alertAndLogException(e);
                } catch (TerminologyException e) {
                    AceLog.getAppLog().alertAndLogException(e);
                }
            }
        });
    }
}
