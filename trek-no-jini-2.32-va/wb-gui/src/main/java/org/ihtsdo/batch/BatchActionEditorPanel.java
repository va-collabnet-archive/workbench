/**
 * Copyright (c) 2009 International Health Terminology Standards Development Organisation
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.ihtsdo.batch;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.ListModel;
import javax.swing.SwingWorker;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import org.dwfa.ace.ACE;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_ShowActivity;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.config.AceFrameConfig;
import org.dwfa.ace.list.TerminologyList;
import org.dwfa.ace.list.TerminologyListModel;
import org.dwfa.ace.log.AceLog;
import org.dwfa.tapi.ComputationCanceled;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ComponentVersionBI;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.RelAssertionType;
import org.ihtsdo.tk.api.TerminologyStoreDI;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.coordinate.EditCoordinate;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.description.DescriptionVersionBI;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.api.refex.type_boolean.RefexBooleanVersionBI;
import org.ihtsdo.tk.api.refex.type_int.RefexIntVersionBI;
import org.ihtsdo.tk.api.refex.type_long.RefexLongVersionBI;
import org.ihtsdo.tk.api.refex.type_nid.RefexNidVersionBI;
import org.ihtsdo.tk.api.refex.type_string.RefexStringVersionBI;
import org.ihtsdo.tk.api.relationship.RelationshipVersionBI;
import org.ihtsdo.tk.binding.snomed.TermAux;
import org.ihtsdo.util.swing.GuiUtil;

/**
 *
 * @author marc
 */
public final class BatchActionEditorPanel extends javax.swing.JPanel {

