/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * BatchActionTaskRoleReplaceValueUI.java
 *
 * Created on Apr 8, 2011, 10:06:04 PM
 */
package org.ihtsdo.batch;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JPanel;
import org.dwfa.ace.ACE;
import org.dwfa.ace.api.I_AmTermComponent;
import org.ihtsdo.batch.BatchActionEvent.BatchActionEventType;
import org.ihtsdo.batch.BatchActionTask.BatchActionTaskType;
import org.ihtsdo.tk.api.ComponentVersionBI;
import org.ihtsdo.tk.api.coordinate.EditCoordinate;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;

/**
 *
 * @author marc
 */
public class BatchActionTaskRoleReplaceValueUI extends javax.swing.JPanel implements I_BatchActionTask {

    BatchActionTask task;

    /** Creates new form BatchActionTaskRoleReplaceValueUI */
    public BatchActionTaskRoleReplaceValueUI() {
        initComponents();
        this.task = new BatchActionTaskRoleReplaceValue();

        // Setup DnD Panel
        ValueDndConceptUI tmp = new ValueDndConceptUI("New Role Value:");
        GroupLayout layout = (GroupLayout) this.getLayout();
        layout.replace(jPanelDndRoleValueNew, tmp.getPanel());
        jPanelDndRoleValueNew = tmp.getPanel();
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
        jPanelDndRoleValueNew = new javax.swing.JPanel();
        jLabelExistingRole = new javax.swing.JLabel();

        setPreferredSize(new java.awt.Dimension(218, 67));

        jComboBoxExistingRoles.setModel(jComboBoxExistingRoles.getModel());
        jComboBoxExistingRoles.setRenderer(new org.ihtsdo.batch.JComboBoxExistingRolesRender());
        jComboBoxExistingRoles.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxExistingRolesActionPerformed(evt);
            }
        });

        jPanelDndRoleValueNew.setBorder(javax.swing.BorderFactory.createTitledBorder("New Role Value:"));

        javax.swing.GroupLayout jPanelDndRoleValueNewLayout = new javax.swing.GroupLayout(jPanelDndRoleValueNew);
        jPanelDndRoleValueNew.setLayout(jPanelDndRoleValueNewLayout);
        jPanelDndRoleValueNewLayout.setHorizontalGroup(
            jPanelDndRoleValueNewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 206, Short.MAX_VALUE)
        );
        jPanelDndRoleValueNewLayout.setVerticalGroup(
            jPanelDndRoleValueNewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 38, Short.MAX_VALUE)
        );

        jLabelExistingRole.setText("Existing Role:");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jLabelExistingRole)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jComboBoxExistingRoles, 0, 127, Short.MAX_VALUE))
            .addComponent(jPanelDndRoleValueNew, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jComboBoxExistingRoles, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelExistingRole))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanelDndRoleValueNew, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jComboBoxExistingRolesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxExistingRolesActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jComboBoxExistingRolesActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox jComboBoxExistingRoles;
    private javax.swing.JLabel jLabelExistingRole;
    private javax.swing.JPanel jPanelDndRoleValueNew;
    // End of variables declaration//GEN-END:variables

    @Override // I_BatchActionTask
    public JPanel getPanel() {
        return this;
    }

    @Override // I_BatchActionTask
    public void updateExisting(List<ComponentVersionBI> existingParents, List<ComponentVersionBI> existingRefsets, List<ComponentVersionBI> existingRoles, List<ComponentVersionBI> parentLinkages) {
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
        for (ComponentVersionBI componentVersionBI : existingRoles) {
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
                jComboBoxExistingRoles.setSelectedIndex(selectedIdx);
            } else {
                // prior selection does not exist in new list
                dcbm.setSelectedItem(dcbm.getElementAt(0));
                jComboBoxExistingRoles.setSelectedIndex(0);
            }
        }
    }

    @Override // I_BatchActionTask
    public BatchActionTask getTask(EditCoordinate ec, ViewCoordinate vc) {

        // SET ROLE TYPE

        // SET OLD ROLE VALUE

        // SET NEW ROLE VALUE
        I_AmTermComponent termRoleValue = ((ValueDndConceptUI) jPanelDndRoleValueNew).getTermComponent();
        if (termRoleValue != null) {
            ((BatchActionTaskRoleReplaceValue) task).setValueNewNid(termRoleValue.getNid());
        } else {
            BatchActionEventReporter.add(new BatchActionEvent(null, BatchActionTaskType.ROLE_REPLACE_VALUE,
                    BatchActionEventType.TASK_INVALID, "value not set"));
            return null;
        }

        return task;
    }
}
