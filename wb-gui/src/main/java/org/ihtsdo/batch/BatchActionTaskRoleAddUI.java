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
import javax.swing.JPanel;
import org.dwfa.ace.api.I_AmTermComponent;
import org.ihtsdo.batch.BatchActionEvent.BatchActionEventType;
import org.ihtsdo.batch.BatchActionTask.BatchActionTaskType;
import org.ihtsdo.tk.api.ComponentVersionBI;
import org.ihtsdo.tk.api.coordinate.EditCoordinate;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.relationship.RelationshipVersionBI;

/**
 *
 * @author marc
 */
public class BatchActionTaskRoleAddUI extends javax.swing.JPanel implements I_BatchActionTask {

    BatchActionTask task;

    /** Creates new form BatchActionTaskRoleAddUI */
    public BatchActionTaskRoleAddUI() {
        initComponents();
        this.task = new BatchActionTaskRoleAdd();

        // Setup DnD Add Role Type Panel
        ValueDndConceptUI tmp = new ValueDndConceptUI("New Role Type:");
        GroupLayout layout = (GroupLayout) this.getLayout();
        layout.replace(jPanelDndNewRoleType, tmp.getPanel());
        jPanelDndNewRoleType = tmp.getPanel();

        // Setup DnD Add Role Type Panel
        tmp = new ValueDndConceptUI("New Role Value:");
        layout.replace(jPanelDndNewRoleValue, tmp.getPanel());
        jPanelDndNewRoleValue = tmp.getPanel();
}

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanelDndNewRoleType = new javax.swing.JPanel();
        jPanelDndNewRoleValue = new javax.swing.JPanel();

        javax.swing.GroupLayout jPanelDndNewRoleTypeLayout = new javax.swing.GroupLayout(jPanelDndNewRoleType);
        jPanelDndNewRoleType.setLayout(jPanelDndNewRoleTypeLayout);
        jPanelDndNewRoleTypeLayout.setHorizontalGroup(
            jPanelDndNewRoleTypeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 420, Short.MAX_VALUE)
        );
        jPanelDndNewRoleTypeLayout.setVerticalGroup(
            jPanelDndNewRoleTypeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 41, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jPanelDndNewRoleValueLayout = new javax.swing.GroupLayout(jPanelDndNewRoleValue);
        jPanelDndNewRoleValue.setLayout(jPanelDndNewRoleValueLayout);
        jPanelDndNewRoleValueLayout.setHorizontalGroup(
            jPanelDndNewRoleValueLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 420, Short.MAX_VALUE)
        );
        jPanelDndNewRoleValueLayout.setVerticalGroup(
            jPanelDndNewRoleValueLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 45, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanelDndNewRoleType, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanelDndNewRoleValue, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanelDndNewRoleType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanelDndNewRoleValue, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jPanelDndNewRoleType;
    private javax.swing.JPanel jPanelDndNewRoleValue;
    // End of variables declaration//GEN-END:variables

    @Override
    public JPanel getPanel() {
        return this;
    }

    @Override
    public BatchActionTask getTask(EditCoordinate ec, ViewCoordinate vc) throws Exception {
        // SET ROLE TYPE
        I_AmTermComponent termRoleType = ((ValueDndConceptUI) jPanelDndNewRoleType).getTermComponent();
        if (termRoleType != null) {
            ((BatchActionTaskRoleAdd) task).setRoleNid(termRoleType.getNid());
        } else {
            BatchActionEventReporter.add(new BatchActionEvent(null, BatchActionTaskType.ROLE_ADD,
                    BatchActionEventType.TASK_INVALID, "role not set"));
            return null;
        }

        // SET ROLE VALUE
        I_AmTermComponent termRoleValue = ((ValueDndConceptUI) jPanelDndNewRoleValue).getTermComponent();
        if (termRoleValue != null) {
            ((BatchActionTaskRoleAdd) task).setValueNid(termRoleValue.getNid());
        } else {
            BatchActionEventReporter.add(new BatchActionEvent(null, BatchActionTaskType.ROLE_ADD,
                    BatchActionEventType.TASK_INVALID, "value not set"));
            return null;
        }

        return task;
    }

    @Override
    public void updateExisting(List<RelationshipVersionBI> existingParents, List<ComponentVersionBI> existingRefsets, List<ComponentVersionBI> existingRoles, List<ComponentVersionBI> parentLinkages) {
        // nothing to do
    }
}
