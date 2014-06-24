/**
 * Copyright (c) 2009 International Health Terminology Standards Development Organisation
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */
package org.ihtsdo.batch;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import javax.swing.JPanel;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.config.AceFrameConfig;
import org.dwfa.ace.log.AceLog;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.batch.BatchActionEvent.BatchActionEventType;
import org.ihtsdo.batch.BatchActionTask.BatchActionTaskType;
import org.ihtsdo.helper.descriptionlogic.DescriptionLogic;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ComponentVersionBI;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.blueprint.ConceptAttributeAB;
import org.ihtsdo.tk.api.blueprint.InvalidCAB;
import org.ihtsdo.tk.api.blueprint.RelationshipCAB;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.coordinate.EditCoordinate;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.relationship.RelationshipVersionBI;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRfx;
import org.ihtsdo.tk.binding.snomed.TermAux;
import org.ihtsdo.tk.dto.concept.component.relationship.TkRelationshipType;

/**
 *
 * @author marc
 */
public class BatchActionTaskLogicDisjointAddUI extends javax.swing.JPanel
        implements I_BatchActionTask {

    BatchActionTask task;
    private AceFrameConfig config;

    /**
     * Creates new form BatchActionTaskRefsetAddMemberUI
     */
    public BatchActionTaskLogicDisjointAddUI() {
        initComponents();

        // TASK
        this.task = new BatchActionTaskLogicDisjointAdd();
    }

    BatchActionTaskLogicDisjointAddUI(AceFrameConfig aceFrameConfig) {
        initComponents();
        config = aceFrameConfig;

        // TASK
        this.task = new BatchActionTaskLogicDisjointAdd();
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this
     * code. The content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setPreferredSize(new java.awt.Dimension(218, 125));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 218, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 54, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
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
    public BatchActionTask getTask(EditCoordinate ec, ViewCoordinate vc,
            List<ConceptChronicleBI> concepts) {
        // referenced component provided at execution time

        if (concepts.size() <= 0) {
            BatchActionEventReporter.add(
                    new BatchActionEvent(null,
                    BatchActionTaskType.LOGIC_DISJOINT_SET_ADD,
                    BatchActionEventType.TASK_INVALID,
                    "empty concepts list"));
            return null;
        }

        int nid;
        UUID refsetUuid;
        UUID refsetRelUuid;
        try {
            refsetUuid = DescriptionLogic.computeOrderedSetUuid(concepts,
                    "org.ihtsdo.descriptionlogic.disjointsetconcepts:");
            refsetRelUuid = DescriptionLogic.computeOrderedSetUuid(concepts,
                    "org.ihtsdo.descriptionlogic.disjointset.parentrel");
            nid = createOrUpdateRefexSet(refsetUuid, refsetRelUuid, concepts, vc);
        } catch (Exception ex) {
            AceLog.getAppLog().alertAndLogException(ex);
            BatchActionEventReporter.add(
                    new BatchActionEvent(null,
                    BatchActionTaskType.LOGIC_DISJOINT_SET_ADD,
                    BatchActionEventType.TASK_INVALID,
                    "error occurred when creating disjoint set"));
            return null;
        }

        ((BatchActionTaskLogicDisjointAdd) task).setCollectionNid(nid);
        return task;
    }

    private int createOrUpdateRefexSet(UUID componentUuid, UUID parentRelUuid,
            List<ConceptChronicleBI> concepts, ViewCoordinate vc)
            throws IOException, InvalidCAB, ContradictionException, TerminologyException,
            PropertyVetoException {
        int nid;
        // TEST FOR EXISTING REFSET
        // SCENARIO: EXISTS
        if (BatchActionTask.ts.hasUuid(componentUuid) == true) {
            ConceptVersionBI refsetCB = BatchActionTask.ts.getConceptVersion(vc, componentUuid);
            // :SNOOWL: review added null, null)
            ConceptAttributeAB cCab = new ConceptAttributeAB(componentUuid, false, null, null);
            cCab.setCurrent();
            BatchActionTask.tsSnapshot.construct(cCab);

            // SET RELATIONSHIP TO CURRENT
            UUID roleTypeUuid = TermAux.IS_A.getLenient().getPrimUuid();
            UUID destUuid = DescriptionLogic.DISJOINT_SETS_REFSET.getLenient().getPrimUuid();
            RelationshipCAB rCab = new RelationshipCAB(refsetCB.getPrimUuid(), roleTypeUuid, destUuid, 0,
                    TkRelationshipType.STATED_HIERARCHY);

            BatchActionTask.tsSnapshot.construct(rCab);
            Ts.get().addUncommitted(refsetCB);

            nid = refsetCB.getNid();

        } else {

            // SCENARIO: CREATE REFSET CONCEPT
            I_TermFactory tf = Terms.get();
            // CREATE NEW CONCEPT
            I_GetConceptData newConcept = tf.newConcept(componentUuid, false, config);

            // ADD FULLY SPECIFIED NAME DESCRIPTION
            StringBuilder desc = new StringBuilder();
            desc.append("Disjoint: ");
            for (ConceptChronicleBI ccbi : concepts) {
                desc.append(ccbi.getVersion(vc).toUserString()).append(";");
            }
            I_GetConceptData fully_specified_description_type =
                    tf.getConcept(SnomedMetadataRfx.getDES_FULL_SPECIFIED_NAME_NID());
            tf.newDescription(UUID.randomUUID(), newConcept, "en", desc.toString(),
                    fully_specified_description_type, config);

            // ADD PREFERRED TERM DESCRIPTION
            I_GetConceptData preferred_description_type =
                    tf.getConcept(SnomedMetadataRfx.getDESC_PREFERRED_NID());
            tf.newDescription(UUID.randomUUID(), newConcept, "en", desc.toString(),
                    preferred_description_type, config);

            // ADD RELATIONSHIP
            I_GetConceptData roleType = tf.getConcept(
                    TermAux.IS_A.getLenient().getNid());
            I_GetConceptData parent = tf.getConcept(
                    DescriptionLogic.DISJOINT_SETS_REFSET.getLenient().getNid());
            I_RelVersioned rel = tf.newRelationship(parentRelUuid, // newRelUid
                    newConcept, // concept
                    roleType, // relType
                    parent, // relDestination
                    config.getDefaultRelationshipCharacteristic(), // relCharacteristic
                    config.getDefaultStatus(), // relRefinability
                    config.getDefaultStatus(), // relStatus
                    0, // relGroup
                    config);

            tf.addUncommitted(newConcept);

            nid = newConcept.getNid();
        }
        return nid;
    }
}