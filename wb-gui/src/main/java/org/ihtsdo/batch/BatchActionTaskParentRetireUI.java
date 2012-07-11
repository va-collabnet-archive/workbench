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
import javax.swing.DefaultComboBoxModel;
import javax.swing.JPanel;
import org.ihtsdo.batch.BatchActionEvent.BatchActionEventType;
import org.ihtsdo.batch.BatchActionTask.BatchActionTaskType;
import org.ihtsdo.tk.api.ComponentVersionBI;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.coordinate.EditCoordinate;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.relationship.RelationshipVersionBI;

/**
 *
 * @author marc
 */
public class BatchActionTaskParentRetireUI extends javax.swing.JPanel implements I_BatchActionTask {

    BatchActionTask task;

    /** Creates new form BatchActionTaskParentRetireUI */
    public BatchActionTaskParentRetireUI() {
        initComponents();
        this.task = new BatchActionTaskParentRetire();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jComboBoxExistingParents = new javax.swing.JComboBox();
        jLabel1 = new javax.swing.JLabel();

        jComboBoxExistingParents.setModel(jComboBoxExistingParents.getModel());
        jComboBoxExistingParents.setRenderer(new org.ihtsdo.batch.JComboBoxExistingParentsRender());

        jLabel1.setText("Retire As Parent:");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jComboBoxExistingParents, 0, 126, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jComboBoxExistingParents, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jLabel1))
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox jComboBoxExistingParents;
    private javax.swing.JLabel jLabel1;
    // End of variables declaration//GEN-END:variables

    @Override // I_BatchActionTask
    public JPanel getPanel() {
        return this;
    }

    @Override // I_BatchActionTask
    public void updateExisting(List<RelationshipVersionBI> existingParents, List<ComponentVersionBI> existingRefsets, List<RelationshipVersionBI> existingRoles, List<ComponentVersionBI> parentLinkages) {
        DefaultComboBoxModel dcbm = (DefaultComboBoxModel) jComboBoxExistingParents.getModel();
        RelationshipVersionBI selectedItem = (RelationshipVersionBI) dcbm.getSelectedItem();

        // Sort existing parents by name.
        Comparator<RelationshipVersionBI> cmp = new Comparator<RelationshipVersionBI>() {

            @Override // Comparator
            public int compare(RelationshipVersionBI o1, RelationshipVersionBI o2) {
                return o1.toUserString().compareToIgnoreCase(o2.toUserString());
            }
        };

        // Add exitings parents to JComboBox model.
        Collections.sort(existingParents, cmp);
        dcbm.removeAllElements();
        for (RelationshipVersionBI relationshipVersionBI : existingParents) {
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
                RelationshipVersionBI cvbi = (RelationshipVersionBI) dcbm.getElementAt(i);
                if (cvbi.getNid() == selectedItem.getNid()) {
                    selectedIdx = i;
                    selectedItem = cvbi;
                    break;
                }
            }

            if (selectedIdx >= 0) {
                // prior selection exists in new list
                dcbm.setSelectedItem(selectedItem);
                jComboBoxExistingParents.setSelectedIndex(selectedIdx);
            } else {
                // prior selection does not exist in new list
                dcbm.setSelectedItem(dcbm.getElementAt(0));
                jComboBoxExistingParents.setSelectedIndex(0);
            }
        }
    }

    @Override // I_BatchActionTask
    public BatchActionTask getTask(EditCoordinate ec, ViewCoordinate vc, List<ConceptChronicleBI> concepts) throws IOException {
        // RETIRE PARENT
        DefaultComboBoxModel dcbm = (DefaultComboBoxModel) jComboBoxExistingParents.getModel();
        RelationshipVersionBI fromParentBI = (RelationshipVersionBI) dcbm.getSelectedItem();

        if (fromParentBI != null) {
            int nidLinkage = fromParentBI.getTypeNid();
            int nidParent = fromParentBI.getTargetNid();

            ((BatchActionTaskParentRetire) task).setSelectedRoleTypeNid(nidLinkage);
            ((BatchActionTaskParentRetire) task).setSelectedDestNid(nidParent);
            return task;
        } else {
            BatchActionEventReporter.add(new BatchActionEvent(null, BatchActionTaskType.PARENT_RETIRE,
                    BatchActionEventType.TASK_INVALID, "value not set"));
            return null;
        }
    }
}