    private class BatchActionSwingWorker extends SwingWorker<Object, Object>
            implements ActionListener {

        private BatchActionProcessor bap;
        private I_ShowActivity gui;

        public void setBap(BatchActionProcessor bap) {
            this.bap = bap;
        }

        @Override
        protected Object doInBackground() throws Exception {
            AceFrameConfig config = ace.aceFrameConfig;
            gui = Terms.get().newActivityPanel(true, config, "Apply Batch Edits", true);
            gui.addRefreshActionListener(this);
            gui.addStopActionListener(this);
            gui.setProgressInfoUpper("Batch Edits");
            gui.setIndeterminate(true);

            // EXERCISE BATCH ACTION TEST
            Ts.get().iterateConceptDataInParallel(bap);
            return null;
        }

        @Override
        protected void done() {
            StringBuilder sb = new StringBuilder("\r\n!!! BATCH ACTION TASK REPORT\r\n");
            if (isCancelled()) {
                sb.append("!!! BATCH ACTION TASK CANCEL OCCURED\r\n");
                try {
                    Ts.get().cancel();
                } catch (IOException ex) {
                    Logger.getLogger(BatchActionEditorPanel.class.getName()).log(Level.SEVERE, null, ex);
                }
                Logger.getLogger(
                        BatchActionEditorPanel.class.getName()).log(Level.INFO, sb.toString());
                gui.setProgressInfoLower("canceled by user");
                BatchActionEventReporter.reset();
                BatchActionEventReporter.add(new BatchActionEvent(null,
                        BatchActionTask.BatchActionTaskType.SIMPLE,
                        BatchActionEvent.BatchActionEventType.EVENT_NOOP,
                        "ALL BATCH EDITS CANCELED BY USER"));
                resultsTextArea.setText(BatchActionEventReporter.createReportHTML());
                AceLog.getAppLog().alertAndLog(Level.INFO, "Batch edits canceled by user.", null);

            } else {
                sb.append(BatchActionEventReporter.createReportTSV());
                Logger.getLogger(
                        BatchActionEditorPanel.class.getName()).log(Level.INFO, sb.toString());
                resultsTextArea.setText(BatchActionEventReporter.createReportHTML());
                gui.setProgressInfoLower("batch edits completed");
                AceLog.getAppLog().alertAndLog(Level.INFO, "Batch edits completed.", null);
            }
            try {
                gui.complete();
            } catch (ComputationCanceled ex) {
                Logger.getLogger(
                        BatchActionEditorPanel.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        // ActionListener
        @Override
        public void actionPerformed(ActionEvent e) {
            bap.setContinueWorkB(false);
        }
    }
    public static boolean batchEditingDisabled = false;
    public final ACE ace;
    private final I_ConfigAceFrame config;
    private final TerminologyStoreDI ts;
    private final TerminologyList batchConceptList;
    private List<BatchActionTaskBase> batchActionEditTaskList;
    private final JPanel batchActionTaskViewPanel;
    private final JTextPane resultsTextArea;
    private List<RelationshipVersionBI> existingParents;
    private List<ComponentVersionBI> existingRefsets;
    private List<ComponentVersionBI> existingDescriptionRefsets;
    private List<RelationshipVersionBI> existingRoles;
    private List<ComponentVersionBI> parentLinkages;
    private Map<Integer, Class> existingRefsetTypes;
    private BatchActionProcessor bap;
    private BatchActionSwingWorker basw;

    public List<RelationshipVersionBI> getExistingParents() {
        return existingParents;
    }

    public List<ComponentVersionBI> getExistingRefsets() {
        return existingRefsets;
    }

    public List<ComponentVersionBI> getExistingDescriptionRefsets() {
        return existingDescriptionRefsets;
    }

    public Class getExistingRefsetType(int nid) {
        return existingRefsetTypes.get(nid);
    }

    public List<RelationshipVersionBI> getExistingRoles() {
        return existingRoles;
    }

    public List<ComponentVersionBI> getParentLinkages() {
        return parentLinkages;
    }

    public void updateExistingLists(ListModel termList) {
        if (batchEditingDisabled) {
            return;
        }
        existingParents = new ArrayList<>();
        existingRefsets = new ArrayList<>();
        existingDescriptionRefsets = new ArrayList<>();
        existingRoles = new ArrayList<>();
        existingRefsetTypes = new HashMap<>();
        parentLinkages = new ArrayList<>();

        LinkedHashSet<Integer> setRefsets = new LinkedHashSet<>();
        LinkedHashSet<Integer> setDescriptionRefsets = new LinkedHashSet<>();
        ViewCoordinate vc = ace.getAceFrameConfig().getViewCoordinate();

        try {
            // parentLinkages from preference settings
            I_IntSet parentLinkageTypes = ace.aceFrameConfig.getDestRelTypes();
            int[] types = parentLinkageTypes.getSetValues();
            for (int typeNid : types) {
                if(typeNid != TermAux.IS_A.getLenient().getConceptNid()){
                    ComponentVersionBI cvbi = ts.getComponentVersion(vc, typeNid);
                    parentLinkages.add(cvbi);
                }
            }
            
            
            for (int i = 0; i < termList.getSize(); i++) {
                I_GetConceptData cb = (I_GetConceptData) termList.getElementAt(i);
                if (!cb.isCanceled()) {
                    // EXISTING PARENTS
                    for (ConceptVersionBI cvbi : cb.getVersions(vc)) {
                        for (RelationshipVersionBI rvbi : cvbi.getRelationshipsOutgoingActive()) {
                            if (rvbi.isStated() && parentLinkageTypes.contains(rvbi.getTypeNid())) {
                                // Check if role-value already exists
                                Boolean found = false;
                                for (RelationshipVersionBI parent : existingParents) {
                                    if (parent.getTypeNid() == rvbi.getTypeNid()
                                            && parent.getTargetNid() == rvbi.getTargetNid()) {
                                        found = true;
                                    }
                                }
                                if (found == false) {
                                    existingParents.add(rvbi);
                                }
                            }
                        }
                    }

                    // EXISTING ROLES
                    for (ConceptVersionBI cvbi : cb.getVersions(vc)) {
                        for (RelationshipVersionBI rvbi : cvbi.getRelationshipsOutgoingActive()) {
                            if (rvbi.isStated()) {
                                // Check if role-value already exists
                                Boolean found = false;
                                for (RelationshipVersionBI role : existingRoles) {
                                    if (role.getTypeNid() == rvbi.getTypeNid()
                                            && role.getTargetNid() == rvbi.getTargetNid()) {
                                        found = true;
                                    }
                                }
                                if (found == false) {
                                    existingRoles.add(rvbi);
                                }
                            }
                        }
                    }

                    // EXISTING CONCEPT LEVEL REFSETS
                    Collection<? extends RefexVersionBI<?>> cr = cb.getRefexesActive(vc);
                    for (RefexVersionBI<?> rvbi : cr) {
                        int refexNid = rvbi.getRefexNid();
                        existingRefsetTypes.put(refexNid, RefexStringVersionBI.class);
                        if (RefexStringVersionBI.class.isAssignableFrom(rvbi.getClass())) {
                            RefexStringVersionBI r = (RefexStringVersionBI) rvbi;
                        } else if (RefexBooleanVersionBI.class.isAssignableFrom(rvbi.getClass())) {
                            RefexBooleanVersionBI r = (RefexBooleanVersionBI) rvbi;
                        } else if (RefexNidVersionBI.class.isAssignableFrom(rvbi.getClass())) {
                            RefexNidVersionBI r = (RefexNidVersionBI) rvbi;
                        } else if (RefexIntVersionBI.class.isAssignableFrom(rvbi.getClass())) {
                            RefexIntVersionBI r = (RefexIntVersionBI) rvbi;
                        } else if (RefexLongVersionBI.class.isAssignableFrom(rvbi.getClass())) {
                            RefexLongVersionBI r = (RefexLongVersionBI) rvbi;
                        }
                        setRefsets.add(refexNid);
                    }
                    // EXISTING DESCRIPTION LEVEL REFSETS
                    ConceptVersionBI cbvi = cb.getVersion(vc);
                    Collection<? extends DescriptionVersionBI> descriptions = cb.getVersion(vc).getDescriptionsActive();
                    for (DescriptionVersionBI dvbi : descriptions) {
                        Collection<? extends RefexVersionBI<?>> rvbiList = dvbi.getRefexesActive(vc);
                        for (RefexVersionBI<?> rvbi : rvbiList) {
                            setDescriptionRefsets.add(rvbi.getRefexNid());
                        }
                    }
                }
            }

            for (Integer nid : setRefsets) {
                ComponentVersionBI cvbi = ts.getComponentVersion(vc, nid);
                if (cvbi != null) {
                    existingRefsets.add(cvbi);
                }
            }

            for (Integer nid : setDescriptionRefsets) {
                ComponentVersionBI cvbi = ts.getComponentVersion(vc, nid);
                if (cvbi != null) {
                    existingDescriptionRefsets.add(cvbi);
                }
            }

            // UPDATE TASK DETAILS
            for (BatchActionTaskBase baet : batchActionEditTaskList) {
                baet.updateExisting(
                        existingParents, existingRefsets, 
                        existingDescriptionRefsets, 
                        existingRoles, parentLinkages);
            }

        } catch (ContradictionException | IOException ex) {
            Logger.getLogger(BatchActionEditorPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    class ExistingListDataListener implements ListDataListener {

        BatchActionEditorPanel baep;

        public ExistingListDataListener(BatchActionEditorPanel baep) {
            this.baep = baep;
        }

        @Override
        public void intervalAdded(ListDataEvent lde) {
            if (batchActionEditTaskList != null && batchActionEditTaskList.size() > 0) {
                baep.updateExistingLists((TerminologyListModel) lde.getSource());
            }
        }

        @Override
        public void intervalRemoved(ListDataEvent lde) {
            if (batchActionEditTaskList != null && batchActionEditTaskList.size() > 0) {
                baep.updateExistingLists((TerminologyListModel) lde.getSource());
            }
        }

        @Override
        public void contentsChanged(ListDataEvent lde) {
            if (batchActionEditTaskList != null && batchActionEditTaskList.size() > 0) {
                baep.updateExistingLists((TerminologyListModel) lde.getSource());
            }
        }
    }

    /**
     * Creates new form BatchActionEditorPanel
     * @param ace
     * @param list
     * @param results
     */
    public BatchActionEditorPanel(ACE ace, TerminologyList list, JTextPane results) {
        this.ace = ace;
        this.ts = Ts.get();
        this.config = ace.getAceFrameConfig();
        this.batchConceptList = list;
        this.batchConceptList.getModel().addListDataListener(new ExistingListDataListener(this));
        this.batchActionEditTaskList = new ArrayList<>();
        this.batchActionTaskViewPanel = new JPanel();
        this.resultsTextArea = results;

        // SETUP 'commit" LISTENER
        config.addPropertyChangeListener("commit",
                new PropertyChangeListener() {
                    @Override
                    public void propertyChange(PropertyChangeEvent evt) {
                        if (batchActionEditTaskList != null && batchActionEditTaskList.size() > 0) {
                            updateExistingLists(batchConceptList.getModel());
                        }
                    }
                });

        // initComponents is autogenerated
        initComponents();

        existingParents = new ArrayList<>();
        existingRefsets = new ArrayList<>();
        existingDescriptionRefsets = new ArrayList<>();
        existingRoles = new ArrayList<>();
        updateExistingLists(batchConceptList.getModel());
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT
     * modify this code. The content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        btnExecuteAllTasks = new javax.swing.JButton();
        btnAdd = new javax.swing.JButton();
        btnClearAll = new javax.swing.JButton();
        jScrollBatchActionTasks = new javax.swing.JScrollPane();

        setLayout(new java.awt.GridBagLayout());

        btnExecuteAllTasks.setText("Execute");
        btnExecuteAllTasks.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                actionExecuteAllTasks(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.weightx = 1.0;
        add(btnExecuteAllTasks, gridBagConstraints);

        btnAdd.setIcon(new javax.swing.ImageIcon(getClass().getResource("/add2.png"))); // NOI18N
        btnAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                actionAddAnotherTask(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 2, 0, 0);
        add(btnAdd, gridBagConstraints);

        btnClearAll.setText("Clear All");
        btnClearAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                actionClearAll(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.weightx = 1.0;
        add(btnClearAll, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jScrollBatchActionTasks, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    // actionExecuteAllTask ... dispatch engine approach
    private void actionExecuteAllTasks(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_actionExecuteAllTasks
        try {
            if (ts.hasUncommittedChanges()) {
                String errStr = "Please commit or cancel pending edits prior to executing batch edits.";
                AceLog.getAppLog().alertAndLog(Level.INFO, errStr, new Exception(errStr));
                return;
            }

            // SETUP CONCEPT LIST
            List<ConceptChronicleBI> concepts = new ArrayList<>();
            ListModel termList = batchConceptList.getModel();
            for (int i = 0; i < termList.getSize(); i++) {
                I_GetConceptData cb = (I_GetConceptData) termList.getElementAt(i);
                concepts.add(ts.getConcept(cb.getConceptNid()));
            }

            // SETUP TASK LIST
            ViewCoordinate vc = ace.aceFrameConfig.getViewCoordinate();
            vc = new ViewCoordinate(vc);
            vc.setRelationshipAssertionType(RelAssertionType.STATED);
            EditCoordinate ec = ace.aceFrameConfig.getEditCoordinate();

            BatchActionTask.setup(ec, vc, config);
            BatchActionEventReporter.reset();

            List<BatchActionTask> tasks = new ArrayList<>();
            for (BatchActionTaskBase taskBase : batchActionEditTaskList) {
                BatchActionTask tmpTask = taskBase.getTask(ec, vc, concepts);
                if (tmpTask != null) {
                    tasks.add(tmpTask);
                }
            }

            if (BatchActionEventReporter.getSize() > 0) {
                StringBuilder sb = new StringBuilder("\r\n!!! BATCH ACTION TASK LAUNCH ERROR\r\n");
                sb.append(BatchActionEventReporter.createReportTSV());
                Logger.getLogger(
                        BatchActionEditorPanel.class.getName()).log(Level.INFO, sb.toString());

                resultsTextArea.setText(BatchActionEventReporter.createReportHTML());

                String errStr = "Incomplete Batch Action Task parameters.  See results listing.";
                AceLog.getAppLog().alertAndLog(Level.SEVERE, errStr, new Exception(errStr));
                return;
            }

            bap = new BatchActionProcessor(concepts, tasks, ec, vc);
            basw = new BatchActionSwingWorker();
            basw.setBap(bap);
            basw.execute();

// The following was moved to the SwingWorker ... can be deleted after testing.
//          ts.iterateConceptDataInParallel(bap);
//          StringBuilder sb = new StringBuilder("\r\n!!! BATCH ACTION TASK REPORT\r\n");
//          sb.append(BatchActionEventReporter.createReportTSV());
//          Logger.getLogger(
//              BatchActionEditorPanel.class.getName()).log(Level.INFO, sb.toString());
//          resultsTextArea.setText(BatchActionEventReporter.createReportHTML());

        } catch (Exception ex) {
            Logger.getLogger(BatchActionEditorPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_actionExecuteAllTasks
    
    public boolean stop(){
        if(bap != null){
            bap.setContinueWorkB(false);
            basw.cancel(true);
        }
        return false;
    }
    private void actionAddAnotherTask(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_actionAddAnotherTask
        actionAddAnotherTask();
    }//GEN-LAST:event_actionAddAnotherTask

    public void actionAddAnotherTask() {
        // CREATE NEW TASK
        BatchActionTaskBase batb = new BatchActionTaskBase(this);
        batb.setTaskParentUI(this);
        batb.updateExisting(
                existingParents, existingRefsets, 
                existingDescriptionRefsets,
                existingRoles, parentLinkages);
        batb.setEnabled(true);
        batb.setVisible(true);
        batb.invalidate();
        if (batchActionEditTaskList.isEmpty()) {
            updateExistingLists(batchConceptList.getModel());
        }
        batchActionEditTaskList.add(batb);

        // FILL SCROLL PANE
        batchActionTaskViewPanel.removeAll();
        batchActionTaskViewPanel.setLayout(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.anchor = GridBagConstraints.NORTH;
        gc.fill = GridBagConstraints.BOTH;
        gc.gridx = 0;
        gc.gridy = 0;
        gc.weightx = 0.5;
        gc.weighty = 0;
        gc.ipady = 0;
        for (BatchActionTaskBase b : batchActionEditTaskList) {
            batchActionTaskViewPanel.add(b, gc);
            gc.gridy++;
        }
        gc.weighty = 0.5;
        batchActionTaskViewPanel.add(new JLabel(""), gc);
        jScrollBatchActionTasks.setViewportView(batchActionTaskViewPanel);

        jScrollBatchActionTasks.invalidate();
        jScrollBatchActionTasks.validate();
        jScrollBatchActionTasks.doLayout();
        this.invalidate();
        this.validate();
        this.doLayout();
    }

    public void actionDeleteTask(BatchActionTaskBase task) {
        if (batchActionEditTaskList.remove(task)) {
            // REFILL SCROLL PANE
            batchActionTaskViewPanel.removeAll();
            batchActionTaskViewPanel.setLayout(new GridBagLayout());
            GridBagConstraints gc = new GridBagConstraints();
            gc.anchor = GridBagConstraints.NORTHWEST;
            gc.fill = GridBagConstraints.HORIZONTAL;
            gc.gridx = 0;
            gc.weightx = 0.5;
            gc.gridy = 0;
            for (BatchActionTaskBase b : batchActionEditTaskList) {
                batchActionTaskViewPanel.add(b, gc);
                gc.gridy++;
            }
            gc.weighty = 0.5;
            batchActionTaskViewPanel.add(new JLabel(""), gc);
            jScrollBatchActionTasks.setViewportView(batchActionTaskViewPanel);

            jScrollBatchActionTasks.invalidate();
            jScrollBatchActionTasks.validate();
            jScrollBatchActionTasks.doLayout();
            this.invalidate();
            this.validate();
            this.doLayout();
        }
    }

    private void actionClearAll(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_actionClearAll
        batchActionEditTaskList.clear();
        batchActionTaskViewPanel.removeAll();
        batchActionTaskViewPanel.invalidate();
        jScrollBatchActionTasks.invalidate();
        jScrollBatchActionTasks.doLayout();
        this.invalidate();
        this.validate();
        GuiUtil.tickle(this);
    }//GEN-LAST:event_actionClearAll
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAdd;
    private javax.swing.JButton btnClearAll;
    private javax.swing.JButton btnExecuteAllTasks;
    private javax.swing.JScrollPane jScrollBatchActionTasks;
    // End of variables declaration//GEN-END:variables
}
