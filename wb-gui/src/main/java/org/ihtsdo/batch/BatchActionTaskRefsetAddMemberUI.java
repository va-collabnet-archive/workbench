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

import javax.swing.JPanel;
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
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jComboBox1 = new javax.swing.JComboBox();

        setPreferredSize(new java.awt.Dimension(218, 67));

        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Add Member To Refset" }));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(20, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(40, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox jComboBox1;
    // End of variables declaration//GEN-END:variables

    @Override
    public void doTaskExecution(ConceptVersionBI c) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public JPanel getPanel() {
        return this;
    }

}
