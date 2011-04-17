/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * BatchActionTaskParentRetireUI.java
 *
 * Created on Mar 31, 2011, 7:42:21 AM
 */
package org.ihtsdo.batch;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JPanel;
import org.dwfa.cement.SNOMED;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ComponentVersionBI;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.coordinate.EditCoordinate;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;

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
    public void updateExisting(List<ComponentVersionBI> existingParents, List<ComponentVersionBI> existingRefsets, List<ComponentVersionBI> existingRoles) {
        DefaultComboBoxModel dcbm = (DefaultComboBoxModel) jComboBoxExistingParents.getModel();
        ComponentVersionBI selectedItem = (ComponentVersionBI) dcbm.getSelectedItem();

        // Sort existing parents by name.
        Comparator<ComponentVersionBI> cmp = new Comparator<ComponentVersionBI>() {

            @Override // Comparator
            public int compare(ComponentVersionBI o1, ComponentVersionBI o2) {
                return o1.toUserString().compareToIgnoreCase(o2.toUserString());
            }
        };

        // Add exitings parents to JComboBox model.
        Collections.sort(existingParents, cmp);
        dcbm.removeAllElements();
        for (ComponentVersionBI componentVersionBI : existingParents) {
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
                jComboBoxExistingParents.setSelectedIndex(selectedIdx);
            } else {
                // prior selection does not exist in new list
                dcbm.setSelectedItem(dcbm.getElementAt(0));
                jComboBoxExistingParents.setSelectedIndex(0);
            }
        }

    }

    @Override // I_BatchActionTask
    public BatchActionTask getTask(EditCoordinate ec, ViewCoordinate vc) throws IOException {
        // RETIRE PARENT
        DefaultComboBoxModel dcbm = (DefaultComboBoxModel) jComboBoxExistingParents.getModel();
        ComponentVersionBI fromParentBI = (ComponentVersionBI) dcbm.getSelectedItem();

        if (fromParentBI != null) {
            int nidIsa = Ts.get().getConcept(SNOMED.Concept.IS_A.getUids()).getNid();
            int nidParent = fromParentBI.getNid();
            ((BatchActionTaskParentRetire) task).setSelectedRoleTypeNid(nidIsa);
            ((BatchActionTaskParentRetire) task).setSelectedDestNid(nidParent);
            return task;
        } else {
            return null;
        }
    }
}
