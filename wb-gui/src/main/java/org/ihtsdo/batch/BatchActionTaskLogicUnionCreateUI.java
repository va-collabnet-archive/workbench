/**
 * Copyright (c) 2009 International Health Terminology Standards Development Organisation
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.ihtsdo.batch;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import javax.swing.GroupLayout;
import javax.swing.JPanel;
import org.dwfa.ace.api.*;
import org.dwfa.ace.config.AceFrameConfig;
import org.dwfa.ace.log.AceLog;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.batch.BatchActionEvent.BatchActionEventType;
import org.ihtsdo.batch.BatchActionTask.BatchActionTaskType;
import org.ihtsdo.helper.descriptionlogic.DescriptionLogic;
import org.ihtsdo.lang.LANG_CODE;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ComponentVersionBI;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.TerminologyBuilderBI;
import org.ihtsdo.tk.api.blueprint.*;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.coordinate.EditCoordinate;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.api.relationship.RelationshipVersionBI;
import org.ihtsdo.tk.binding.snomed.Snomed;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRfx;
import org.ihtsdo.tk.binding.snomed.TermAux;
import org.ihtsdo.tk.dto.concept.component.refset.TK_REFSET_TYPE;
import org.ihtsdo.tk.dto.concept.component.relationship.TkRelType;

/**
 *
 * @author marc
 */
