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
package org.dwfa.ace.refset;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JToggleButton;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import org.dwfa.ace.ACE;
import org.dwfa.ace.TermComponentLabel;
import org.dwfa.ace.api.I_AmTermComponent;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_HostConceptPlugins;
import org.dwfa.ace.api.I_PluginToConceptPanel;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCid;
import org.dwfa.ace.api.ebr.I_ExtendByRefVersion;
import org.dwfa.ace.config.AceFrameConfig;
import org.dwfa.ace.gui.concept.ConceptPanel;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.refset.spec.I_HelpSpecRefset;
import org.dwfa.ace.table.JTableWithDragImage;
import org.dwfa.ace.table.refset.ReflexiveRefsetCommentTableModel;
import org.dwfa.ace.table.refset.ReflexiveRefsetFieldData;
import org.dwfa.ace.table.refset.ReflexiveRefsetMemberTableModel;
import org.dwfa.ace.table.refset.ReflexiveRefsetUtil;
import org.dwfa.ace.table.refset.ReflexiveRefsetFieldData.INVOKE_ON_OBJECT_TYPE;
import org.dwfa.ace.table.refset.ReflexiveRefsetFieldData.REFSET_FIELD_TYPE;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.ace.task.WorkerAttachmentKeys;
import org.dwfa.ace.task.refset.spec.RefsetSpec;
import org.dwfa.ace.tree.TermTreeHelper;
import org.dwfa.bpa.BusinessProcess;
import org.dwfa.bpa.ExecutionRecord;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.etypes.EConcept;

public class RefsetSpecEditor implements I_HostConceptPlugins, PropertyChangeListener {

    private static EnumSet<EConcept.REFSET_TYPES> allowedTypes =
            EnumSet.of(EConcept.REFSET_TYPES.CID_CID, EConcept.REFSET_TYPES.CID_CID_CID, EConcept.REFSET_TYPES.CID_CID_STR);

    PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(propertyName, listener);
    }

    public boolean equals(Object obj) {
        return pcs.equals(obj);
    }

    public void fireIndexedPropertyChange(String propertyName, int index, boolean oldValue, boolean newValue) {
        pcs.fireIndexedPropertyChange(propertyName, index, oldValue, newValue);
    }

    public void fireIndexedPropertyChange(String propertyName, int index, int oldValue, int newValue) {
        pcs.fireIndexedPropertyChange(propertyName, index, oldValue, newValue);
    }

    public void fireIndexedPropertyChange(String propertyName, int index, Object oldValue, Object newValue) {
        pcs.fireIndexedPropertyChange(propertyName, index, oldValue, newValue);
    }

    public void firePropertyChange(PropertyChangeEvent evt) {
        pcs.firePropertyChange(evt);
    }

    public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) {
        pcs.firePropertyChange(propertyName, oldValue, newValue);
    }

    public void firePropertyChange(String propertyName, int oldValue, int newValue) {
        pcs.firePropertyChange(propertyName, oldValue, newValue);
    }

    public void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        pcs.firePropertyChange(propertyName, oldValue, newValue);
    }

    public PropertyChangeListener[] getPropertyChangeListeners() {
        return pcs.getPropertyChangeListeners();
    }

    public PropertyChangeListener[] getPropertyChangeListeners(String propertyName) {
        return pcs.getPropertyChangeListeners(propertyName);
    }

    public int hashCode() {
        return pcs.hashCode();
    }

    public boolean hasListeners(String propertyName) {
        return pcs.hasListeners(propertyName);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(propertyName, listener);
    }

    public String toString() {
        return pcs.toString();
    }

    public class RefsetSpecSelectionListener implements TreeSelectionListener {

        public void valueChanged(TreeSelectionEvent tse) {
            if (tse.getNewLeadSelectionPath() != null) {
                TreePath selectionPath = tse.getNewLeadSelectionPath();
                RefsetSpecTreeNode selectedNode = (RefsetSpecTreeNode) selectionPath.getLastPathComponent();
                I_ExtendByRef ext = (I_ExtendByRef) selectedNode.getUserObject();

                try {

                    EConcept.REFSET_TYPES extType = EConcept.REFSET_TYPES.nidToType(ext.getTypeId());

                    if (allowedTypes.contains(extType) == false) {
                        throw new Exception("Can't handle " + extType);
                    }

                    List<ReflexiveRefsetFieldData> columns = new ArrayList<ReflexiveRefsetFieldData>();

                    getDefaultSpecColumns(extType, columns);

                    if (extType == EConcept.REFSET_TYPES.CID_CID_CID) {
                        ReflexiveRefsetFieldData column3 = new ReflexiveRefsetFieldData();
                        column3.setColumnName("constraint");
                        column3.setCreationEditable(true);
                        column3.setUpdateEditable(false);
                        column3.setFieldClass(Number.class);
                        column3.setMin(5);
                        column3.setPref(175);
                        column3.setMax(2000);
                        column3.setInvokeOnObjectType(INVOKE_ON_OBJECT_TYPE.PART);
                        column3.setReadMethod(extType.getPartClass().getMethod("getC3id"));
                        column3.setWriteMethod(extType.getPartClass().getMethod("setC3id", int.class));
                        column3.setType(REFSET_FIELD_TYPE.COMPONENT_IDENTIFIER);
                        columns.add(column3);

                    } else if (extType == EConcept.REFSET_TYPES.CID_CID_STR) {
                        ReflexiveRefsetFieldData column3 = new ReflexiveRefsetFieldData();
                        column3.setColumnName("query string");
                        column3.setCreationEditable(true);
                        column3.setUpdateEditable(false);
                        column3.setFieldClass(String.class);
                        column3.setMin(5);
                        column3.setPref(175);
                        column3.setMax(2000);
                        column3.setInvokeOnObjectType(INVOKE_ON_OBJECT_TYPE.PART);
                        column3.setReadMethod(extType.getPartClass().getMethod("getStringValue"));
                        column3.setWriteMethod(extType.getPartClass().getMethod("setStringValue", String.class));
                        column3.setType(REFSET_FIELD_TYPE.STRING);
                        columns.add(column3);
                    }

                    ReflexiveRefsetFieldData column4 = new ReflexiveRefsetFieldData();
                    column4.setColumnName("status");
                    column4.setCreationEditable(true);
                    column4.setUpdateEditable(true);
                    column4.setFieldClass(Number.class);
                    column4.setMin(5);
                    column4.setPref(150);
                    column4.setMax(150);
                    column4.setInvokeOnObjectType(INVOKE_ON_OBJECT_TYPE.PART);
                    column4.setReadMethod(extType.getPartClass().getMethod("getStatusId"));
                    column4.setWriteMethod(extType.getPartClass().getMethod("setStatusId", int.class));
                    column4.setType(REFSET_FIELD_TYPE.CONCEPT_IDENTIFIER);
                    columns.add(column4);

                    if (historyButton.isSelected()) {

                        ReflexiveRefsetFieldData column5 = new ReflexiveRefsetFieldData();
                        column5.setColumnName("time");
                        column5.setCreationEditable(false);
                        column5.setUpdateEditable(false);
                        column5.setFieldClass(Number.class);
                        column5.setMin(5);
                        column5.setPref(150);
                        column5.setMax(150);
                        column5.setInvokeOnObjectType(INVOKE_ON_OBJECT_TYPE.PART);
                        column5.setReadMethod(extType.getPartClass().getMethod("getTime"));
                        column5.setWriteMethod(extType.getPartClass().getMethod("setTime", long.class));
                        column5.setType(REFSET_FIELD_TYPE.TIME);
                        columns.add(column5);

                        ReflexiveRefsetFieldData column6 = new ReflexiveRefsetFieldData();
                        column6.setColumnName("path");
                        column6.setCreationEditable(false);
                        column6.setUpdateEditable(false);
                        column6.setFieldClass(String.class);
                        column6.setMin(5);
                        column6.setPref(150);
                        column6.setMax(150);
                        column6.setInvokeOnObjectType(INVOKE_ON_OBJECT_TYPE.PART);
                        column6.setReadMethod(extType.getPartClass().getMethod("getPathId"));
                        column6.setWriteMethod(extType.getPartClass().getMethod("setPathId", int.class));
                        column6.setType(REFSET_FIELD_TYPE.CONCEPT_IDENTIFIER);
                        columns.add(column6);
                    }

                    ReflexiveRefsetMemberTableModel reflexiveModel =
                            new ReflexiveRefsetMemberTableModel(RefsetSpecEditor.this, columns
                                .toArray(new ReflexiveRefsetFieldData[columns.size()]));

                    reflexiveModel.setComponentId(ext.getMemberId());

                    reflexiveModel.getRowCount();
                    clauseTable.setModel(reflexiveModel);

                    int columnIndex = 0;
                    for (ReflexiveRefsetFieldData columnId : reflexiveModel.getColumns()) {
                        clauseTable.getColumnModel().getColumn(columnIndex).setIdentifier(columnId);
                        columnIndex++;
                    }

                } catch (Exception e) {
                    AceLog.getAppLog().alertAndLogException(e);
                }
            } else {
                try {
                    List<ReflexiveRefsetFieldData> columns = new ArrayList<ReflexiveRefsetFieldData>();
                    getDefaultSpecColumns(EConcept.REFSET_TYPES.CID_CID, columns);
                    ReflexiveRefsetMemberTableModel reflexiveModel =
                            new ReflexiveRefsetMemberTableModel(RefsetSpecEditor.this, columns
                                .toArray(new ReflexiveRefsetFieldData[columns.size()]));
                    reflexiveModel.setComponentId(Integer.MIN_VALUE);
                    reflexiveModel.getRowCount();
                    clauseTable.setModel(reflexiveModel);

                    int columnIndex = 0;
                    for (ReflexiveRefsetFieldData columnId : reflexiveModel.getColumns()) {
                        clauseTable.getColumnModel().getColumn(columnIndex).setIdentifier(columnId);
                        columnIndex++;
                    }
                } catch (Exception e) {
                    AceLog.getAppLog().alertAndLogException(e);
                }
            }
        }

    }

    /**
	 *
	 */
    private static final long serialVersionUID = 1L;

    private class UncommittedChangeListener implements PropertyChangeListener {

        public void propertyChange(PropertyChangeEvent arg0) {
            UpdateTreeSpec specUpdater = new UpdateTreeSpec(RefsetSpecEditor.this);
            specUpdater.execute();
        }
    }

    private class FixedToggleChangeActionListener implements ActionListener, PropertyChangeListener {

        public void actionPerformed(ActionEvent e) {
            perform();
        }

        private void perform() {
            firePropertyChange(I_HostConceptPlugins.SHOW_HISTORY, !historyButton.isSelected(), historyButton.isSelected());
            try {
                updateSpecTree(false);
                if (refsetSpecPanel != null && refsetSpecPanel.getRefsetTable() != null) {
                    refsetSpecPanel.getRefsetTable().repaint();
                }
            } catch (Exception e1) {
                AceLog.getAppLog().alertAndLog(contentPanel, Level.SEVERE,
                    "Database Exception: " + e1.getLocalizedMessage(), e1);
            }
        }

        public void propertyChange(PropertyChangeEvent arg0) {
            perform();
        }
    }

    private class LabelListener implements PropertyChangeListener {

        public void propertyChange(PropertyChangeEvent evt) {

            I_GetConceptData refset = (I_GetConceptData) getLabel().getTermComponent();
            if (refset != null) {
                RefsetSpec spec = new RefsetSpec(refset, true, ace.getAceFrameConfig());
                refsetStatusValueLabel.setText(spec.getOverallSpecStatusString());
                computeTypeValueLabel.setText(spec.getComputeTypeString());

                try {
                    boolean needsCompute = spec.needsCompute();
                    if (needsCompute) {
                        Long lastComputeTime = spec.getLastComputeTime();
                        if (lastComputeTime == null) {
                            computeStatusValueLabel.setText("never computed");
                            computeStatusValueLabel.setForeground(Color.red);
                        } else {
                            computeStatusValueLabel.setText("modified since last compute");
                            computeStatusValueLabel.setForeground(Color.red);
                        }
                    } else {
                        Long lastComputeTime = spec.getLastComputeTime();
                        if (lastComputeTime == null) {
                            computeStatusValueLabel.setText("never  computed");
                            computeStatusValueLabel.setForeground(Color.black);
                        } else {
                            computeStatusValueLabel.setText("unmodified since last compute");
                            computeStatusValueLabel.setForeground(Color.black);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    computeStatusValueLabel.setText("Unknown");
                    computeStatusValueLabel.setForeground(Color.black);
                    computeStatusValueLabel.setBorder(null);
                }

                try {
                    Long lastComputeTime = spec.getLastComputeTime();

                    if (lastComputeTime == null) {
                        lastComputeTimeValueLabel.setText("never");
                    } else {
                        lastComputeTimeValueLabel.setText("" + new Date(lastComputeTime));
                    }
                } catch (Exception e) {
                    lastComputeTimeValueLabel.setText("Error fetching last compute time");
                    e.printStackTrace();
                }

                try {
                    Collection<? extends I_ExtendByRef> extensions =
                            Terms.get().getRefsetExtensionMembers(refset.getConceptNid());
                    int count = 0;
                    I_HelpSpecRefset helper = Terms.get().getSpecRefsetHelper(ace.getAceFrameConfig());

                    for (I_ExtendByRef ext : extensions) {
                        List<? extends I_ExtendByRefVersion> tuples =
                                ext.getTuples(helper.getCurrentStatusIntSet(), ace.getAceFrameConfig()
                                    .getViewPositionSetReadOnly(), ace.getAceFrameConfig().getPrecedence(), ace
                                    .getAceFrameConfig().getConflictResolutionStrategy());
                        I_ExtendByRefVersion latestTuple = null;

                        for (I_ExtendByRefVersion currentTuple : tuples) {
                            if (latestTuple == null || latestTuple.getVersion() > currentTuple.getVersion()) {
                                latestTuple = currentTuple;
                            }
                        }

                        if (latestTuple != null) {
                            if (latestTuple.getMutablePart() instanceof I_ExtendByRefPartCid) {
                                if (latestTuple.getRefsetId() == refset.getConceptNid()) {
                                    if (helper.getCurrentStatusIntSet().contains(latestTuple.getStatusId())) {
                                        count++;
                                    }
                                }
                            }
                        }

                    }

                    memberCountValueLabel.setText("" + count);
                } catch (Exception e) {
                    memberCountValueLabel.setText("Error computing member count");
                    e.printStackTrace();
                }

                if (leftTogglePane != null) {
                    leftTogglePane.validate();
                    if (leftTogglePane.getParent() != null) {
                        leftTogglePane.getParent().validate();
                    }
                }
            }

            refsetSpecConcept = null;
            tempComponent = label.getTermComponent();
            if (label.getTermComponent() != null) {
                ace.getAceFrameConfig().setLastViewed((I_GetConceptData) label.getTermComponent());
                if (tabHistoryList.size() == 0) {
                    tabHistoryList.addFirst((I_GetConceptData) label.getTermComponent());
                } else if ((tabHistoryList.size() > 0)
                    && (label.getTermComponent().equals(tabHistoryList.getFirst()) == false)) {
                    tabHistoryList.addFirst((I_GetConceptData) label.getTermComponent());
                }
                while (tabHistoryList.size() > 20) {
                    tabHistoryList.removeLast();
                }
            }
            updateSpecTree(false);
            commentTableModel.fireTableDataChanged();

            if (treeHelper.getRenderer() != null) {
                treeHelper.getRenderer().propertyChange(
                    new PropertyChangeEvent(this, "showRefsetInfoInTaxonomy", null, null));
                treeHelper.getRenderer().propertyChange(
                    new PropertyChangeEvent(this, "variableHeightTaxonomyView", null, null));
                treeHelper.getRenderer().propertyChange(
                    new PropertyChangeEvent(this, "highlightConflictsInTaxonomyView", null, null));
                treeHelper.getRenderer().propertyChange(
                    new PropertyChangeEvent(this, "showViewerImagesInTaxonomy", null, null));
                treeHelper.getRenderer().propertyChange(new PropertyChangeEvent(this, "refsetsToShow", null, null));
            } else {
                AceLog.getAppLog().info("treeHelper.getRenderer() == null");
            }
            if (refsetTree.getRenderer() != null) {
                refsetTree.getRenderer().propertyChange(
                    new PropertyChangeEvent(this, "showRefsetInfoInTaxonomy", null, null));
                refsetTree.getRenderer().propertyChange(
                    new PropertyChangeEvent(this, "variableHeightTaxonomyView", null, null));
                refsetTree.getRenderer().propertyChange(
                    new PropertyChangeEvent(this, "highlightConflictsInTaxonomyView", null, null));
                refsetTree.getRenderer().propertyChange(
                    new PropertyChangeEvent(this, "showViewerImagesInTaxonomy", null, null));
                refsetTree.getRenderer().propertyChange(new PropertyChangeEvent(this, "refsetsToShow", null, null));
            } else {
                AceLog.getAppLog().info("treeHelper.getRenderer() == null");
            }
            firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
        }
    }

    private class ShowHistoryListener implements ActionListener {

        private class ShowHistoryAction extends AbstractAction {

            /**
			 *
			 */
            private static final long serialVersionUID = 1L;
            I_GetConceptData concept;

            public ShowHistoryAction(I_GetConceptData concept) {
                super(concept.toString());
                this.concept = concept;
            }

            public void actionPerformed(ActionEvent e) {
                RefsetSpecEditor.this.setTermComponent(concept);
            }

        }

        public void actionPerformed(ActionEvent e) {
            if (tabHistoryList.size() > 1) {
                JPopupMenu popup = new JPopupMenu();
                List<I_GetConceptData> historyToRemove = new ArrayList<I_GetConceptData>();
                for (I_GetConceptData historyItem : tabHistoryList) {
                    try {
                        if (Terms.get().getUids(historyItem.getConceptNid()) != null) {
                            JMenuItem menuItem = new JMenuItem(new ShowHistoryAction(historyItem));
                            popup.add(menuItem);
                        } else {
                            historyToRemove.add(historyItem);
                        }
                    } catch (IOException e1) {
                        historyToRemove.add(historyItem);
                    } catch (TerminologyException e2) {
                        historyToRemove.add(historyItem);
                    }
                }
                tabHistoryList.removeAll(historyToRemove);
                Point mouseLocation = MouseInfo.getPointerInfo().getLocation();
                SwingUtilities.convertPointFromScreen(mouseLocation, contentPanel);
                popup.show(contentPanel, mouseLocation.x, mouseLocation.y);
            }
        }
    }

    ACE ace;

    private JLabel refsetStatusValueLabel;

    private JLabel computeTypeValueLabel;

    private JLabel computeStatusValueLabel;

    private JLabel lastComputeTimeValueLabel;

    private JLabel memberCountValueLabel;

    private LinkedList<I_GetConceptData> tabHistoryList;

    private ArrayList<org.dwfa.ace.api.I_PluginToConceptPanel> plugins;

    TermComponentLabel label;

    private PropertyChangeListener labelListener = new LabelListener();

    private FixedToggleChangeActionListener fixedToggleChangeActionListener;

    JToggleButton historyButton;

    private static final String TAB_HISTORY_KEY = "refset 0";

    private Map<TOGGLES, I_PluginToConceptPanel> pluginMap = new HashMap<TOGGLES, I_PluginToConceptPanel>();

    private JButton componentHistoryButton;

    JTree specTree;

    public I_GetConceptData refsetSpecConcept;

    private JTableWithDragImage clauseTable;

    private JPanel topPanel;

    private JComponent contentPanel;

    private TermTreeHelper treeHelper;

    JScrollPane specTreeScroller;

    JScrollPane commentScroller;

    private TermTreeHelper refsetTree;

    private JTableWithDragImage commentTable;

    private ReflexiveRefsetCommentTableModel commentTableModel;

    private RefsetSpecPanel refsetSpecPanel;

    private JPanel leftTogglePane;

    public RefsetSpecEditor(ACE ace, TermTreeHelper treeHelper, TermTreeHelper refsetTree, RefsetSpecPanel refsetSpecPanel)
            throws Exception {
        super();
        this.ace = ace;
        this.treeHelper = treeHelper;
        this.refsetTree = refsetTree;
        this.refsetSpecPanel = refsetSpecPanel;
        topPanel = new JPanel(new GridBagLayout());

        this.tabHistoryList = (LinkedList<I_GetConceptData>) ace.getAceFrameConfig().getTabHistoryMap().get(TAB_HISTORY_KEY);

        if (this.tabHistoryList == null) {
            this.tabHistoryList = new LinkedList<I_GetConceptData>();
            ace.getAceFrameConfig().getTabHistoryMap().put(TAB_HISTORY_KEY, this.tabHistoryList);
        }

        plugins =
                new ArrayList<org.dwfa.ace.api.I_PluginToConceptPanel>(Arrays
                    .asList(new org.dwfa.ace.api.I_PluginToConceptPanel[] {}));
        ace.getAceFrameConfig().addPropertyChangeListener("uncommitted", new UncommittedChangeListener());
        label = new TermComponentLabel(this.ace.getAceFrameConfig());
        label.addMouseListener(new RefsetCommentPopupListener(ace.getAceFrameConfig(), this));
        label.addTermChangeListener(treeHelper);
        label.addTermChangeListener(refsetTree);
        fixedToggleChangeActionListener = new FixedToggleChangeActionListener();
        this.ace.getAceFrameConfig().addPropertyChangeListener("visibleRefsets", fixedToggleChangeActionListener);
        this.ace.getAceFrameConfig().addPropertyChangeListener(this);
        GridBagConstraints c = new GridBagConstraints();

        JLabel linkSpinner = new JLabel(new ImageIcon(ACE.class.getResource("/24x24/plain/paperclip.png")));
        linkSpinner.setBorder(BorderFactory.createEmptyBorder(3, 3, 2, 5));

        c.anchor = GridBagConstraints.WEST;
        c.gridx = 0;
        c.gridy = 0;
        c.fill = GridBagConstraints.BOTH;
        topPanel.add(linkSpinner, c);
        c.gridx++;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        topPanel.add(label, c);
        c.weightx = 0.0;
        c.gridx++;
        componentHistoryButton = new JButton(ConceptPanel.HISTORY_ICON);
        componentHistoryButton.addActionListener(new ShowHistoryListener());
        componentHistoryButton.setToolTipText("click to show history of the RefSet Specification displayed in this viewer");
        topPanel.add(componentHistoryButton, c);

        c.gridx = 0;
        c.gridy++;
        c.gridwidth = 3;
        c.fill = GridBagConstraints.HORIZONTAL;
        JComponent toggleBar = getToggleBar();
        topPanel.add(toggleBar, c);
        c.fill = GridBagConstraints.BOTH;
        c.weighty = 1.0;
        c.gridy++;
        topPanel.setBorder(BorderFactory.createRaisedBevelBorder());

        this.contentPanel = getContentPane();
        label.addPropertyChangeListener("termComponent", labelListener);
        if (this.tabHistoryList.size() > 0 && this.tabHistoryList.getFirst() != null) {
            this.tempComponent = this.tabHistoryList.getFirst();
            this.setTermComponent(this.tabHistoryList.getFirst());
        }
    }

    public JComponent getToggleBar() throws IOException, ClassNotFoundException {
        JPanel toggleBar = new JPanel(new GridBagLayout());
        GridBagConstraints outer = new GridBagConstraints();
        outer.anchor = GridBagConstraints.WEST;
        outer.gridx = 0;
        outer.gridy = 0;
        outer.fill = GridBagConstraints.NONE;
        outer.weightx = 0;
        outer.weighty = 0;

        leftTogglePane = new JPanel(new GridBagLayout());
        toggleBar.add(leftTogglePane, outer);

        JPanel rightTogglePane = new JPanel(new FlowLayout());

        fixedToggleChangeActionListener = new FixedToggleChangeActionListener();

        historyButton = new JToggleButton(new ImageIcon(ACE.class.getResource("/24x24/plain/history.png")));
        historyButton.setSelected(false);
        historyButton.addActionListener(fixedToggleChangeActionListener);
        historyButton.setToolTipText("show/hide the history records");

        GridBagConstraints inner = new GridBagConstraints();
        inner.anchor = GridBagConstraints.WEST;
        inner.gridx = 0;
        inner.gridy = 0;
        inner.fill = GridBagConstraints.NONE;
        inner.weightx = 0;
        inner.weighty = 0;
        inner.gridheight = 2; // make button use 2 rows
        inner.insets = new Insets(0, 0, 0, 10);
        leftTogglePane.add(historyButton, inner);

        inner.gridheight = 1;
        inner.insets = new Insets(0, 0, 0, 0);
        inner.gridx++;
        inner.anchor = GridBagConstraints.EAST;
        JLabel refsetStatusLabel = new JLabel("refset status: ");
        leftTogglePane.add(refsetStatusLabel, inner);
        inner.gridy++;
        JLabel computeTypeLabel = new JLabel("compute type: ");
        leftTogglePane.add(computeTypeLabel, inner);
        inner.gridy++;
        JLabel computeStatusLabel = new JLabel("compute status: ");
        leftTogglePane.add(computeStatusLabel, inner);
        inner.gridy++;
        JLabel memberCountLabel = new JLabel("member count at last compute: ");
        leftTogglePane.add(memberCountLabel, inner);
        inner.gridy++;
        JLabel lastComputeTimeLabel = new JLabel("time of last compute: ");
        leftTogglePane.add(lastComputeTimeLabel, inner);

        inner.anchor = GridBagConstraints.WEST;
        inner.gridx++;
        inner.gridy--;
        inner.gridy--;
        inner.gridy--;
        inner.gridy--;
        refsetStatusValueLabel = new JLabel("");
        leftTogglePane.add(refsetStatusValueLabel, inner);
        inner.gridy++;
        computeTypeValueLabel = new JLabel("");
        leftTogglePane.add(computeTypeValueLabel, inner);
        inner.gridy++;
        computeStatusValueLabel = new JLabel("");
        leftTogglePane.add(computeStatusValueLabel, inner);
        inner.gridy++;
        memberCountValueLabel = new JLabel("");
        leftTogglePane.add(memberCountValueLabel, inner);
        inner.gridy++;
        lastComputeTimeValueLabel = new JLabel("");
        leftTogglePane.add(lastComputeTimeValueLabel, inner);

        outer.gridx++;
        outer.weightx = 1.0;
        outer.fill = GridBagConstraints.HORIZONTAL;
        toggleBar.add(new JPanel(), outer);

        File componentPluginDir = new File(ace.getPluginRoot() + File.separator + "refsetspec");
        File[] plugins = componentPluginDir.listFiles(new FilenameFilter() {
            public boolean accept(File arg0, String fileName) {
                return fileName.toLowerCase().endsWith(".bp");
            }

        });
        if (plugins != null) {
            outer.weightx = 0.0;
            outer.weighty = 0.0;
            outer.fill = GridBagConstraints.NONE;
            outer.gridx++;
            toggleBar.add(rightTogglePane, outer);
            boolean exceptions = false;
            StringBuffer exceptionMessage = new StringBuffer();
            exceptionMessage.append("<html>Exception(s) reading the following plugin(s): <p><p>");
            for (File f : plugins) {
                try {
                    FileInputStream fis = new FileInputStream(f);
                    BufferedInputStream bis = new BufferedInputStream(fis);
                    ObjectInputStream ois = new ObjectInputStream(bis);
                    BusinessProcess bp = (BusinessProcess) ois.readObject();
                    ois.close();
                    byte[] iconBytes = (byte[]) bp.readAttachement("button_icon");
                    if (iconBytes != null) {
                        ImageIcon icon = new ImageIcon(iconBytes);
                        JButton pluginButton = new JButton(icon);
                        pluginButton.setToolTipText(bp.getSubject());
                        pluginButton.addActionListener(new PluginListener(f));
                        rightTogglePane.add(pluginButton, outer);
                    } else {
                        JButton pluginButton = new JButton(bp.getName());
                        pluginButton.setToolTipText(bp.getSubject());
                        pluginButton.addActionListener(new PluginListener(f));
                        rightTogglePane.add(pluginButton, outer);
                    }
                } catch (Throwable e) {
                    exceptions = true;
                    exceptionMessage.append("Exception reading plugin: " + f.getAbsolutePath() + "<p>");
                    AceLog.getAppLog().log(Level.SEVERE, "Exception reading: " + f.getAbsolutePath(), e);
                }
            }

            if (exceptions) {
                exceptionMessage.append("<p>Please see the log file for more details.");
                JOptionPane.showMessageDialog(this.contentPanel, exceptionMessage.toString());
            }
        }

        updateToggles();

        return toggleBar;
    }

    private void updateToggles() {
        for (TOGGLES t : TOGGLES.values()) {
            boolean visible = ((AceFrameConfig) ace.getAceFrameConfig()).isToggleVisible(t);
            if (pluginMap.get(t) != null) {
                org.dwfa.ace.api.I_PluginToConceptPanel plugin = pluginMap.get(t);
                for (JComponent toggleComponent : plugin.getToggleBarComponents()) {
                    toggleComponent.setVisible(visible);
                    toggleComponent.setEnabled(visible);
                }
            } else {
                switch (t) {
                case HISTORY:
                    historyButton.setVisible(visible);
                    historyButton.setEnabled(visible);
                    break;

                default:
                    break;
                }
            }
        }
    }

    private class PluginListener implements ActionListener {
        File pluginProcessFile;

        private PluginListener(File pluginProcessFile) {
            super();
            this.pluginProcessFile = pluginProcessFile;
        }

        public void actionPerformed(ActionEvent e) {
            try {
                final JButton button = (JButton) e.getSource();
                button.setEnabled(false);
                FileInputStream fis = new FileInputStream(pluginProcessFile);
                BufferedInputStream bis = new BufferedInputStream(fis);
                ObjectInputStream ois = new ObjectInputStream(bis);
                final BusinessProcess bp = (BusinessProcess) ois.readObject();
                ois.close();
                getConfig().setStatusMessage("Executing: " + bp.getName());

                final I_Work worker;
                if (getConfig().getWorker().isExecuting()) {
                    worker = getConfig().getWorker().getTransactionIndependentClone();
                } else {
                    worker = getConfig().getWorker();
                }
                // Set concept bean
                // Set config

                worker.writeAttachment(WorkerAttachmentKeys.ACE_FRAME_CONFIG.name(), getConfig());
                bp.writeAttachment(ProcessAttachmentKeys.I_GET_CONCEPT_DATA.name(), label.getTermComponent());
                worker.writeAttachment(WorkerAttachmentKeys.I_HOST_CONCEPT_PLUGINS.name(), RefsetSpecEditor.this);
                Runnable r = new Runnable() {
                    private String exceptionMessage;

                    public void run() {
                        button.setEnabled(false);
                        I_EncodeBusinessProcess process = bp;
                        try {
                            worker.getLogger().info(
                                "Worker: " + worker.getWorkerDesc() + " (" + worker.getId() + ") executing process: "
                                    + process.getName());

                            worker.execute(process);

                            SortedSet<ExecutionRecord> sortedRecords =
                                    new TreeSet<ExecutionRecord>(process.getExecutionRecords());
                            Iterator<ExecutionRecord> recordItr = sortedRecords.iterator();
                            StringBuffer buff = new StringBuffer();
                            while (recordItr.hasNext()) {
                                ExecutionRecord rec = recordItr.next();
                                buff.append("\n");
                                buff.append(rec.toString());
                            }
                            worker.getLogger().info(buff.toString());
                            exceptionMessage = "";
                            button.setEnabled(true);
                        } catch (Throwable e1) {
                            worker.getLogger().log(Level.WARNING, e1.toString(), e1);
                            exceptionMessage = e1.toString();
                            button.setEnabled(true);
                        }
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {

                                getConfig().setStatusMessage("<html><font color='#006400'>execute");
                                if (exceptionMessage.equals("")) {
                                    getConfig().setStatusMessage(
                                        "<html>Execution of <font color='blue'>" + bp.getName() + "</font> complete.");
                                } else {
                                    getConfig().setStatusMessage(
                                        "<html><font color='blue'>Process complete: <font color='red'>" + exceptionMessage);
                                }
                                ace.getAceFrameConfig().refreshRefsetTab();
                            }
                        });

                    }

                };
                new Thread(r).start();
            } catch (Exception e1) {
                getConfig().setStatusMessage("Exception during execution.");
                AceLog.getAppLog().alertAndLogException(e1);
            }
        }
    }

    private JComponent getContentPane() throws Exception {
        JTabbedPane refsetTabs = new JTabbedPane();
        refsetTabs.addTab("specification", getSpecPane());
        commentTable = getRefsetSpecPanel().createCommentTable(ace.getAceFrameConfig(), this);
        commentTableModel = (ReflexiveRefsetCommentTableModel) commentTable.getModel();
        commentScroller = new JScrollPane(commentTable);
        refsetTabs.addTab("comments", commentScroller);
        return refsetTabs;
    }

    private JComponent getSpecPane() throws Exception {
        JPanel content = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1.0;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.BOTH;

        List<ReflexiveRefsetFieldData> columns = new ArrayList<ReflexiveRefsetFieldData>();
        getDefaultSpecColumns(EConcept.REFSET_TYPES.CID_CID, columns);
        ReflexiveRefsetMemberTableModel reflexiveModel =
                new ReflexiveRefsetMemberTableModel(RefsetSpecEditor.this, columns
                    .toArray(new ReflexiveRefsetFieldData[columns.size()]));
        reflexiveModel.setComponentId(Integer.MIN_VALUE);
        reflexiveModel.getRowCount();

        JPanel clauseTablePanel =
                ReflexiveRefsetUtil.getExtensionPanel(null, reflexiveModel, RefsetSpecEditor.this, false, false);
        clauseTable = (JTableWithDragImage) clauseTablePanel.getClientProperty("extTable");
        int columnIndex = 0;
        for (ReflexiveRefsetFieldData columnId : reflexiveModel.getColumns()) {
            clauseTable.getColumnModel().getColumn(columnIndex).setIdentifier(columnId);
            columnIndex++;
        }
        content.add(clauseTablePanel, c);
        c.gridy++;
        c.weighty = 1.0;

        specTree = new JTree(new DefaultTreeModel(new RefsetSpecTreeNode(null)));
        specTree.addMouseListener(new RefsetSpecTreeMouseListener(ace.getAceFrameConfig(), this));
        specTree.setCellRenderer(new RefsetSpecTreeCellRenderer(ace.getAceFrameConfig()));
        specTree.setRootVisible(false);
        specTree.setShowsRootHandles(true);
        specTreeScroller = new JScrollPane(specTree);
        content.add(specTreeScroller, c);

        specTree.addTreeSelectionListener(new RefsetSpecSelectionListener());

        c.gridy++;

        for (org.dwfa.ace.api.I_PluginToConceptPanel plugin : plugins) {
            if (plugin.showComponent()) {
                content.add(plugin.getComponent(this), c);
                c.gridy++;
            }
        }
        c.weightx = 0.0;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.BOTH;
        content.add(new JPanel(), c);
        return content;
    }

    public I_AmTermComponent getTermComponent() {
        I_AmTermComponent returnValue = tempComponent;
        if (returnValue != null) {
            return returnValue;
        }
        return label.getTermComponent();
    }

    I_AmTermComponent tempComponent;

    public void setTermComponent(final I_AmTermComponent termComponent) {
        tempComponent = termComponent;
        if (SwingUtilities.isEventDispatchThread()) {
            label.setTermComponent(termComponent);
        } else {
            try {
                SwingUtilities.invokeAndWait(new Runnable() {
                    public void run() {
                        label.setTermComponent(termComponent);
                    }
                });
            } catch (InterruptedException e) {
                AceLog.getAppLog().alertAndLogException(e);
            } catch (InvocationTargetException e) {
                AceLog.getAppLog().alertAndLogException(e);
            }
        }
    }

    public I_GetConceptData getHierarchySelection() {
        throw new UnsupportedOperationException();
    }

    public boolean getShowHistory() {
        return historyButton.isSelected();
    }

    public boolean getShowRefsets() {
        throw new UnsupportedOperationException();
    }

    public boolean getToggleState(TOGGLES toggle) {
        org.dwfa.ace.api.I_PluginToConceptPanel plugin = pluginMap.get(toggle);
        if (plugin != null) {
            for (JComponent component : plugin.getToggleBarComponents()) {
                if (JToggleButton.class.isAssignableFrom(component.getClass())) {
                    JToggleButton toggleButton = (JToggleButton) component;
                    return toggleButton.isSelected();
                }
            }
        } else {
            switch (toggle) {
            case HISTORY:
                return historyButton.isSelected();
            }
        }
        throw new UnsupportedOperationException(" Can't handle toggle: " + toggle);
    }

    public boolean getUsePrefs() {
        return false;
    }

    public void setAllTogglesToState(boolean state) {
        throw new UnsupportedOperationException();
    }

    public void setLinkType(LINK_TYPE link) {
        throw new UnsupportedOperationException();
    }

    public void setToggleState(TOGGLES toggle, boolean state) {
        throw new UnsupportedOperationException();
    }

    public void unlink() {
        throw new UnsupportedOperationException();
    }

    public I_ConfigAceFrame getConfig() {
        return ace.getAceFrameConfig();
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals("viewPositions")) {
            fixedToggleChangeActionListener.actionPerformed(null);
        } else if (evt.getPropertyName().equals("commit")) {
            if (label.getTermComponent() != null) {
                I_GetConceptData cb = (I_GetConceptData) label.getTermComponent();
                try {
                    if (cb.getConceptAttributes() == null) {
                        label.setTermComponent(null);
                    }
                } catch (IOException e) {
                    label.setTermComponent(null);
                    AceLog.getAppLog().alertAndLogException(e);
                }
            }
            this.updateSpecTree(false);
            this.firePropertyChange("commit", null, null);
        } else if (evt.getPropertyName().equals("uncommitted")) {
            this.updateSpecTree(false);
        } else if (evt.getPropertyName().equals("refsetSpecChanged")) {
            this.updateSpecTree(false);
        }
    }

    public Dimension getPreferredScrollableViewportSize() {
        return new Dimension(30, 30);
    }

    public int getScrollableBlockIncrement(Rectangle arg0, int arg1, int arg2) {
        return 75;
    }

    public boolean getScrollableTracksViewportHeight() {
        return false;
    }

    public boolean getScrollableTracksViewportWidth() {
        return true;
    }

    public int getScrollableUnitIncrement(Rectangle arg0, int arg1, int arg2) {
        return 10;
    }

    UpdateTreeSpec updater;

    public synchronized void updateSpecTree(boolean clearSelection) {
        refsetSpecConcept = null;
        if (clearSelection) {
            specTree.clearSelection();
        }
        if (updater != null) {
            updater.cancel = true;
        }
        updater = new UpdateTreeSpec(this);
        updater.execute();
    }

    public JTree getTreeInSpecEditor() {
        return specTree;
    }

    public I_GetConceptData getRefsetSpecInSpecEditor() throws IOException, TerminologyException {
        I_GetConceptData refsetConcept = (I_GetConceptData) getLabel().getTermComponent();
        if (refsetConcept != null) {
            Set<? extends I_GetConceptData> specs =
                    Terms.get().getRefsetHelper(ace.getAceFrameConfig()).getSpecificationRefsetForRefset(refsetConcept,
                        ace.getAceFrameConfig());
            if (specs.size() > 0) {
                refsetSpecConcept = specs.iterator().next();
            }
        }
        return refsetSpecConcept;
    }

    public TermComponentLabel getLabel() {
        return label;
    }

    public static void getDefaultSpecColumns(EConcept.REFSET_TYPES extType, List<ReflexiveRefsetFieldData> columns)
            throws NoSuchMethodException {
        ReflexiveRefsetFieldData column1 = new ReflexiveRefsetFieldData();
        column1.setColumnName("truth");
        column1.setCreationEditable(true);
        column1.setUpdateEditable(false);
        column1.setFieldClass(Number.class);
        column1.setMin(5);
        column1.setPref(50);
        column1.setMax(50);
        column1.setInvokeOnObjectType(INVOKE_ON_OBJECT_TYPE.PART);
        column1.setReadMethod(extType.getPartClass().getMethod("getC1id"));
        column1.setWriteMethod(extType.getPartClass().getMethod("setC1id", int.class));
        column1.setType(REFSET_FIELD_TYPE.CONCEPT_IDENTIFIER);
        columns.add(column1);

        ReflexiveRefsetFieldData column2 = new ReflexiveRefsetFieldData();
        column2.setColumnName("clause");
        column2.setCreationEditable(true);
        column2.setUpdateEditable(false);
        column2.setFieldClass(Number.class);
        column2.setMin(5);
        column2.setPref(75);
        column2.setMax(1000);
        column2.setInvokeOnObjectType(INVOKE_ON_OBJECT_TYPE.PART);
        column2.setReadMethod(extType.getPartClass().getMethod("getC2id"));
        column2.setWriteMethod(extType.getPartClass().getMethod("setC2id", int.class));
        column2.setType(REFSET_FIELD_TYPE.CONCEPT_IDENTIFIER);
        columns.add(column2);
    }

    public JPanel getTopPanel() {
        return topPanel;
    }

    public JComponent getContentPanel() {
        return contentPanel;
    }

    public void addHistoryActionListener(ActionListener al) {
        this.historyButton.addActionListener(al);
    }

    public void removeHistoryActionListener(ActionListener al) {
        this.historyButton.removeActionListener(al);
    }

    public ReflexiveRefsetCommentTableModel getCommentTableModel() {
        return commentTableModel;
    }

    public RefsetSpecPanel getRefsetSpecPanel() {
        return refsetSpecPanel;
    }

    public JToggleButton getHistoryButton() {
        return historyButton;
    }

    public void refresh() {
        if (commentTableModel != null) {
            commentTableModel.fireTableDataChanged();
        }
    }
}
