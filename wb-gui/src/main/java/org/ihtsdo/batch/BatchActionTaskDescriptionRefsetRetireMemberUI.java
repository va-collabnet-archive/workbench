/**
 * Copyright (c) 2009 International Health Terminology Standards Development
 * Organisation
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
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
public class BatchActionTaskDescriptionRefsetRetireMemberUI extends javax.swing.JPanel implements I_BatchActionTask {

    BatchActionTask task;
    boolean useFilter;

    /**
     * Creates new form BatchActionTaskDescriptionRefsetRetireMemberUI
     */
    public BatchActionTaskDescriptionRefsetRetireMemberUI() {
        initComponents();

        // TASK
        this.task = new BatchActionTaskDescriptionRefsetRetireMember();

        // Setup Filter Value Panel
        GroupLayout layout = (GroupLayout)jPanelCriteria.getLayout();
        ValueDndNidUI tmp = new ValueDndNidUI("Member Match Value:");
        layout.replace(jPanelValueMatch, tmp.getPanel());
        jPanelValueMatch = tmp.getPanel();

        useFilter = false;
        jCheckBoxMatch.setSelected(useFilter);
        jPanelValueMatch.setEnabled(useFilter);
        jPanelValueMatch.setVisible(useFilter);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanelCriteria = new javax.swing.JPanel();
        jComboBoxSearchByConstraint = new javax.swing.JComboBox();
        jCheckBoxSearchIsCaseSensitive = new javax.swing.JCheckBox();
        jLabel3 = new javax.swing.JLabel();
        jComboBoxSearchByType = new javax.swing.JComboBox();
        jLabel4 = new javax.swing.JLabel();
        jComboBoxSearchByLanguage = new javax.swing.JComboBox();
        jCheckBoxMatch = new javax.swing.JCheckBox();
        jTextFieldSearchText = new javax.swing.JTextField();
        jPanelValueMatch = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jComboBoxExistingRefsets = new javax.swing.JComboBox();

        setPreferredSize(new java.awt.Dimension(218, 120));

        jPanelCriteria.setBorder(javax.swing.BorderFactory.createTitledBorder("Criteria"));

        jComboBoxSearchByConstraint.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Text Does Not Apply", "Contains", "Begins with", "Ends with" }));
        jComboBoxSearchByConstraint.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxSearchByContraintActionPerformed(evt);
            }
        });

        jCheckBoxSearchIsCaseSensitive.setText("Case Sensitive Criteria");
        jCheckBoxSearchIsCaseSensitive.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxSearchIsCaseSensitiveActionPerformed(evt);
            }
        });

        jLabel3.setText("Type:");

        jComboBoxSearchByType.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "-------", "FSN", "Synonym", "Definition" }));
        jComboBoxSearchByType.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxSearchByTypeActionPerformed(evt);
            }
        });

        jLabel4.setText("Language");

        jComboBoxSearchByLanguage.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "--", "EN", "DA", "ES", "FR", "LIT", "LT", "NL", "PL", "SV", "ZH" }));
        jComboBoxSearchByLanguage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxSearchByLanguageActionPerformed(evt);
            }
        });

        jCheckBoxMatch.setText("Filter On Refset Member Value:");
        jCheckBoxMatch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxMatchActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanelValueMatchLayout = new javax.swing.GroupLayout(jPanelValueMatch);
        jPanelValueMatch.setLayout(jPanelValueMatchLayout);
        jPanelValueMatchLayout.setHorizontalGroup(
            jPanelValueMatchLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        jPanelValueMatchLayout.setVerticalGroup(
            jPanelValueMatchLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 31, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jPanelCriteriaLayout = new javax.swing.GroupLayout(jPanelCriteria);
        jPanelCriteria.setLayout(jPanelCriteriaLayout);
        jPanelCriteriaLayout.setHorizontalGroup(
            jPanelCriteriaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanelValueMatch, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanelCriteriaLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelCriteriaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelCriteriaLayout.createSequentialGroup()
                        .addComponent(jCheckBoxSearchIsCaseSensitive)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jComboBoxSearchByType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jComboBoxSearchByLanguage, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanelCriteriaLayout.createSequentialGroup()
                        .addComponent(jComboBoxSearchByConstraint, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextFieldSearchText))
                    .addGroup(jPanelCriteriaLayout.createSequentialGroup()
                        .addComponent(jCheckBoxMatch)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanelCriteriaLayout.setVerticalGroup(
            jPanelCriteriaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelCriteriaLayout.createSequentialGroup()
                .addGroup(jPanelCriteriaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jComboBoxSearchByConstraint, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextFieldSearchText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanelCriteriaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jCheckBoxSearchIsCaseSensitive)
                    .addComponent(jLabel3)
                    .addComponent(jComboBoxSearchByType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4)
                    .addComponent(jComboBoxSearchByLanguage, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBoxMatch)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanelValueMatch, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 18, Short.MAX_VALUE))
        );

        jLabel2.setText("Retire from Refset:");

        jComboBoxExistingRefsets.setModel(jComboBoxExistingRefsets.getModel());
        jComboBoxExistingRefsets.setRenderer(new org.ihtsdo.batch.JComboBoxExistingRefsetsRender());

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanelCriteria, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jComboBoxExistingRefsets, 0, 488, Short.MAX_VALUE)))
                .addGap(34, 34, 34))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jComboBoxExistingRefsets, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanelCriteria, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(106, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jCheckBoxMatchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxMatchActionPerformed
        useFilter = ((JCheckBox) evt.getSource()).getModel().isSelected();
        jPanelValueMatch.setEnabled(useFilter);
        jPanelValueMatch.setVisible(useFilter);
    }//GEN-LAST:event_jCheckBoxMatchActionPerformed

    private void jCheckBoxSearchIsCaseSensitiveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxSearchIsCaseSensitiveActionPerformed
        boolean checkboxState = ((JCheckBox) evt.getSource()).isSelected();
        ((BatchActionTaskDescriptionRefsetRetireMember) task).setIsSearchCaseSensitive(checkboxState);
    }//GEN-LAST:event_jCheckBoxSearchIsCaseSensitiveActionPerformed

    private void jComboBoxSearchByContraintActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxSearchByContraintActionPerformed
        int idx = ((JComboBox) evt.getSource()).getSelectedIndex();
        ((BatchActionTaskDescriptionRefsetRetireMember) task).setSearchByTextConstraint(idx);
    }//GEN-LAST:event_jComboBoxSearchByContraintActionPerformed

    private void jComboBoxSearchByTypeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxSearchByTypeActionPerformed
        int idx = ((JComboBox) evt.getSource()).getSelectedIndex();
        ((BatchActionTaskDescriptionRefsetRetireMember) task).setSearchByType(idx);
    }//GEN-LAST:event_jComboBoxSearchByTypeActionPerformed

    private void jComboBoxSearchByLanguageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxSearchByLanguageActionPerformed
        int idx = ((JComboBox) evt.getSource()).getSelectedIndex();
        ((BatchActionTaskDescriptionRefsetRetireMember) task).setSearchByLanguage(idx);
    }//GEN-LAST:event_jComboBoxSearchByLanguageActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox jCheckBoxMatch;
    private javax.swing.JCheckBox jCheckBoxSearchIsCaseSensitive;
    private javax.swing.JComboBox jComboBoxExistingRefsets;
    private javax.swing.JComboBox jComboBoxSearchByConstraint;
    private javax.swing.JComboBox jComboBoxSearchByLanguage;
    private javax.swing.JComboBox jComboBoxSearchByType;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanelCriteria;
    private javax.swing.JPanel jPanelValueMatch;
    private javax.swing.JTextField jTextFieldSearchText;
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

            @Override // Comparator
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
        
        // Description Search Text
        String valStr = jTextFieldSearchText.getText();
        if (valStr.equalsIgnoreCase("")) {
            valStr = null;
        }
        ((BatchActionTaskDescriptionRefsetRetireMember) task).setSearchText(valStr);

        
        // SET REFSET EXITING COLLECTION NID
        DefaultComboBoxModel dcbm = (DefaultComboBoxModel) jComboBoxExistingRefsets.getModel();
        ComponentVersionBI refsetBI = (ComponentVersionBI) dcbm.getSelectedItem();
        if (refsetBI != null) {
            int refsetNid = refsetBI.getNid();
            ((BatchActionTaskDescriptionRefsetRetireMember) task).setCollectionNid(refsetNid);
        } else {
            BatchActionEventReporter.add(new BatchActionEvent(null, BatchActionTaskType.DESCRIPTION_REFSET_CHANGE_VALUE,
                    BatchActionEventType.TASK_INVALID, "no selected refset"));
            return null;
        }

        // CHECK MATCH FILTER
        if (jCheckBoxMatch.isSelected() == false) {
            ((BatchActionTaskDescriptionRefsetRetireMember) task).setMatchValue(null);
            return task;
        }

        // SET MATCH VALUE
        ((BatchActionTaskDescriptionRefsetRetireMember) task).setRefsetType(TK_REFEX_TYPE.CID);
        Integer valConcept = ((ValueDndNidUI) jPanelValueMatch).getValue();
        if (valConcept != null) {
            ((BatchActionTaskDescriptionRefsetRetireMember) task).setMatchValue(valConcept);
            return task;
        }

        BatchActionEventReporter.add(new BatchActionEvent(null, BatchActionTaskType.DESCRIPTION_REFSET_CHANGE_VALUE,
                BatchActionEventType.TASK_INVALID, "match value not set"));
        return null;
    }
}
