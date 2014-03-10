/**
 * Copyright (c) 2009 International Health Terminology Standards Development Organisation
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.ihtsdo.batch;

import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import org.ihtsdo.batch.BatchActionTask.BatchActionTaskType;
import org.ihtsdo.helper.descriptionlogic.DescriptionLogic;
import org.ihtsdo.tk.api.ComponentVersionBI;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.coordinate.EditCoordinate;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.relationship.RelationshipVersionBI;

/**
 *
 * @author marc
 */
public class BatchActionTaskBase extends javax.swing.JPanel {

    private final BatchActionEditorPanel editor;
    private JPanel taskParentUI;

    public void setTaskParentUI(JPanel taskParentUI) {
        this.taskParentUI = taskParentUI;
    }
    private I_BatchActionTask taskDetailUI;
    private BatchActionTaskType actionTaskType;
    private int batchActionTypeIdxCache;

    // LEVELS
    private int batchActionLevel;
    private static final int CONCEPT_LEVEL = 0;
    private static final int DESCRIPTION_LEVEL = 1;
    private static final int RELATIONSHIP_LEVEL = 2;
    private static final int LOGIC_LEVEL = 3;

    // CONCEPT LEVEL TASKS
    private static final int CONCEPT_PARENT_ADD_NEW = 0;
    private static final int CONCEPT_PARENT_REPLACE = 1;
    private static final int CONCEPT_PARENT_RETIRE = 2;
    private static final int CONCEPT_REFSET_ADD_MEMBER = 3;
    private static final int CONCEPT_REFSET_MOVE_MEMBER = 4;
    private static final int CONCEPT_REFSET_REPLACE_VALUE = 5;
    private static final int CONCEPT_REFSET_RETIRE_MEMBER = 6;

    // DESCRIPTION LEVEL TASKS
    private static final int DESCRIPTION_INITIAL_CHAR_CASE_SENSITIVITY = 0;
    private static final int DESCRIPTION_RETIRE = 1;
    private static final int DESCRIPTION_TEXT_FIND_REPLACE = 2;
    private static final int DESCRIPTION_TEXT_FIND_CREATE = 3;
    private static final int DESCRIPTION_REFSET_ADD_MEMBER = 4;
    private static final int DESCRIPTION_REFSET_CHANGE_VALUE = 5;
    private static final int DESCRIPTION_REFSET_RETIRE_MEMBER = 6;

    // RELATIONSHIP LEVEL TASKS
    private static final int RELATIONSHIP_ROLE_ADD = 0;
    private static final int RELATIONSHIP_ROLE_REPLACE_VALUE = 1;
    private static final int RELATIONSHIP_ROLE_RETIRE = 2;

    // LOGIC LEVEL TASKS
    private static final int LOGIC_DISJOINT_SET_ADD = 0;
    private static final int LOGIC_DISJOINT_SET_RETIRE = 1;
    private static final int LOGIC_NEGATE_RELATIONSHIP_VALUE = 2;
    private static final int LOGIC_UNION_SET_CREATE = 3;
    private static final int LOGIC_UNION_SET_RETIRE = 4;

