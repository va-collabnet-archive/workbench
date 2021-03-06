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

import java.util.List;
import javax.swing.GroupLayout;
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
public class BatchActionTaskConceptRefsetAddMemberUI extends javax.swing.JPanel implements I_BatchActionTask {

    BatchActionTask task;
    int currentValueTypeIdx;

    /** Creates new form BatchActionTaskConceptRefsetAddMemberUI */
    public BatchActionTaskConceptRefsetAddMemberUI() {
        initComponents();

        // TASK
        this.task = new BatchActionTaskConceptRefsetAddMember();

        // Setup DnD Add Refset Panel
        ValueDndNidUI tmp = new ValueDndNidUI("To Refset:");
        GroupLayout layout = (GroupLayout) this.getLayout();
        layout.replace(jPanelDndRefsetAddTo, tmp.getPanel());
        jPanelDndRefsetAddTo = tmp.getPanel();

        // Setup Add Value Panel
        tmp = new ValueDndNidUI("Add Concept Value:");
        layout.replace(jPanelValueNew, tmp.getPanel());
        jPanelValueNew = tmp.getPanel();

        currentValueTypeIdx = 1; // concept

    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanelDndRefsetAddTo = new javax.swing.JPanel();
        jComboBoxType = new javax.swing.JComboBox();
        jPanelValueNew = new javax.swing.JPanel();

        setPreferredSize(new java.awt.Dimension(218, 125));

        jPanelDndRefsetAddTo.setPreferredSize(new java.awt.Dimension(218, 36));

        javax.swing.GroupLayout jPanelDndRefsetAddToLayout = new javax.swing.GroupLayout(jPanelDndRefsetAddTo);
        jPanelDndRefsetAddTo.setLayout(jPanelDndRefsetAddToLayout);
        jPanelDndRefsetAddToLayout.setHorizontalGroup(
            jPanelDndRefsetAddToLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 218, Short.MAX_VALUE)
        );
        jPanelDndRefsetAddToLayout.setVerticalGroup(
            jPanelDndRefsetAddToLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 36, Short.MAX_VALUE)
        );

        jComboBoxType.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "boolean", "concept", "integer", "string" }));
        jComboBoxType.setSelectedIndex(1);
        jComboBoxType.setSelectedItem("concept");
        jComboBoxType.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxTypeActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanelValueNewLayout = new javax.swing.GroupLayout(jPanelValueNew);
        jPanelValueNew.setLayout(jPanelValueNewLayout);
        jPanelValueNewLayout.setHorizontalGroup(
            jPanelValueNewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 218, Short.MAX_VALUE)
        );
        jPanelValueNewLayout.setVerticalGroup(
            jPanelValueNewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 36, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jComboBoxType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
            .addComponent(jPanelValueNew, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanelDndRefsetAddTo, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanelDndRefsetAddTo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jComboBoxType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanelValueNew, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jComboBoxTypeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxTypeActionPerformed
        int idx = ((JComboBox) evt.getSource()).getSelectedIndex();
        if (idx != currentValueTypeIdx && idx >= 0) { // idx == -1 can occur with no menu selection change
            currentValueTypeIdx = idx;
            GroupLayout layout = (GroupLayout) this.getLayout();
            switch (idx) {
                case 0: // boolean
                    // MEMBER VALUE PANEL
                    ValueBooleanUI tmpB = new ValueBooleanUI("Add Boolean Value:");
                    layout.replace(jPanelValueNew, tmpB.getPanel());
                    jPanelValueNew = tmpB.getPanel();
                    break;
                case 1: // concept
                    // MEMBER VALUE PANEL
                    ValueDndNidUI tmpC = new ValueDndNidUI("Add Concept Value:");
                    layout.replace(jPanelValueNew, tmpC.getPanel());
                    jPanelValueNew = tmpC.getPanel();
                    break;
                case 2: // integer
                    // MEMBER VALUE PANEL
                    ValueIntUI tmpI = new ValueIntUI("Add Integer Value:");
                    layout.replace(jPanelValueNew, tmpI.getPanel());
                    jPanelValueNew = tmpI.getPanel();
                    break;
                case 3: // string
                    // MEMBER VALUE PANEL
                    ValueStringUI tmpS = new ValueStringUI("Add String Value:");
                    layout.replace(jPanelValueNew, tmpS.getPanel());
                    jPanelValueNew = tmpS.getPanel();
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
    private javax.swing.JComboBox jComboBoxType;
    private javax.swing.JPanel jPanelDndRefsetAddTo;
    private javax.swing.JPanel jPanelValueNew;
    // End of variables declaration//GEN-END:variables

    @Override // I_BatchActionTask
    public JPanel getPanel() {
        return this;
    }

    @Override // I_BatchActionTask
    public void updateExisting(List<RelationshipVersionBI> existingParents, List<ComponentVersionBI> existingRefsets, List<ComponentVersionBI> existingDescriptionRefsets, List<RelationshipVersionBI> existingRoles, List<ComponentVersionBI> parentLinkages) {
        // nothing to do
    }

    @Override // I_BatchActionTask
    public BatchActionTask getTask(EditCoordinate ec, ViewCoordinate vc, List<ConceptChronicleBI> concepts) {
        // referenced component provided at execution time

        // SET REFSET DND COLLECTION NID
        I_AmTermComponent refsetAddToCB = ((ValueDndNidUI) jPanelDndRefsetAddTo).getTermComponent();
        if (refsetAddToCB != null) {
            ((BatchActionTaskConceptRefsetAddMember) task).setCollectionNid(refsetAddToCB.getConceptNid());
        } else {
            BatchActionEventReporter.add(new BatchActionEvent(null, BatchActionTaskType.CONCEPT_REFSET_ADD_MEMBER,
                    BatchActionEventType.TASK_INVALID, "missing refset to add to"));
            return null;
        }

        // SET VALUE TYPE
        switch (currentValueTypeIdx) {
            case 0:
                ((BatchActionTaskConceptRefsetAddMember) task).setRefsetType(TK_REFEX_TYPE.BOOLEAN);
                Boolean b = ((ValueBooleanUI) jPanelValueNew).getValue();
                if (b != null) {
                    ((BatchActionTaskConceptRefsetAddMember) task).setRefsetValue(b);
                    return task;
                }
                break;
            case 1:
                ((BatchActionTaskConceptRefsetAddMember) task).setRefsetType(TK_REFEX_TYPE.CID);
                Integer cNid = ((ValueDndNidUI) jPanelValueNew).getValue();
                if (cNid != null) {
                    ((BatchActionTaskConceptRefsetAddMember) task).setRefsetValue(cNid);
                    return task;
                }
                break;
            case 2:
                ((BatchActionTaskConceptRefsetAddMember) task).setRefsetType(TK_REFEX_TYPE.INT);
                Integer i = ((ValueIntUI) jPanelValueNew).getValue();
                if (i != null) {
                    ((BatchActionTaskConceptRefsetAddMember) task).setRefsetValue(i);
                    return task;
                }
                break;
            case 3:
                ((BatchActionTaskConceptRefsetAddMember) task).setRefsetType(TK_REFEX_TYPE.STR);
                String s = ((ValueStringUI) jPanelValueNew).getValue();
                if (s != null) {
                    ((BatchActionTaskConceptRefsetAddMember) task).setRefsetValue(s);
                    return task;
                }
                break;
            default:
                throw new AssertionError();
        }



        BatchActionEventReporter.add(new BatchActionEvent(null, BatchActionTaskType.CONCEPT_REFSET_ADD_MEMBER,
                BatchActionEventType.TASK_INVALID, "match value not set"));
        return null;
    }
}
