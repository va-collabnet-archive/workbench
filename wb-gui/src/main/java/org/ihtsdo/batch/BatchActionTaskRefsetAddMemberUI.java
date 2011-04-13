/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * BatchActionTaskRefsetAddMemberUI.java
 *
 * Created on Apr 8, 2011, 10:04:35 PM
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
public class BatchActionTaskRefsetAddMemberUI extends javax.swing.JPanel implements I_BatchActionTask {
    BatchActionTask task;

    /** Creates new form BatchActionTaskRefsetAddMemberUI */
    public BatchActionTaskRefsetAddMemberUI() {
        initComponents();
        this.task = new BatchActionTaskRefsetAddMember();

        // Setup DnD Panel
        BatchActionTaskDndConcept tmp = new BatchActionTaskDndConcept("New Value:");
        GroupLayout layout = (GroupLayout) this.getLayout();
        layout.replace(jPanelDndNewValue, tmp.getPanel());
        jPanelDndNewValue = tmp.getPanel();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jComboBoxExistingRefsets = new javax.swing.JComboBox();
        jPanelDndNewValue = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();

        setPreferredSize(new java.awt.Dimension(218, 67));

        jComboBoxExistingRefsets.setModel(jComboBoxExistingRefsets.getModel());
        jComboBoxExistingRefsets.setRenderer(new org.ihtsdo.batch.JComboBoxExistingRefsetsRender());

        jPanelDndNewValue.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createTitledBorder(""), "New Value:"));

        javax.swing.GroupLayout jPanelDndNewValueLayout = new javax.swing.GroupLayout(jPanelDndNewValue);
        jPanelDndNewValue.setLayout(jPanelDndNewValueLayout);
        jPanelDndNewValueLayout.setHorizontalGroup(
            jPanelDndNewValueLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 198, Short.MAX_VALUE)
        );
        jPanelDndNewValueLayout.setVerticalGroup(
            jPanelDndNewValueLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 8, Short.MAX_VALUE)
        );

        jLabel1.setText("To Refset:");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jComboBoxExistingRefsets, 0, 150, Short.MAX_VALUE))
            .addComponent(jPanelDndNewValue, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jComboBoxExistingRefsets, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanelDndNewValue, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox jComboBoxExistingRefsets;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanelDndNewValue;
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
        DefaultComboBoxModel dcbm = (DefaultComboBoxModel) jComboBoxExistingRefsets.getModel();
        ComponentVersionBI selectedItem = (ComponentVersionBI) dcbm.getSelectedItem();

        // Sort existing parents by name.
        Comparator<ComponentVersionBI> cmp = new Comparator<ComponentVersionBI>() {

            @Override
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

}