    /**
     * Creates new form BatchActionTaskBase
     *
     * @param batchActionEditorPanel
     */
    public BatchActionTaskBase(BatchActionEditorPanel batchActionEditorPanel) {
        this.editor = batchActionEditorPanel;

        initComponents();

        // INITIALIZE DEFAULT TASK
        this.taskDetailUI = new BatchActionTaskConceptParentAddNewUI();
        this.actionTaskType = BatchActionTaskType.CONCEPT_PARENT_ADD_NEW;

        // INITIALIZE DETAIL PANEL
        GroupLayout layout = (GroupLayout) this.getLayout();
        layout.replace(jPanelTaskDetail, taskDetailUI.getPanel());
        jPanelTaskDetail = taskDetailUI.getPanel();
        this.invalidate();

        batchActionLevel = 0; // Initial Concept Level
        batchActionTypeIdxCache = 0; // Initial Task
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        btnAddTask = new javax.swing.JButton();
        btnDeleteTask = new javax.swing.JButton();
        jComboTaskType = new javax.swing.JComboBox();
        jPanelTaskDetail = new javax.swing.JPanel();
        jComboTaskLevel = new javax.swing.JComboBox();

        setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));

        btnAddTask.setIcon(new javax.swing.ImageIcon(getClass().getResource("/add2.png"))); // NOI18N
        btnAddTask.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                actionAddAnotherTask(evt);
            }
        });

        btnDeleteTask.setIcon(new javax.swing.ImageIcon(getClass().getResource("/delete2.png"))); // NOI18N
        btnDeleteTask.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                actionDeleteThisTask(evt);
            }
        });

        jComboTaskType.setModel(newBatchActionTypeComboBoxModel());
        jComboTaskType.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                actionSelectTaskType(evt);
            }
        });

        javax.swing.GroupLayout jPanelTaskDetailLayout = new javax.swing.GroupLayout(jPanelTaskDetail);
        jPanelTaskDetail.setLayout(jPanelTaskDetailLayout);
        jPanelTaskDetailLayout.setHorizontalGroup(
            jPanelTaskDetailLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        jPanelTaskDetailLayout.setVerticalGroup(
            jPanelTaskDetailLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 52, Short.MAX_VALUE)
        );

        jComboTaskLevel.setModel(newBatchActionLevelComboBoxModel());
        jComboTaskLevel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                actionSelectTaskLevel(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(btnAddTask)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnDeleteTask)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jComboTaskLevel, 0, 130, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jComboTaskType, javax.swing.GroupLayout.PREFERRED_SIZE, 261, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
            .addComponent(jPanelTaskDetail, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jComboTaskType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jComboTaskLevel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(btnDeleteTask)
                    .addComponent(btnAddTask))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanelTaskDetail, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void actionSelectTaskType(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_actionSelectTaskType
        int idx = ((JComboBox) evt.getSource()).getSelectedIndex();
        switch (batchActionLevel) {
            case CONCEPT_LEVEL:
                switch (idx) {
                    case CONCEPT_PARENT_ADD_NEW: // PARENT_ADD_NEW
                        if (actionTaskType.compareTo(BatchActionTaskType.CONCEPT_PARENT_ADD_NEW) != 0) {
                            actionTaskType = BatchActionTaskType.CONCEPT_PARENT_ADD_NEW;
                            taskDetailUI = new BatchActionTaskConceptParentAddNewUI();
                            batchActionTypeIdxCache = idx;
                        }
                        break;
                    case CONCEPT_PARENT_REPLACE: // PARENT_REPLACE
                        if (actionTaskType.compareTo(BatchActionTaskType.CONCEPT_PARENT_REPLACE) != 0) {
                            actionTaskType = BatchActionTaskType.CONCEPT_PARENT_REPLACE;
                            taskDetailUI = new BatchActionTaskConceptParentReplaceUI();
                            batchActionTypeIdxCache = idx;
                        }
                        break;
                    case CONCEPT_PARENT_RETIRE: // CONCEPT_PARENT_RETIRE
                        if (actionTaskType.compareTo(BatchActionTaskType.CONCEPT_PARENT_RETIRE) != 0) {
                            actionTaskType = BatchActionTaskType.CONCEPT_PARENT_RETIRE;
                            taskDetailUI = new BatchActionTaskConceptParentRetireUI();
                            batchActionTypeIdxCache = idx;
                        }
                        break;
                    case CONCEPT_REFSET_ADD_MEMBER: // CONCEPT_REFSET_ADD_MEMBER
                        if (actionTaskType.compareTo(BatchActionTaskType.CONCEPT_REFSET_ADD_MEMBER) != 0) {
                            actionTaskType = BatchActionTaskType.CONCEPT_REFSET_ADD_MEMBER;
                            taskDetailUI = new BatchActionTaskConceptRefsetAddMemberUI();
                            batchActionTypeIdxCache = idx;
                        }
                        break;
                    case CONCEPT_REFSET_MOVE_MEMBER: // CONCEPT_REFSET_MOVE_MEMBER
                        if (actionTaskType.compareTo(BatchActionTaskType.CONCEPT_REFSET_MOVE_MEMBER) != 0) {
                            actionTaskType = BatchActionTaskType.CONCEPT_REFSET_MOVE_MEMBER;
                            taskDetailUI = new BatchActionTaskConceptRefsetMoveMemberUI();
                            batchActionTypeIdxCache = idx;
                        }
                        break;
                    case CONCEPT_REFSET_REPLACE_VALUE: // CONCEPT_REFSET_REPLACE_VALUE
                        if (actionTaskType.compareTo(BatchActionTaskType.CONCEPT_REFSET_REPLACE_VALUE) != 0) {
                            actionTaskType = BatchActionTaskType.CONCEPT_REFSET_REPLACE_VALUE;
                            taskDetailUI = new BatchActionTaskConceptRefsetReplaceValueUI();
                            batchActionTypeIdxCache = idx;
                        }
                        break;
                    case CONCEPT_REFSET_RETIRE_MEMBER: // CONCEPT_REFSET_RETIRE_MEMBER
                        if (actionTaskType.compareTo(BatchActionTaskType.CONCEPT_REFSET_RETIRE_MEMBER) != 0) {
                            actionTaskType = BatchActionTaskType.CONCEPT_REFSET_RETIRE_MEMBER;
                            taskDetailUI = new BatchActionTaskConceptRefsetRetireMemberUI();
                            batchActionTypeIdxCache = idx;
                        }
                        break;
                    default:
                        if (batchActionTypeIdxCache >= 0
                                && batchActionTypeIdxCache < jComboTaskType.getItemCount()) {
                            jComboTaskType.setSelectedIndex(batchActionTypeIdxCache);
                        }
                        break;
                }
                break;

            case DESCRIPTION_LEVEL:
                switch (idx) {
                    case DESCRIPTION_INITIAL_CHAR_CASE_SENSITIVITY:
                        if (actionTaskType.compareTo(BatchActionTaskType.DESCRIPTION_INITIAL_CHAR_CASE_SENSITIVITY) != 0) {
                            actionTaskType = BatchActionTaskType.DESCRIPTION_INITIAL_CHAR_CASE_SENSITIVITY;
                            taskDetailUI = new BatchActionTaskDescriptionInitialCapsUI();
                            batchActionTypeIdxCache = idx;
                        }
                        break;
                    case DESCRIPTION_RETIRE:
                        if (actionTaskType.compareTo(BatchActionTaskType.DESCRIPTION_RETIRE) != 0) {
                            actionTaskType = BatchActionTaskType.DESCRIPTION_RETIRE;
                            taskDetailUI = new BatchActionTaskDescriptionRetireUI();
                            batchActionTypeIdxCache = idx;
                        }
                        break;
                    case DESCRIPTION_TEXT_FIND_REPLACE:
                        if (actionTaskType.compareTo(BatchActionTaskType.DESCRIPTION_TEXT_FIND_REPLACE) != 0) {
                            actionTaskType = BatchActionTaskType.DESCRIPTION_TEXT_FIND_REPLACE;
                            taskDetailUI = new BatchActionTaskDescriptionTextFindReplaceUI();
                            batchActionTypeIdxCache = idx;
                        }
                        break;
                    case DESCRIPTION_TEXT_FIND_CREATE:
                        if (actionTaskType.compareTo(BatchActionTaskType.DESCRIPTION_TEXT_FIND_CREATE) != 0) {
                            actionTaskType = BatchActionTaskType.DESCRIPTION_TEXT_FIND_CREATE;
                            taskDetailUI = new BatchActionTaskDescriptionTextFindCreateUI();
                            batchActionTypeIdxCache = idx;
                        }
                        break;
                    case DESCRIPTION_REFSET_ADD_MEMBER:
                        if (actionTaskType.compareTo(BatchActionTaskType.DESCRIPTION_REFSET_ADD_MEMBER) != 0) {
                            actionTaskType = BatchActionTaskType.DESCRIPTION_REFSET_ADD_MEMBER;
                            taskDetailUI = new BatchActionTaskDescriptionRefsetAddMemberUI();
                            batchActionTypeIdxCache = idx;
                        }
                        break;
                    case DESCRIPTION_REFSET_CHANGE_VALUE:
                        if (actionTaskType.compareTo(BatchActionTaskType.DESCRIPTION_REFSET_CHANGE_VALUE) != 0) {
                            actionTaskType = BatchActionTaskType.DESCRIPTION_REFSET_CHANGE_VALUE;
                            taskDetailUI = new BatchActionTaskDescriptionRefsetReplaceValueUI();
                            batchActionTypeIdxCache = idx;
                        }
                        break;
                    case DESCRIPTION_REFSET_RETIRE_MEMBER:
                        if (actionTaskType.compareTo(BatchActionTaskType.DESCRIPTION_REFSET_RETIRE_MEMBER) != 0) {
                            actionTaskType = BatchActionTaskType.DESCRIPTION_REFSET_RETIRE_MEMBER;
                            taskDetailUI = new BatchActionTaskDescriptionRefsetRetireMemberUI();
                            batchActionTypeIdxCache = idx;
                        }
                        break;
                    default:
                        if (batchActionTypeIdxCache >= 0
                                && batchActionTypeIdxCache < jComboTaskType.getItemCount()) {
                            jComboTaskType.setSelectedIndex(batchActionTypeIdxCache);
                        }
                        break;
                }
                break;

            case RELATIONSHIP_LEVEL:
                switch (idx) {

                    case RELATIONSHIP_ROLE_ADD: // RELATIONSHIP_ROLE_ADD
                        if (actionTaskType.compareTo(BatchActionTaskType.RELATIONSHIP_ROLE_ADD) != 0) {
                            actionTaskType = BatchActionTaskType.RELATIONSHIP_ROLE_ADD;
                            taskDetailUI = new BatchActionTaskRelationshipRoleAddUI();
                            batchActionTypeIdxCache = idx;
                        }
                        break;
                    case RELATIONSHIP_ROLE_REPLACE_VALUE: // RELATIONSHIP_ROLE_REPLACE_VALUE
                        if (actionTaskType.compareTo(BatchActionTaskType.RELATIONSHIP_ROLE_REPLACE_VALUE) != 0) {
                            actionTaskType = BatchActionTaskType.RELATIONSHIP_ROLE_REPLACE_VALUE;
                            taskDetailUI = new BatchActionTaskRelationshipRoleReplaceValueUI();
                            batchActionTypeIdxCache = idx;
                        }
                        break;
                    case RELATIONSHIP_ROLE_RETIRE: // RELATIONSHIP_ROLE_RETIRE
                        if (actionTaskType.compareTo(BatchActionTaskType.RELATIONSHIP_ROLE_RETIRE) != 0) {
                            actionTaskType = BatchActionTaskType.RELATIONSHIP_ROLE_RETIRE;
                            taskDetailUI = new BatchActionTaskRelationshipRoleRetireUI();
                            batchActionTypeIdxCache = idx;
                        }
                        break;
                    default:
                        if (batchActionTypeIdxCache >= 0
                                && batchActionTypeIdxCache < jComboTaskType.getItemCount()) {
                            jComboTaskType.setSelectedIndex(batchActionTypeIdxCache);
                        }
                        break;
                }
                break;

            case LOGIC_LEVEL:
                switch (idx) {
                    case LOGIC_DISJOINT_SET_ADD: // LOGIC_DISJOINT_SET_ADD :SNOOWL:
                        if (actionTaskType.compareTo(BatchActionTaskType.LOGIC_DISJOINT_SET_ADD) != 0) {
                            actionTaskType = BatchActionTaskType.LOGIC_DISJOINT_SET_ADD;
                            taskDetailUI = new BatchActionTaskLogicDisjointAddUI(editor.ace.aceFrameConfig);
                            batchActionTypeIdxCache = idx;
                        }
                        break;
                    case LOGIC_DISJOINT_SET_RETIRE: // LOGIC_DISJOINT_SET_RETIRE :SNOOWL:
                        if (actionTaskType.compareTo(BatchActionTaskType.LOGIC_DISJOINT_SET_RETIRE) != 0) {
                            actionTaskType = BatchActionTaskType.LOGIC_DISJOINT_SET_RETIRE;
                            taskDetailUI = new BatchActionTaskLogicDisjointRetireUI();
                            batchActionTypeIdxCache = idx;
                        }
                        break;
                    case LOGIC_NEGATE_RELATIONSHIP_VALUE: // LOGIC_NEGATE_RELATIONSHIP_VALUE :SNOOWL:
                        if (actionTaskType.compareTo(BatchActionTaskType.LOGIC_NEGATE_RELATIONSHIP_VALUE) != 0) {
                            actionTaskType = BatchActionTaskType.LOGIC_NEGATE_RELATIONSHIP_VALUE;
                            taskDetailUI = new BatchActionTaskLogicNegateRelValueUI();
                            batchActionTypeIdxCache = idx;
                        }
                        break;
                    case LOGIC_UNION_SET_CREATE: // LOGIC_UNION_SET_CREATE :SNOOWL:
                        if (actionTaskType.compareTo(BatchActionTaskType.LOGIC_UNION_SET_CREATE) != 0) {
                            actionTaskType = BatchActionTaskType.LOGIC_UNION_SET_CREATE;
                            taskDetailUI = new BatchActionTaskLogicUnionCreateUI(editor.ace.aceFrameConfig);
                            batchActionTypeIdxCache = idx;
                        }
                        break;
                    case LOGIC_UNION_SET_RETIRE: // LOGIC_UNION_SET_RETIRE :SNOOWL:
                        if (actionTaskType.compareTo(BatchActionTaskType.LOGIC_UNION_SET_RETIRE) != 0) {
                            actionTaskType = BatchActionTaskType.LOGIC_UNION_SET_RETIRE;
                            taskDetailUI = new BatchActionTaskLogicUnionRetireUI();
                            batchActionTypeIdxCache = idx;
                        }
                        break;
                    default:
                        if (batchActionTypeIdxCache >= 0
                                && batchActionTypeIdxCache < jComboTaskType.getItemCount()) {
                            jComboTaskType.setSelectedIndex(batchActionTypeIdxCache);
                        }
                        break;
                }
        }
        
        // INITIALIZE DETAIL PANEL
        taskDetailUI.updateExisting(editor.getExistingParents(), editor.getExistingRefsets(),
                editor.getExistingDescriptionRefsets(),
                editor.getExistingRoles(), editor.getParentLinkages());
        GroupLayout layout = (GroupLayout) this.getLayout();
        layout.replace(jPanelTaskDetail, taskDetailUI.getPanel());
        jPanelTaskDetail = taskDetailUI.getPanel();

        this.invalidate();
        this.validate();
        this.doLayout();
        taskParentUI.invalidate();
        taskParentUI.validate();
        taskParentUI.doLayout();
    }//GEN-LAST:event_actionSelectTaskType

    private void actionAddAnotherTask(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_actionAddAnotherTask
        editor.actionAddAnotherTask();
    }//GEN-LAST:event_actionAddAnotherTask

    private void actionDeleteThisTask(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_actionDeleteThisTask
        editor.actionDeleteTask(this);
    }//GEN-LAST:event_actionDeleteThisTask

    private void actionSelectTaskLevel(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_actionSelectTaskLevel
        int idx = ((JComboBox) evt.getSource()).getSelectedIndex();
        switch (idx) {
            case CONCEPT_LEVEL: // 0
                batchActionLevel = CONCEPT_LEVEL;
                break;
            case DESCRIPTION_LEVEL: // 1
                batchActionLevel = DESCRIPTION_LEVEL;
                break;
            case RELATIONSHIP_LEVEL: // 2
                batchActionLevel = RELATIONSHIP_LEVEL;
                break;
            case LOGIC_LEVEL: // 3
                batchActionLevel = LOGIC_LEVEL;
                break;
            default:
                if (batchActionLevel < jComboTaskLevel.getItemCount()) {
                    // reselect what was unselected
                    jComboTaskLevel.setSelectedIndex(batchActionLevel);
                    return;
                }
                break;
        }
        jComboTaskType.setModel(newBatchActionTypeComboBoxModel());
        jComboTaskType.setSelectedIndex(0);
    }//GEN-LAST:event_actionSelectTaskLevel

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAddTask;
    private javax.swing.JButton btnDeleteTask;
    private javax.swing.JComboBox jComboTaskLevel;
    private javax.swing.JComboBox jComboTaskType;
    private javax.swing.JPanel jPanelTaskDetail;
    // End of variables declaration//GEN-END:variables

    BatchActionTask getTask(EditCoordinate ec, ViewCoordinate vc, List<ConceptChronicleBI> concepts) throws Exception {
        return taskDetailUI.getTask(ec, vc, concepts);
    }

    void updateExisting(List<RelationshipVersionBI> existingParents, 
            List<ComponentVersionBI> existingRefsets,
            List<ComponentVersionBI> existingDescriptionRefsets,
            List<RelationshipVersionBI> existingRoles, List<ComponentVersionBI> parentLinkages) {
        taskDetailUI.updateExisting(
                existingParents, existingRefsets, 
                existingDescriptionRefsets, 
                existingRoles, parentLinkages);
    }

    private DefaultComboBoxModel newBatchActionLevelComboBoxModel() {
        String[] sa;
        if (DescriptionLogic.isVisible()) {
            sa = new String[]{
                "Concept",
                "Description",
                "Relationship",
                "Logic"
            };
        } else {
            sa = new String[]{
                "Concept",
                "Description",
                "Relationship",};
        }

        return new javax.swing.DefaultComboBoxModel(sa);
    }

    private DefaultComboBoxModel newBatchActionTypeComboBoxModel() {
        String[] sa;
        switch (batchActionLevel) {
            case CONCEPT_LEVEL:
                sa = new String[]{
                    "Parent, Add New",
                    "Parent, Replace",
                    "Parent, Retire",
                    "Refset, Add Member",
                    "Refset, Move Member",
                    "Refset, Replace Value",
                    "Refset, Retire Member"
                };
                break;
            case DESCRIPTION_LEVEL:
                sa = new String[]{
                    "Initial Character Case Sensitiviy",
                    "Retire Description",
                    "Text, Find & Replace",
                    "Text, Find & Create Another",
                    "Refset, Add Membership",
                    "Refset, Change Value",
                    "Refset, Retire Membership"
                };
                break;
            case RELATIONSHIP_LEVEL:
                sa = new String[]{
                    "Role, Add",
                    "Role, Replace Value",
                    "Role, Retire"
                };
                break;
            case LOGIC_LEVEL:
                sa = new String[]{
                    "Add Disjoint Set",
                    "Retire Disjoint Set",
                    "Negate Relationship Value",
                    "Create Union Set",
                    "Retire Union Set"
                };
                break;
            default:
                sa = new String[]{
                    "TASK LEVEL NOT SET"
                };
                break;
        }

        return new javax.swing.DefaultComboBoxModel(sa);
    }

}
