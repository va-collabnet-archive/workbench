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

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.config.AceFrameConfig;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.id.Type5UuidFactory;
import org.ihtsdo.batch.BatchActionEvent.BatchActionEventType;
import org.ihtsdo.batch.BatchActionTask.BatchActionTaskType;
import org.ihtsdo.descriptionlogic.DescriptionLogic;
import org.ihtsdo.tk.api.ComponentVersionBI;
import org.ihtsdo.tk.api.ContraditionException;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.coordinate.EditCoordinate;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.relationship.RelationshipVersionBI;
import org.ihtsdo.tk.binding.snomed.TermAux;

/**
 *
 * @author marc
 */
public class BatchActionTaskLogicDisjoinAddUI extends javax.swing.JPanel implements I_BatchActionTask {

    BatchActionTask task;
    private AceFrameConfig config;

    /** Creates new form BatchActionTaskRefsetAddMemberUI */
    public BatchActionTaskLogicDisjoinAddUI() {
        initComponents();

        // TASK
        this.task = new BatchActionTaskLogicDisjoinAdd();
    }

    BatchActionTaskLogicDisjoinAddUI(AceFrameConfig aceFrameConfig) {
        initComponents();
        config = aceFrameConfig;

        // TASK
        this.task = new BatchActionTaskLogicDisjoinAdd();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
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
            .addGap(0, 22, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
    @Override // I_BatchActionTask
    public JPanel getPanel() {
        return this;
    }

    @Override // I_BatchActionTask
    public void updateExisting(List<RelationshipVersionBI> existingParents, List<ComponentVersionBI> existingRefsets, List<RelationshipVersionBI> existingRoles, List<ComponentVersionBI> parentLinkages) {
        // nothing to do
    }

    @Override // I_BatchActionTask
    public BatchActionTask getTask(EditCoordinate ec, ViewCoordinate vc, List<ConceptChronicleBI> concepts) {
        // referenced component provided at execution time

        if (concepts.size() <= 0) {
            BatchActionEventReporter.add(
                    new BatchActionEvent(null, BatchActionTaskType.LOGIC_DISJOIN_CONCEPTS,
                    BatchActionEventType.TASK_INVALID, "empty concepts list"));
            return null;
        }

        // UUID SORT ORDER -- order required for deterministic refset UUID
        Comparator<ConceptChronicleBI> comp = new Comparator<ConceptChronicleBI>() {

            @Override
            public int compare(ConceptChronicleBI o1, ConceptChronicleBI o2) {
                return o1.getPrimUuid().compareTo(o2.getPrimUuid());
            }
        };
        Collections.sort(concepts, comp);

        // COMPUTE REFSET UUID
        StringBuilder conceptsUuidStr = new StringBuilder(concepts.size() * (36 + 1));
        conceptsUuidStr.append("org.ihtsdo.descriptionlogic.disjointconcepts:");
        for (ConceptChronicleBI ccbi : concepts) {
            conceptsUuidStr.append(ccbi.getPrimUuid().toString()).append("|");
        }
        UUID refsetUuid = null;
        try {
            refsetUuid = Type5UuidFactory.get(conceptsUuidStr.toString());
        } catch (Exception ex) {
            Logger.getLogger(BatchActionTaskLogicDisjoinAddUI.class.getName()).
                    log(Level.SEVERE, "error generating uuid", ex);
            BatchActionEventReporter.add(
                    new BatchActionEvent(null, BatchActionTaskType.LOGIC_DISJOIN_CONCEPTS,
                    BatchActionEventType.TASK_INVALID, "error generating uuid"));
            return null;
        }

        // TEST FOR EXISTING REFSET
        // SCENARIO: EXISTS
        if (BatchActionTask.ts.hasUuid(refsetUuid) == true) {
            // refsetCB = BatchActionTask.ts.getConcept(refsetUuid);
            // :!!!:NYI: additional check and validations of existing set could be done here
            // :!!!: UNRETIRE, RETIRED REFSET
            BatchActionEventReporter.add(
                    new BatchActionEvent(null, BatchActionTaskType.LOGIC_DISJOIN_CONCEPTS,
                    BatchActionEventType.TASK_INVALID, "disjoint set already exists"));
            return null;
        }

        // SCENARIO: CREATE REFSET CONCEPT
        try {
            I_TermFactory tf = Terms.get();
            // CREATE NEW CONCEPT
            I_GetConceptData newConcept = tf.newConcept(refsetUuid, false, config);

            // ADD FULLY SPECIFIED NAME DESCRIPTION
            StringBuilder desc = new StringBuilder();
            desc.append("Disjoint: ");
            try {
                for (ConceptChronicleBI ccbi : concepts) {
                    desc.append(ccbi.getVersion(vc).toUserString()).append("; ");
                }
            } catch (ContraditionException ex) {
                Logger.getLogger(BatchActionTaskLogicDisjoinAddUI.class.getName()).log(Level.SEVERE, null, ex);
                
            }
            I_GetConceptData fully_specified_description_type =
                    tf.getConcept(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids());
            tf.newDescription(UUID.randomUUID(), newConcept, "en", desc.toString(),
                    fully_specified_description_type, config);

            // ADD PREFERRED TERM DESCRIPTION
            I_GetConceptData preferred_description_type =
                    tf.getConcept(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.getUids());
            tf.newDescription(UUID.randomUUID(), newConcept, "en", desc.toString(),
                    preferred_description_type, config);

            // ADD RELATIONSHIP
            I_GetConceptData parent = tf.getConcept(DescriptionLogic.DISJUNTION.getLenient().getNid());
            I_RelVersioned rel = tf.newRelationship(UUID.randomUUID(), newConcept, config);
            rel.setTypeNid(TermAux.IS_A.getLenient().getNid());
            rel.setC2Id(parent.getConceptNid());

            ((BatchActionTaskLogicDisjoinAdd) task).setCollectionNid(newConcept.getNid());

            tf.addUncommitted(newConcept);

        } catch (PropertyVetoException ex) {
            Logger.getLogger(BatchActionTaskLogicDisjoinAddUI.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(BatchActionTaskLogicDisjoinAddUI.class.getName()).log(Level.SEVERE, null, ex);
        } catch (TerminologyException ex) {
            Logger.getLogger(BatchActionTaskLogicDisjoinAddUI.class.getName()).log(Level.SEVERE, null, ex);
        }

        return task;
    }
}