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

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import org.dwfa.ace.api.I_AmTermComponent;
import org.ihtsdo.batch.BatchActionEvent.BatchActionEventType;
import org.ihtsdo.batch.BatchActionTask.BatchActionTaskType;
import org.ihtsdo.tk.api.ComponentVersionBI;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.coordinate.EditCoordinate;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.relationship.RelationshipVersionBI;
import org.ihtsdo.tk.dto.concept.component.refex.TK_REFEX_TYPE;

/**
 *
 * @author marc
 */
public class BatchActionTaskRefsetMoveMemberUI extends javax.swing.JPanel implements I_BatchActionTask {

    BatchActionTask task;
    boolean useFilter;
    int currentValueTypeIdx;

    /** Creates new form BatchActionTaskRefsetMoveMemberUI */
    public BatchActionTaskRefsetMoveMemberUI() {
        initComponents();

        // TASK
        this.task = new BatchActionTaskRefsetMoveMember();

        // Setup DnD Move To Panel
        ValueDndNidUI tmp = new ValueDndNidUI("Move To:");
        GroupLayout layout = (GroupLayout) this.getLayout();
        layout.replace(jPanelDndRefsetMoveTo, tmp.getPanel());
        jPanelDndRefsetMoveTo = tmp.getPanel();

        // Setup Filter Value Panel
        tmp = new ValueDndNidUI("Concept Match Value:");
        layout.replace(jPanelValueMatch, tmp.getPanel());
        jPanelValueMatch = tmp.getPanel();

        currentValueTypeIdx = 1; // concept

        useFilter = false;
        jCheckBoxMatch.setSelected(useFilter);
        jPanelValueMatch.setEnabled(useFilter);
        jPanelValueMatch.setVisible(useFilter);
        jComboBoxType.setEnabled(useFilter);
        jComboBoxType.setVisible(useFilter);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jComboBoxExistingRefsets = new javax.swing.JComboBox();
        jPanelDndRefsetMoveTo = new javax.swing.JPanel();
        jCheckBoxMatch = new javax.swing.JCheckBox();
        jComboBoxType = new javax.swing.JComboBox();
        jPanelValueMatch = new javax.swing.JPanel();

        setPreferredSize(new java.awt.Dimension(218, 67));

        jLabel1.setText("Move Fr0m:");

        jComboBoxExistingRefsets.setModel(jComboBoxExistingRefsets.getModel());
        jComboBoxExistingRefsets.setRenderer(new org.ihtsdo.batch.JComboBoxExistingRefsetsRender());

        javax.swing.GroupLayout jPanelDndRefsetMoveToLayout = new javax.swing.GroupLayout(jPanelDndRefsetMoveTo);
        jPanelDndRefsetMoveTo.setLayout(jPanelDndRefsetMoveToLayout);
        jPanelDndRefsetMoveToLayout.setHorizontalGroup(
            jPanelDndRefsetMoveToLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 218, Short.MAX_VALUE)
        );
        jPanelDndRefsetMoveToLayout.setVerticalGroup(
            jPanelDndRefsetMoveToLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 28, Short.MAX_VALUE)
        );

        jCheckBoxMatch.setText("If Matches Value 2:");
        jCheckBoxMatch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxMatchActionPerformed(evt);
            }
        });

        jComboBoxType.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "boolean", "concept", "integer", "string" }));
        jComboBoxType.setSelectedIndex(1);
        jComboBoxType.setSelectedItem("concept");
        jComboBoxType.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxTypeActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanelValueMatchLayout = new javax.swing.GroupLayout(jPanelValueMatch);
        jPanelValueMatch.setLayout(jPanelValueMatchLayout);
        jPanelValueMatchLayout.setHorizontalGroup(
            jPanelValueMatchLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 218, Short.MAX_VALUE)
        );
        jPanelValueMatchLayout.setVerticalGroup(
            jPanelValueMatchLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 32, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jComboBoxExistingRefsets, 0, 140, Short.MAX_VALUE))
            .addComponent(jPanelDndRefsetMoveTo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanelValueMatch, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jCheckBoxMatch)
                    .addComponent(jComboBoxType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jComboBoxExistingRefsets, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanelDndRefsetMoveTo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBoxMatch)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jComboBoxType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanelValueMatch, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jCheckBoxMatchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxMatchActionPerformed
        useFilter = ((JCheckBox) evt.getSource()).getModel().isSelected();
        jPanelValueMatch.setEnabled(useFilter);
        jPanelValueMatch.setVisible(useFilter);
        jComboBoxType.setEnabled(useFilter);
        jComboBoxType.setVisible(useFilter);
    }//GEN-LAST:event_jCheckBoxMatchActionPerformed

    private void jComboBoxTypeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxTypeActionPerformed
        int idx = ((JComboBox) evt.getSource()).getSelectedIndex();
        if (idx != currentValueTypeIdx) {
            currentValueTypeIdx = idx;
            GroupLayout layout = (GroupLayout) this.getLayout();
            switch (idx) {
                case 0: // boolean
                    // FILTER PANEL
                    ValueBooleanUI tmpB = new ValueBooleanUI("Boolean Match Value:");
                    layout.replace(jPanelValueMatch, tmpB.getPanel());
                    jPanelValueMatch = tmpB.getPanel();
                    break;
                case 1: // concept
                    // FILTER PANEL
                    ValueDndNidUI tmpC = new ValueDndNidUI("Concept Match Value:");
                    layout.replace(jPanelValueMatch, tmpC.getPanel());
                    jPanelValueMatch = tmpC.getPanel();
                    break;
                case 2: // integer
                    // FILTER PANEL
                    ValueIntUI tmpI = new ValueIntUI("Integer Match Value:");
                    layout.replace(jPanelValueMatch, tmpI.getPanel());
                    jPanelValueMatch = tmpI.getPanel();
                    break;
                case 3: // string
                    // FILTER PANEL
                    ValueStringUI tmpS = new ValueStringUI("String Match Value:");
                    layout.replace(jPanelValueMatch, tmpS.getPanel());
                    jPanelValueMatch = tmpS.getPanel();
                    break;
                default:
                    throw new AssertionError();
            }

            this.invalidate();
            this.validate();
            this.doLayout();
        }
    }//GEN-LAST:event_jComboBoxTypeActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox jCheckBoxMatch;
    private javax.swing.JComboBox jComboBoxExistingRefsets;
    private javax.swing.JComboBox jComboBoxType;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanelDndRefsetMoveTo;
    private javax.swing.JPanel jPanelValueMatch;
    // End of variables declaration//GEN-END:variables

    @Override // I_BatchActionTask
    public JPanel getPanel() {
        return this;
    }

    @Override // I_BatchActionTask
    public void updateExisting(List<RelationshipVersionBI> existingParents, List<ComponentVersionBI> existingRefsets, List<RelationshipVersionBI> existingRoles, List<ComponentVersionBI> parentLinkages) {
        DefaultComboBoxModel dcbm = (DefaultComboBoxModel) jComboBoxExistingRefsets.getModel();
        ComponentVersionBI selectedItem = (ComponentVersionBI) dcbm.getSelectedItem();

        // Sort existing parents by name.
        Comparator<ComponentVersionBI> cmp = new Comparator<ComponentVersionBI>() {

            @Override // Compare
            public int compare(ComponentVersionBI o1, ComponentVersionBI o2) {
                return o1.toUserString().compareToIgnoreCase(o2.toUserString());
            }
        };

        // Add exitings parents to JComboBox model.
        Collections.sort(existingRefsets, cmp);
        dcbm.removeAllElements();
        for (ComponentVersionBI componentVersionBI : existingRefsets) {
            dcbm.addElement(componentVersionBI);
        }

        if (dcbm.getSize() == 0) {
            // empty list
        } else if (selectedItem == null) {
            // no prior selection
            dcbm.setSelectedItem(dcbm.getElementAt(0));
        } else {
            // Search by nid
            int selectedIdx = -1;
            for (int i = 0; i < dcbm.getSize(); i++) {
                ComponentVersionBI cvbi = (ComponentVersionBI) dcbm.getElementAt(i);
                if (cvbi.getNid() == selectedItem.getNid()) {
                    selectedIdx = i;
                    selectedItem = cvbi;
                    break;
                }
            }

            if (selectedIdx >= 0) {
                // prior selection exists in new list
                dcbm.setSelectedItem(selectedItem);
                jComboBoxExistingRefsets.setSelectedIndex(selectedIdx);
            } else {
                // prior selection does not exist in new list
                dcbm.setSelectedItem(dcbm.getElementAt(0));
                jComboBoxExistingRefsets.setSelectedIndex(0);
            }
        }

    }

    @Override // I_BatchActionTask
    public BatchActionTask getTask(EditCoordinate ec, ViewCoordinate vc, List<ConceptChronicleBI> concepts) {
        // referenced component provided at execution time

        // SET REFSET MOVE FROM EXITING COLLECTION NID
        DefaultComboBoxModel dcbm = (DefaultComboBoxModel) jComboBoxExistingRefsets.getModel();
        ComponentVersionBI refsetBI = (ComponentVersionBI) dcbm.getSelectedItem();
        if (refsetBI != null) {
            int refsetMoveFromNid = refsetBI.getNid();
            ((BatchActionTaskRefsetMoveMember) task).setCollectionFromNid(refsetMoveFromNid);
        } else {
            BatchActionEventReporter.add(new BatchActionEvent(null, BatchActionTaskType.REFSET_MOVE_MEMBER,
                    BatchActionEventType.TASK_INVALID, "no selected (move from) refset"));
            return null;
        }

        // SET REFSET DND COLLECTION NID
        I_AmTermComponent refsetMoveToCB = ((ValueDndNidUI) jPanelDndRefsetMoveTo).getTermComponent();
        if (refsetMoveToCB != null) {
            ((BatchActionTaskRefsetMoveMember) task).setCollectionToNid(refsetMoveToCB.getConceptNid());
        } else {
            BatchActionEventReporter.add(new BatchActionEvent(null, BatchActionTaskType.REFSET_MOVE_MEMBER,
                    BatchActionEventType.TASK_INVALID, "missing (move from) refset"));
            return null;
        }

        // CHECK MATCH FILTER
        if (jCheckBoxMatch.isSelected() == false) {
            ((BatchActionTaskRefsetMoveMember) task).setRefsetType(null);
            ((BatchActionTaskRefsetMoveMember) task).setMatchValue(null);
            return task;
        }

        // SET MATCH TYPE AND VALUE
        switch (jComboBoxType.getSelectedIndex()) {
            case 0:
                ((BatchActionTaskRefsetMoveMember) task).setRefsetType(TK_REFEX_TYPE.BOOLEAN);
                Boolean valBoolean = ((ValueBooleanUI) jPanelValueMatch).getValue();
                if (valBoolean != null) {
                    ((BatchActionTaskRefsetMoveMember) task).setMatchValue(valBoolean);
                    return task;
                }
                break;
            case 1:
                ((BatchActionTaskRefsetMoveMember) task).setRefsetType(TK_REFEX_TYPE.CID);
                Integer valConcept = ((ValueDndNidUI) jPanelValueMatch).getValue();
                if (valConcept != null) {
                    ((BatchActionTaskRefsetMoveMember) task).setMatchValue(valConcept);
                    return task;
                }
                break;
            case 2:
                ((BatchActionTaskRefsetMoveMember) task).setRefsetType(TK_REFEX_TYPE.INT);
                Integer valInt = ((ValueIntUI) jPanelValueMatch).getValue();
                if (valInt != null) {
                    ((BatchActionTaskRefsetMoveMember) task).setMatchValue(valInt);
                    return task;
                }
                break;
            case 3:
                ((BatchActionTaskRefsetMoveMember) task).setRefsetType(TK_REFEX_TYPE.STR);
                String valStr = ((ValueStringUI) jPanelValueMatch).getValue();
                if (valStr != null) {
                    ((BatchActionTaskRefsetMoveMember) task).setMatchValue(valStr);
                    return task;
                }
                break;
            default:
                throw new AssertionError();
        }

        BatchActionEventReporter.add(new BatchActionEvent(null, BatchActionTaskType.REFSET_MOVE_MEMBER,
                BatchActionEventType.TASK_INVALID, "match value not set"));
        return null;
    }
}