public class BatchActionTaskLogicUnionCreateUI extends javax.swing.JPanel
        implements I_BatchActionTask {

    BatchActionTask task;
    private AceFrameConfig config;
    private int nidUnionSetRefex;

    /**
     * Creates new form BatchActionTaskLogicUnionCreateUI
     */
    BatchActionTaskLogicUnionCreateUI(AceFrameConfig aceFrameConfig) {
        initComponents();
        config = aceFrameConfig;

        // TASK
        this.task = new BatchActionTaskLogicUnionCreate();

        // Setup DnD Panel
        ValueDndNidUI tmp = new ValueDndNidUI("Parent:");
        GroupLayout layout = (GroupLayout) this.getLayout();
        layout.replace(jPanelDndNewParent, tmp.getPanel());
        jPanelDndNewParent = tmp.getPanel();
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT
     * modify this code. The content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanelDndNewParent = new javax.swing.JPanel();
        jTextFieldDescription = new javax.swing.JTextField();

        setPreferredSize(new java.awt.Dimension(218, 125));

        jPanelDndNewParent.setBorder(javax.swing.BorderFactory.createTitledBorder("Parent:"));

        javax.swing.GroupLayout jPanelDndNewParentLayout = new javax.swing.GroupLayout(jPanelDndNewParent);
        jPanelDndNewParent.setLayout(jPanelDndNewParentLayout);
        jPanelDndNewParentLayout.setHorizontalGroup(
            jPanelDndNewParentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 264, Short.MAX_VALUE)
        );
        jPanelDndNewParentLayout.setVerticalGroup(
            jPanelDndNewParentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 24, Short.MAX_VALUE)
        );

        jTextFieldDescription.setBorder(javax.swing.BorderFactory.createTitledBorder("Description:"));
        jTextFieldDescription.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldDescriptionActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanelDndNewParent, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jTextFieldDescription, javax.swing.GroupLayout.DEFAULT_SIZE, 206, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanelDndNewParent, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextFieldDescription, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(17, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jTextFieldDescriptionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldDescriptionActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextFieldDescriptionActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jPanelDndNewParent;
    private javax.swing.JTextField jTextFieldDescription;
    // End of variables declaration//GEN-END:variables

    @Override // I_BatchActionTask
    public JPanel getPanel() {
        return this;
    }

    @Override // I_BatchActionTask
    public void updateExisting(List<RelationshipVersionBI> existingParents,
            List<ComponentVersionBI> existingRefsets,
            List<RelationshipVersionBI> existingRoles,
            List<ComponentVersionBI> parentLinkages) {
        // nothing to do
    }

    @Override // I_BatchActionTask
    public BatchActionTask getTask(EditCoordinate ec, ViewCoordinate vc,
            List<ConceptChronicleBI> concepts) {
        // referenced component provided at execution time

        if (concepts.size() <= 0) {
            BatchActionEventReporter.add(
                    new BatchActionEvent(null, BatchActionTaskType.LOGIC_UNION_SET_CREATE,
                    BatchActionEventType.TASK_INVALID, "empty concepts list"));
            return null;
        }

        // Get UI values
        I_TermFactory tf = Terms.get();
        I_AmTermComponent termNewParentDest =
                ((ValueDndNidUI) jPanelDndNewParent).getTermComponent();
        I_GetConceptData parent;
        try {
            if (termNewParentDest != null) {
                parent = tf.getConcept(termNewParentDest.getNid());
            } else {
                BatchActionEventReporter.add(new BatchActionEvent(null,
                        BatchActionTaskType.LOGIC_UNION_SET_CREATE,
                        BatchActionEventType.TASK_INVALID, "parent concept not set"));
                return null;
            }
        } catch (TerminologyException ex) {
            BatchActionEventReporter.add(new BatchActionEvent(null,
                    BatchActionTaskType.LOGIC_UNION_SET_CREATE,
                    BatchActionEventType.TASK_INVALID, "parent concept error"));
            AceLog.getAppLog().alertAndLogException(ex);
            return null;
        } catch (IOException ex) {
            BatchActionEventReporter.add(new BatchActionEvent(null,
                    BatchActionTaskType.LOGIC_UNION_SET_CREATE,
                    BatchActionEventType.TASK_INVALID, "parent concept *error*"));
            AceLog.getAppLog().alertAndLogException(ex);
            return null;
        }

        // Setup Union set refset
        try {

            UUID refsetUuid = DescriptionLogic.computeOrderedSetUuid(concepts,
                    "org.ihtsdo.descriptionlogic.unionsetconcepts:");
            UUID refsetRelUuid = DescriptionLogic.computeOrderedSetUuid(concepts,
                    "org.ihtsdo.descriptionlogic.unionset.parentrel");
            nidUnionSetRefex = createOrUpdateRefexSet(refsetUuid, refsetRelUuid, concepts, vc);
        } catch (Exception ex) {
            AceLog.getAppLog().alertAndLogException(ex);
            BatchActionEventReporter.add(
                    new BatchActionEvent(null,
                    BatchActionTaskType.LOGIC_UNION_SET_CREATE,
                    BatchActionEventType.TASK_INVALID,
                    "error occurred when creating union refex"));
            return null;
        }
        ((BatchActionTaskLogicUnionCreate) task).setUnionSetRefexNid(nidUnionSetRefex);

        // Setup Union set concept in SNOMED taxonomy.
        // The SNOMED concept is a convenience when searching for the Union set
        try {
            UUID conceptUuid = DescriptionLogic.computeOrderedSetUuid(concepts,
                    "org.ihtsdo.descriptionlogic.unionset.concept");
            createOrUpdateConceptSet(conceptUuid, parent, vc);

        } catch (Exception ex) {
            AceLog.getAppLog().alertAndLogException(ex);
            BatchActionEventReporter.add(
                    new BatchActionEvent(null,
                    BatchActionTaskType.LOGIC_UNION_SET_CREATE,
                    BatchActionEventType.TASK_INVALID,
                    "error occurred when creating union concept"));
            return null;
        }

        return task;
    }

    private int createOrUpdateRefexSet(UUID componentUuid, UUID parentRelUuid,
            List<ConceptChronicleBI> concepts, ViewCoordinate vc)
            throws IOException, InvalidCAB, ContradictionException,
            TerminologyException, PropertyVetoException {
        int nid;
        // TEST FOR EXISTING REFSET
        // SCENARIO: EXISTS
        if (BatchActionTask.ts.hasUuid(componentUuid) == true) {
            ConceptVersionBI refsetCB = BatchActionTask.ts.getConceptVersion(vc, componentUuid);
            // :SNOOWL: review added null, null)
            ConAttrAB cCab = new ConAttrAB(componentUuid, false, null, null);
            cCab.setCurrent();
            BatchActionTask.tsSnapshot.construct(cCab);

            // SET RELATIONSHIP TO CURRENT
            UUID roleTypeUuid = TermAux.IS_A.getLenient().getPrimUuid();
            UUID destUuid = DescriptionLogic.UNION_SETS_REFSET.getLenient().getPrimUuid();
            RelCAB rCab = new RelCAB(refsetCB.getPrimUuid(), roleTypeUuid, destUuid, 0,
                    TkRelType.STATED_HIERARCHY);

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
            desc.append("Union: ");
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
            I_GetConceptData parent = tf.getConcept(
                    DescriptionLogic.UNION_SETS_REFSET.getLenient().getNid());
            I_RelVersioned rel = tf.newRelationship(parentRelUuid, newConcept, config);
            rel.setTypeNid(TermAux.IS_A.getLenient().getNid());
            rel.setC2Id(parent.getConceptNid());

            tf.addUncommitted(newConcept);

            nid = newConcept.getNid();
        }
        return nid;
    }

    // Create or update union concept
    private int createOrUpdateConceptSet(UUID unionSetConceptUuid, I_GetConceptData parent,
            ViewCoordinate vc)
            throws IOException, InvalidCAB, ContradictionException, TerminologyException {
        int nid;
        // TEST FOR EXISTING UNION_SETS_REFSET SET
        // SCENARIO: EXISTS ... then make current
        if (BatchActionTask.ts.hasUuid(unionSetConceptUuid) == true) {
            ConceptVersionBI unionSetCB;
            unionSetCB = BatchActionTask.ts.getConceptVersion(vc, unionSetConceptUuid);
            // :SNOOWL: review added null, null)
            ConAttrAB cCab = new ConAttrAB(unionSetConceptUuid, false, null, null);
            cCab.setCurrent();
            BatchActionTask.tsSnapshot.construct(cCab);

            // SET PARENT RELATIONSHIP TO CURRENT
            UUID roleTypeUuid = Snomed.IS_A.getLenient().getPrimUuid();
            UUID destUuid;
            destUuid = parent.getPrimUuid();
            RelCAB rCab = new RelCAB(unionSetConceptUuid, roleTypeUuid, destUuid, 0,
                    TkRelType.STATED_HIERARCHY);
            rCab.setCurrent();
            BatchActionTask.tsSnapshot.construct(rCab);

            // SET UNION_SETS_REFSET SET RELATIONSHIP TO CURRENT
            destUuid = DescriptionLogic.UNION_SETS_REFSET.getUuids()[0];
            RelCAB rCab2 = new RelCAB(unionSetConceptUuid, roleTypeUuid, destUuid, 0,
                    TkRelType.STATED_HIERARCHY);
            rCab2.setCurrent();
            BatchActionTask.tsSnapshot.construct(rCab2);


            Ts.get().addUncommitted(unionSetCB);

            // Update reflex membership as the "union concept"

            nid = unionSetCB.getNid();

        } else {
            // SCENARIO: CREATE UNION_SETS_REFSET SET CONCEPT
            // PREFERRED TERM DESCRIPTION
            String ptStr = jTextFieldDescription.getText();

            // FULLY SPECIFIED NAME DESCRIPTION
            String descAttribute = "";
            for (I_DescriptionVersioned d : parent.getDescriptions()) {
                if (d.getTypeNid() == SnomedMetadataRfx.getDES_FULL_SPECIFIED_NAME_NID()
                        && d.getLang().equalsIgnoreCase("en")) {
                    int a = d.getText().lastIndexOf("(");
                    int b = d.getText().lastIndexOf(")");
                    if (a < d.getText().length() && b < d.getText().length()) {
                        descAttribute = " " + d.getText().substring(a, b + 1);
                    }
                }
            }
            String fsnStr = ptStr + descAttribute;

            // PARENTS
            UUID[] parentUuids = new UUID[1];
            parentUuids[0] = Terms.get().nidToUuid(parent.getConceptNid());
            // parentUuids[1] = DescriptionLogic.UNION_SETS_REFSET.getUuids()[0];

            // CREATE CONCEPT
            UUID isa = Snomed.IS_A.getLenient().getPrimUuid();
            ConceptCB newUnionSetCB = new ConceptCB(fsnStr, ptStr, LANG_CODE.EN, isa,
                    parentUuids);
            newUnionSetCB.setComponentUuid(unionSetConceptUuid);
            TerminologyBuilderBI tc = Ts.get().getTerminologyBuilder(config.getEditCoordinate(),
                    config.getViewCoordinate());
            ConceptChronicleBI newUnionSetConcept = tc.constructIfNotCurrent(newUnionSetCB);

            // REFEX
            ConceptVersionBI cv = Ts.get().getConceptVersion(
                    config.getViewCoordinate(), newUnionSetConcept.getConceptNid());
            createBlueprintRefex(cv);


            Ts.get().addUncommitted(newUnionSetConcept);
            nid = newUnionSetConcept.getNid();
        }

        // :???:!!!:SNOOWL:
        // If not already a refex member, then a member record is added.
        // Check if member already exists
//        Collection<? extends RefexVersionBI<?>> currentRefexes = c.getCurrentRefexes(vc);
//        for (RefexVersionBI rvbi : currentRefexes) {
//            if (rvbi.getCollectionNid() == collectionNid) {
//                BatchActionEventReporter.add(new BatchActionEvent(c, BatchActionTaskType.REFSET_ADD_MEMBER,
//                        BatchActionEventType.EVENT_NOOP, "already member of: " + nidToName(collectionNid)));
//                return false;
//            }
//        }
        RefexCAB refexSpec = new RefexCAB(TK_REFSET_TYPE.CID, nid, nidUnionSetRefex);
        int parentMemberTypeNid = Terms.get().getConcept(
                RefsetAuxiliary.Concept.MARKED_PARENT.getPrimoridalUid()).getConceptNid();
        refexSpec.with(RefexCAB.RefexProperty.CNID1, parentMemberTypeNid);
        // refexSpec.setMemberContentUuid(); :???:!!!:SNOOWL:
        BatchActionTask.tsSnapshot.constructIfNotCurrent(refexSpec);

        return nid;
    }

    // Handles US & GB Language Refsets
    private void createBlueprintRefex(ConceptVersionBI cvbi) throws ContradictionException {
        try {
            TerminologyBuilderBI tc = Ts.get().getTerminologyBuilder(
                    config.getEditCoordinate(), config.getViewCoordinate());

            ConceptChronicleBI gbRefexConcept =
                    Ts.get().getConcept(SnomedMetadataRfx.getGB_DIALECT_REFEX_NID());
            UUID gbUuid = gbRefexConcept.getPrimUuid();
            ConceptChronicleBI usRefexConcept =
                    Ts.get().getConcept(SnomedMetadataRfx.getUS_DIALECT_REFEX_NID());

            UUID usUuid = usRefexConcept.getPrimUuid();
            ConceptChronicleBI acceptableConcept =
                    Ts.get().getConcept(SnomedMetadataRfx.getDESC_ACCEPTABLE_NID());
            ConceptChronicleBI preferredConcept =
                    Ts.get().getConcept(SnomedMetadataRfx.getDESC_PREFERRED_NID());
            ConceptChronicleBI fsnConcept =
                    Ts.get().getConcept(SnomedMetadataRfx.getDES_FULL_SPECIFIED_NAME_NID());
            ConceptChronicleBI synConcept =
                    Ts.get().getConcept(SnomedMetadataRfx.getDES_SYNONYM_NID());

            ComponentVersionBI fsn = cvbi.getFullySpecifiedDescription();
            ComponentVersionBI pref = cvbi.getPreferredDescription();

            // createBluePrintUsFsnRefex
            RefexCAB refexSpecUsFsn = new RefexCAB(
                    TK_REFSET_TYPE.CID,
                    fsn.getNid(),
                    Ts.get().getNidForUuids(usUuid));
            refexSpecUsFsn.put(RefexCAB.RefexProperty.CNID1, preferredConcept.getNid());
            RefexChronicleBI<?> annot = tc.construct(refexSpecUsFsn);

            if (!usRefexConcept.isAnnotationStyleRefex()) {
                Ts.get().addUncommitted(usRefexConcept);
            }
            // createBluePrintUsPrefRefex
            RefexCAB refexSpecUsPref = new RefexCAB(
                    TK_REFSET_TYPE.CID,
                    pref.getNid(),
                    Ts.get().getNidForUuids(usUuid));

            refexSpecUsPref.put(RefexCAB.RefexProperty.CNID1, preferredConcept.getNid());
            annot = tc.construct(refexSpecUsPref);
            if (!usRefexConcept.isAnnotationStyleRefex()) {
                Ts.get().addUncommitted(usRefexConcept);
            }
            // createBluePrintGbFsnRefex
            RefexCAB refexSpecGbFsn = new RefexCAB(
                    TK_REFSET_TYPE.CID,
                    fsn.getNid(),
                    Ts.get().getNidForUuids(gbUuid));
            refexSpecGbFsn.put(RefexCAB.RefexProperty.CNID1, preferredConcept.getNid());
            annot = tc.construct(refexSpecGbFsn);
            if (!gbRefexConcept.isAnnotationStyleRefex()) {
                Ts.get().addUncommitted(gbRefexConcept);
            }
            // createBluePrintGbPrefRefex
            RefexCAB refexSpecGbPref = new RefexCAB(
                    TK_REFSET_TYPE.CID,
                    pref.getNid(),
                    Ts.get().getNidForUuids(gbUuid));
            refexSpecGbPref.put(RefexCAB.RefexProperty.CNID1, preferredConcept.getNid());
            annot = tc.construct(refexSpecGbPref);
            if (!gbRefexConcept.isAnnotationStyleRefex()) {
                Ts.get().addUncommitted(gbRefexConcept);
            }

        } catch (IOException ex) {
            AceLog.getAppLog().alertAndLogException(ex);
        } catch (InvalidCAB ex) {
            AceLog.getAppLog().alertAndLogException(ex);
        }
    }
}