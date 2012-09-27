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

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JPanel;
import org.ihtsdo.batch.BatchActionEvent.BatchActionEventType;
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
public class BatchActionTaskLogicNegateRelValueUI extends javax.swing.JPanel implements I_BatchActionTask {

    BatchActionTask task;

    /** Creates new form BatchActionTaskLogicNegateRelValueUI */
    public BatchActionTaskLogicNegateRelValueUI() {
        initComponents();
        this.task = new BatchActionTaskLogicNegateRelValue();

        // Setup DnD Panel
        ValueDndNidUI tmp = new ValueDndNidUI("New Role Value:");
        GroupLayout layout = (GroupLayout) this.getLayout();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jComboBoxExistingRoles = new javax.swing.JComboBox();
        jLabelExistingRole = new javax.swing.JLabel();

        setPreferredSize(new java.awt.Dimension(218, 67));

        jComboBoxExistingRoles.setModel(jComboBoxExistingRoles.getModel());
        jComboBoxExistingRoles.setRenderer(new org.ihtsdo.batch.JComboBoxExistingRolesRender());
        jComboBoxExistingRoles.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxExistingRolesActionPerformed(evt);
            }
        });

        jLabelExistingRole.setText("Negate Value:");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jLabelExistingRole)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jComboBoxExistingRoles, 0, 127, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jComboBoxExistingRoles, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jLabelExistingRole))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jComboBoxExistingRolesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxExistingRolesActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jComboBoxExistingRolesActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox jComboBoxExistingRoles;
    private javax.swing.JLabel jLabelExistingRole;
    // End of variables declaration//GEN-END:variables

    @Override // I_BatchActionTask
    public JPanel getPanel() {
        return this;
    }

    @Override // I_BatchActionTask
    public void updateExisting(List<RelationshipVersionBI> existingParents, List<ComponentVersionBI> existingRefsets, List<RelationshipVersionBI> existingRoles, List<ComponentVersionBI> parentLinkages) {
        DefaultComboBoxModel dcbm = (DefaultComboBoxModel) jComboBoxExistingRoles.getModel();
        ComponentVersionBI selectedItem = (ComponentVersionBI) dcbm.getSelectedItem();

        // Sort existing parents by name.
        Comparator<ComponentVersionBI> cmp = new Comparator<ComponentVersionBI>() {

            @Override
            public int compare(ComponentVersionBI o1, ComponentVersionBI o2) {
                return o1.toUserString().compareToIgnoreCase(o2.toUserString());
            }
        };

        // Add exitings parents to JComboBox model.
        Collections.sort(existingRoles, cmp);
        dcbm.removeAllElements();
        for (RelationshipVersionBI relationshipVersionBI : existingRoles) {
            dcbm.addElement(relationshipVersionBI);
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
                RelationshipVersionBI rvbi = (RelationshipVersionBI) dcbm.getElementAt(i);
                if (rvbi.getNid() == selectedItem.getNid()) {
                    selectedIdx = i;
                    selectedItem = rvbi;
                    break;
                }
            }

            if (selectedIdx >= 0) {
                // prior selection exists in new list
                dcbm.setSelectedItem(selectedItem);
                jComboBoxExistingRoles.setSelectedIndex(selectedIdx);
            } else {
                // prior selection does not exist in new list
                dcbm.setSelectedItem(dcbm.getElementAt(0));
                jComboBoxExistingRoles.setSelectedIndex(0);
            }
        }
    }

    @Override // I_BatchActionTask
    public BatchActionTask getTask(EditCoordinate ec, ViewCoordinate vc, List<ConceptChronicleBI> concepts) {
        DefaultComboBoxModel dcbm = (DefaultComboBoxModel) jComboBoxExistingRoles.getModel();
        RelationshipVersionBI roleValueBI = (RelationshipVersionBI) dcbm.getSelectedItem();

        if (roleValueBI != null) {
            // SET ROLE TYPE
            int roleTypeNid = roleValueBI.getTypeNid();
            ((BatchActionTaskLogicNegateRelValue) task).setRoleNid(roleTypeNid);
            // SET ROLE VALUE
            int valueNid = roleValueBI.getTargetNid();
            ((BatchActionTaskLogicNegateRelValue) task).setValueNid(valueNid);
            // SET ROLE GROUP
            ((BatchActionTaskLogicNegateRelValue) task).setRoleGroup(-1);
        } else {
            BatchActionEventReporter.add(new BatchActionEvent(null,
                    BatchActionTaskType.LOGIC_NEGATE_RELATIONSHIP_VALUE,
                    BatchActionEventType.TASK_INVALID, "no selected role"));
            return null;
        }
        try {
            // SET NEGATION REFSET NID
            ((BatchActionTaskLogicNegateRelValue) task).setCollectionNid(
                    DescriptionLogic.getNegationRefsetNid());
        } catch (IOException ex) {
            Logger.getLogger(BatchActionTaskLogicNegateRelValueUI.class.getName()).
                    log(Level.SEVERE, null, ex);
            BatchActionEventReporter.add(new BatchActionEvent(null,
                    BatchActionTaskType.LOGIC_NEGATE_RELATIONSHIP_VALUE,
                    BatchActionEventType.TASK_INVALID, "error setting nedation refset collection nid"));
            return null;
        }

        return task;
    }
}