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
package org.ihtsdo.batch;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import org.dwfa.ace.ACE;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.list.TerminologyList;
import org.dwfa.ace.list.TerminologyListModel;
import org.dwfa.ace.log.AceLog;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ComponentVersionBI;
import org.ihtsdo.tk.api.ContraditionException;
import org.ihtsdo.tk.api.RelAssertionType;
import org.ihtsdo.tk.api.TerminologyStoreDI;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.coordinate.EditCoordinate;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.api.refex.type_boolean.RefexBooleanVersionBI;
import org.ihtsdo.tk.api.refex.type_cnid.RefexCnidVersionBI;
import org.ihtsdo.tk.api.refex.type_int.RefexIntVersionBI;
import org.ihtsdo.tk.api.refex.type_long.RefexLongVersionBI;
import org.ihtsdo.tk.api.refex.type_str.RefexStrVersionBI;
import org.ihtsdo.tk.api.relationship.RelationshipVersionBI;
import org.ihtsdo.util.swing.GuiUtil;

/**
 *
 * @author marc
 */
public final class BatchActionEditorPanel extends javax.swing.JPanel {

    private final ACE ace;
    private final TerminologyStoreDI ts;
    private final TerminologyList batchConceptList;
    private List<BatchActionTaskBase> batchActionEditTaskList;
    private JPanel batchActionTaskViewPanel;
    private JTextPane resultsTextArea;
    private List<ComponentVersionBI> existingParents;
    private List<ComponentVersionBI> existingRefsets;
    private List<ComponentVersionBI> existingRoles;
    private List<ComponentVersionBI> parentLinkages;
    private Map<Integer, Class> existingRefsetTypes;

    public List<ComponentVersionBI> getExistingParents() {
        return existingParents;
    }

    public List<ComponentVersionBI> getExistingRefsets() {
        return existingRefsets;
    }

    public Class getExistingRefsetType(int nid) {
        return existingRefsetTypes.get(nid);
    }

    public List<ComponentVersionBI> getExistingRoles() {
        return existingRoles;
    }

    public List<ComponentVersionBI> getParentLinkages() {
        return parentLinkages;
    }

