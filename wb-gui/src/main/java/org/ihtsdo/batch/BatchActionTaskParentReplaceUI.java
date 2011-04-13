/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * BatchActionTaskParentReplaceUI.java
 *
 * Created on Mar 31, 2011, 7:32:05 AM
 */
package org.ihtsdo.batch;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JPanel;
import org.ihtsdo.tk.api.ComponentVersionBI;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;

/**
 *
 * @author marc
 */
public class BatchActionTaskParentReplaceUI extends javax.swing.JPanel implements I_BatchActionTask {

    BatchActionTask task;

    /** Creates new form BatchActionTaskParentReplaceUI */
    public BatchActionTaskParentReplaceUI() {
        initComponents();
        this.task = new BatchActionTaskParentReplace();

        // Setup DnD Panel
        BatchActionTaskDndConcept tmp = new BatchActionTaskDndConcept("With Parent:");
        GroupLayout layout = (GroupLayout) this.getLayout();
        layout.replace(jPanelDndParentReplace, tmp.getPanel());
        jPanelDndParentReplace = tmp.getPanel();
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
        jPanelDndParentReplace = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();

        jComboBoxExistingParents.setModel(jComboBoxExistingParents.getModel());
        jComboBoxExistingParents.setRenderer(new org.ihtsdo.batch.JComboBoxExistingParentsRender());

        jPanelDndParentReplace.setBorder(javax.swing.BorderFactory.createTitledBorder("With Parent:"));

        javax.swing.GroupLayout jPanelDndParentReplaceLayout = new javax.swing.GroupLayout(jPanelDndParentReplace);
        jPanelDndParentReplace.setLayout(jPanelDndParentReplaceLayout);
        jPanelDndParentReplaceLayout.setHorizontalGroup(
            jPanelDndParentReplaceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 342, Short.MAX_VALUE)
        );
        jPanelDndParentReplaceLayout.setVerticalGroup(
            jPanelDndParentReplaceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 39, Short.MAX_VALUE)
        );

        jLabel1.setText("Replace:");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jComboBoxExistingParents, 0, 277, Short.MAX_VALUE))
            .addComponent(jPanelDndParentReplace, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jComboBoxExistingParents, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanelDndParentReplace, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox jComboBoxExistingParents;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanelDndParentReplace;
    // End of variables declaration//GEN-END:variables

    @Override
    public void doTaskExecution(ConceptVersionBI c) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public JPanel getPanel() {
        return this;
    }

    @Override
    public void updateExisting(List<ComponentVersionBI> existingParents, List<ComponentVersionBI> existingRefsets, List<ComponentVersionBI> existingRoles) {
        DefaultComboBoxModel dcbm = (DefaultComboBoxModel) jComboBoxExistingParents.getModel();
        ComponentVersionBI selectedItem = (ComponentVersionBI) dcbm.getSelectedItem();

        // Sort existing parents by name.
        Comparator<ComponentVersionBI> cmp = new Comparator<ComponentVersionBI>() {

            @Override
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
}
