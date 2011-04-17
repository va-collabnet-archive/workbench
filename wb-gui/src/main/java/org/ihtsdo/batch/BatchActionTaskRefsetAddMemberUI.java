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
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import org.ihtsdo.tk.api.ComponentVersionBI;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.coordinate.EditCoordinate;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;

/**
 *
 * @author marc
 */
public class BatchActionTaskRefsetAddMemberUI extends javax.swing.JPanel implements I_BatchActionTask {

    BatchActionTask task;
    boolean useFilter;
    int currentValueTypeIdx;

    /** Creates new form BatchActionTaskRefsetAddMemberUI */
    public BatchActionTaskRefsetAddMemberUI() {
        initComponents();

        // TASK
        this.task = new BatchActionTaskRefsetAddMember();

        // Setup DnD Add Value Panel
        ValueConceptDndUI tmp = new ValueConceptDndUI("To Refset:");
        GroupLayout layout = (GroupLayout) this.getLayout();
        layout.replace(jPanelDndRefsetAddTo, tmp.getPanel());
        jPanelDndRefsetAddTo = tmp.getPanel();

        // Setup Add Value Panel
        tmp = new ValueConceptDndUI("Add Concept Value:");
        layout.replace(jPanelValueNew, tmp.getPanel());
        jPanelValueNew = tmp.getPanel();

        // Setup Filter Value Panel
        tmp = new ValueConceptDndUI("Filter Concept Value:");
        layout.replace(jPanelFilter, tmp.getPanel());
        jPanelFilter = tmp.getPanel();

        currentValueTypeIdx = 1; // concept

        useFilter = false;
        jCheckBoxFilter.setSelected(useFilter);
        jPanelFilter.setEnabled(useFilter);
        jPanelFilter.setVisible(useFilter);
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
        jCheckBoxFilter = new javax.swing.JCheckBox();
        jPanelFilter = new javax.swing.JPanel();

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

        jCheckBoxFilter.setText("Filter On Value:");
        jCheckBoxFilter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxFilterActionPerformed(evt);
            }
        });

        jPanelFilter.setPreferredSize(new java.awt.Dimension(218, 36));

        javax.swing.GroupLayout jPanelFilterLayout = new javax.swing.GroupLayout(jPanelFilter);
        jPanelFilter.setLayout(jPanelFilterLayout);
        jPanelFilterLayout.setHorizontalGroup(
            jPanelFilterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 218, Short.MAX_VALUE)
        );
        jPanelFilterLayout.setVerticalGroup(
            jPanelFilterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 40, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jComboBoxType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
            .addComponent(jCheckBoxFilter, javax.swing.GroupLayout.DEFAULT_SIZE, 218, Short.MAX_VALUE)
            .addComponent(jPanelValueNew, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanelDndRefsetAddTo, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanelFilter, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanelDndRefsetAddTo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jComboBoxType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanelValueNew, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBoxFilter)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanelFilter, javax.swing.GroupLayout.DEFAULT_SIZE, 40, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jCheckBoxFilterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxFilterActionPerformed
        useFilter = ((JCheckBox) evt.getSource()).getModel().isSelected();
        jPanelFilter.setEnabled(useFilter);
        jPanelFilter.setVisible(useFilter);
    }//GEN-LAST:event_jCheckBoxFilterActionPerformed

    private void jComboBoxTypeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxTypeActionPerformed
        int idx = ((JComboBox) evt.getSource()).getSelectedIndex();
        if (idx != currentValueTypeIdx) {
            currentValueTypeIdx = idx;
            GroupLayout layout = (GroupLayout) this.getLayout();
            switch (idx) {
                case 0: // boolean
                    // MEMBER VALUE PANEL
                    ValueBooleanUI tmpB = new ValueBooleanUI("Add Boolean Value:");
                    layout.replace(jPanelValueNew, tmpB.getPanel());
                    jPanelValueNew = tmpB.getPanel();
                    // FILTER PANEL
                    tmpB = new ValueBooleanUI("Filter Boolean Value:");
                    layout.replace(jPanelFilter, tmpB.getPanel());
                    jPanelFilter = tmpB.getPanel();
                    break;
                case 1: // concept
                    // MEMBER VALUE PANEL
                    ValueConceptDndUI tmpC = new ValueConceptDndUI("Add Concept Value:");
                    layout.replace(jPanelValueNew, tmpC.getPanel());
                    jPanelValueNew = tmpC.getPanel();
                    // FILTER PANEL
                    tmpC = new ValueConceptDndUI("Filter Concept Value:");
                    layout.replace(jPanelFilter, tmpC.getPanel());
                    jPanelFilter = tmpC.getPanel();
                    break;
                case 2: // integer
                    // MEMBER VALUE PANEL
                    ValueIntegerUI tmpI = new ValueIntegerUI("Add Integer Value:");
                    layout.replace(jPanelValueNew, tmpI.getPanel());
                    jPanelValueNew = tmpI.getPanel();
                    // FILTER PANEL
                    tmpI = new ValueIntegerUI("Filter Integer Value:");
                    layout.replace(jPanelFilter, tmpI.getPanel());
                    jPanelFilter = tmpI.getPanel();
                    break;
                case 3: // string
                    // MEMBER VALUE PANEL
                    ValueStringUI tmpS = new ValueStringUI("Add String Value:");
                    layout.replace(jPanelValueNew, tmpS.getPanel());
                    jPanelValueNew = tmpS.getPanel();
                    // FILTER PANEL
                    tmpS = new ValueStringUI("Filter String Value:");
                    layout.replace(jPanelFilter, tmpS.getPanel());
                    jPanelFilter = tmpS.getPanel();
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
    private javax.swing.JCheckBox jCheckBoxFilter;
    private javax.swing.JComboBox jComboBoxType;
    private javax.swing.JPanel jPanelDndRefsetAddTo;
    private javax.swing.JPanel jPanelFilter;
    private javax.swing.JPanel jPanelValueNew;
    // End of variables declaration//GEN-END:variables

    @Override // I_BatchActionTask
    public JPanel getPanel() {
        return this;
    }

    @Override // I_BatchActionTask
    public void updateExisting(List<ComponentVersionBI> existingParents, List<ComponentVersionBI> existingRefsets, List<ComponentVersionBI> existingRoles) {
        // nothing to do
    }

    @Override // I_BatchActionTask
    public BatchActionTask getTask(EditCoordinate ec, ViewCoordinate vc) {
        return task;
    }
}