    public void updateExistingLists(ListModel termList) {
        existingParents = new ArrayList<ComponentVersionBI>();
        existingRefsets = new ArrayList<ComponentVersionBI>();
        existingRoles = new ArrayList<ComponentVersionBI>();
        existingRefsetTypes = new HashMap<Integer, Class>();
        parentLinkages = new ArrayList<ComponentVersionBI>();

        LinkedHashSet<Integer> setParents = new LinkedHashSet<Integer>();
        LinkedHashSet<Integer> setRefsets = new LinkedHashSet<Integer>();
        LinkedHashSet<Integer> setRoles = new LinkedHashSet<Integer>();
        ViewCoordinate vc = ace.getAceFrameConfig().getViewCoordinate();

        try {
            for (int i = 0; i < termList.getSize(); i++) {
                I_GetConceptData cb = (I_GetConceptData) termList.getElementAt(i);
                if (!cb.isCanceled()) {
                    // EXISTING PARENTS
                    for (ConceptVersionBI cvbi : cb.getVersions(vc)) {
                        for (RelationshipVersionBI rvbi : cvbi.getRelsOutgoingActive()) {
                            if (rvbi.isStated()) {
                                setParents.add(rvbi.getDestinationNid());
                            }
                        }
                    }

                    // EXISTING ROLES
                    for (ConceptVersionBI cvbi : cb.getVersions(vc)) {
                        for (RelationshipVersionBI rvbi : cvbi.getRelsOutgoingActive()) {
                            if (rvbi.isStated()) {
                                setRoles.add(rvbi.getNid());
                            }
                        }
                    }

                    //
                    Collection<? extends RefexVersionBI<?>> cr = cb.getCurrentRefexes(vc);
                    for (RefexVersionBI<?> rvbi : cr) {
                        int refexNid = rvbi.getCollectionNid();
                        existingRefsetTypes.put(refexNid, RefexStrVersionBI.class);
                        if (RefexStrVersionBI.class.isAssignableFrom(rvbi.getClass())) {
                            RefexStrVersionBI r = (RefexStrVersionBI) rvbi;
                        } else if (RefexBooleanVersionBI.class.isAssignableFrom(rvbi.getClass())) {
                            RefexBooleanVersionBI r = (RefexBooleanVersionBI) rvbi;

                            // rvbi.getRefexEditSpec() throws exception on internal index = -1
                            // RefexBooleanAnalogBI ra = (RefexBooleanAnalogBI) r.makeAnalog(r.getStatusNid(), r.getAuthorNid(), r.getPathNid(), Long.MAX_VALUE);
                            // ra.setBoolean1(true);
                        } else if (RefexCnidVersionBI.class.isAssignableFrom(rvbi.getClass())) {
                            RefexCnidVersionBI r = (RefexCnidVersionBI) rvbi;
                        } else if (RefexIntVersionBI.class.isAssignableFrom(rvbi.getClass())) {
                            RefexIntVersionBI r = (RefexIntVersionBI) rvbi;
                        } else if (RefexLongVersionBI.class.isAssignableFrom(rvbi.getClass())) {
                            RefexLongVersionBI r = (RefexLongVersionBI) rvbi;
                        }
                        setRefsets.add(refexNid);
                    }
                }
            }
            // CONVERT SET TO LIST
            for (Integer nid : setParents) {
                ComponentVersionBI cvbi = ts.getComponentVersion(vc, nid);
                if (cvbi != null) {
                    existingParents.add(cvbi);
                }
            }

            for (Integer nid : setRefsets) {
                ComponentVersionBI cvbi = ts.getComponentVersion(vc, nid);
                if (cvbi != null) {
                    existingRefsets.add(cvbi);
                }
            }

            for (Integer nid : setRoles) {
                ComponentVersionBI cvbi = ts.getComponentVersion(vc, nid);
                if (cvbi != null) {
                    existingRoles.add(cvbi);
                }
            }

            int[] types = ace.aceFrameConfig.getDestRelTypes().getSetValues();
            for (int typeNid : types) {
                ComponentVersionBI cvbi = ts.getComponentVersion(vc, typeNid);
                parentLinkages.add(cvbi);
            }

            // UPDATE TASK DETAILS
            for (BatchActionTaskBase baet : batchActionEditTaskList) {
                baet.updateExisting(existingParents, existingRefsets, existingRoles, parentLinkages);
            }

        } catch (ContraditionException ex) {
            Logger.getLogger(BatchActionEditorPanel.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
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
            baep.updateExistingLists((TerminologyListModel) lde.getSource());
        }

        @Override
        public void intervalRemoved(ListDataEvent lde) {
            baep.updateExistingLists((TerminologyListModel) lde.getSource());
        }

        @Override
        public void contentsChanged(ListDataEvent lde) {
            baep.updateExistingLists((TerminologyListModel) lde.getSource());
        }
    }

    /** Creates new form BatchActionEditorPanel */
    public BatchActionEditorPanel(ACE ace, TerminologyList list, JTextPane results) {
        this.ace = ace;
        this.ts = Ts.get();
        this.batchConceptList = list;
        this.batchConceptList.getModel().addListDataListener(new ExistingListDataListener(this));
        this.batchActionEditTaskList = new ArrayList<BatchActionTaskBase>();
        this.batchActionTaskViewPanel = new JPanel();
        this.resultsTextArea = results;

        // initComponents is autogenerated
        initComponents();

        existingParents = new ArrayList<ComponentVersionBI>();
        existingRefsets = new ArrayList<ComponentVersionBI>();
        existingRoles = new ArrayList<ComponentVersionBI>();
        updateExistingLists(batchConceptList.getModel());
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
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
            // SETUP CONCEPT LIST
            List<ConceptChronicleBI> concepts = new ArrayList<ConceptChronicleBI>();
            ListModel termList = batchConceptList.getModel();
            for (int i = 0; i < termList.getSize(); i++) {
                I_GetConceptData cb = (I_GetConceptData) termList.getElementAt(i);
                concepts.add(ts.getConcept(cb.getConceptNid()));
            }

            // SETUP TASK LIST
            ViewCoordinate vc = ace.aceFrameConfig.getViewCoordinate();
            vc = new ViewCoordinate(vc);
            vc.setRelAssertionType(RelAssertionType.STATED);
            EditCoordinate ec = ace.aceFrameConfig.getEditCoordinate();

            BatchActionTask.setup(ec, vc);
            BatchActionEventReporter.reset();

            List<BatchActionTask> tasks = new ArrayList<BatchActionTask>();
            for (BatchActionTaskBase taskBase : batchActionEditTaskList) {
                BatchActionTask tmpTask = taskBase.getTask(ec, vc);
                if (tmpTask != null) {
                    tasks.add(tmpTask);
                }
            }

            if (BatchActionEventReporter.getSize() > 0) {
                StringBuilder sb = new StringBuilder("\r\n!!! BATCH ACTION TASK LAUNCH ERROR\r\n");
                sb.append(BatchActionEventReporter.createReportTSV());
                Logger.getLogger(BatchActionEditorPanel.class.getName()).log(Level.INFO, sb.toString());

                resultsTextArea.setText(BatchActionEventReporter.createReportHTML());

                String errStr = "Incomplete Batch Action Task parameters.  See results listing.";
                AceLog.getAppLog().alertAndLog(Level.SEVERE, errStr, new Exception(errStr));
                return;
            }

            BatchActionProcessor bap = new BatchActionProcessor(concepts, tasks, ec, vc);
            ts.iterateConceptDataInParallel(bap);

            StringBuilder sb = new StringBuilder("\r\n!!! BATCH ACTION TASK REPORT\r\n");
            sb.append(BatchActionEventReporter.createReportTSV());
            Logger.getLogger(BatchActionEditorPanel.class.getName()).log(Level.INFO, sb.toString());

            resultsTextArea.setText(BatchActionEventReporter.createReportHTML());
        } catch (Exception ex) {
            Logger.getLogger(BatchActionEditorPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_actionExecuteAllTasks

    private void actionAddAnotherTask(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_actionAddAnotherTask
        actionAddAnotherTask();
    }//GEN-LAST:event_actionAddAnotherTask

    public void actionAddAnotherTask() {
        // CREATE NEW TASK
        BatchActionTaskBase batb = new BatchActionTaskBase(this);
        batb.setTaskParentUI(this);
        batb.updateExisting(existingParents, existingRefsets, existingRoles, parentLinkages);
        batb.setEnabled(true);
        batb.setVisible(true);
        batb.invalidate();
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
