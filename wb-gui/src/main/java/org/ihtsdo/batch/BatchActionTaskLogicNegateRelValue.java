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

import java.io.IOException;
import java.util.Collection;
import org.dwfa.cement.RefsetAuxiliary;
import org.ihtsdo.batch.BatchActionEvent.BatchActionEventType;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.blueprint.InvalidCAB;
import org.ihtsdo.tk.api.blueprint.RefexCAB;
import org.ihtsdo.tk.api.blueprint.RefexCAB.RefexProperty;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.coordinate.EditCoordinate;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.api.relationship.RelationshipVersionBI;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRfx;
import org.ihtsdo.tk.dto.concept.component.refex.TK_REFEX_TYPE;

/**
 * BatchActionTaskLogicNegateRelValue
 * 
 */
public class BatchActionTaskLogicNegateRelValue extends BatchActionTask {

    private int collectionNid; // Negation Refset NID
    private int roleNid;
    private int valueNid;
    private int roleGroup;  // -1 for all groups

    public BatchActionTaskLogicNegateRelValue() {
        this.collectionNid = Integer.MAX_VALUE;
        this.roleNid = Integer.MAX_VALUE;
        this.valueNid = Integer.MAX_VALUE;
        this.roleGroup = -1;  // -1 for all groups
    }

    public void setCollectionNid(int collectionNid) {
        this.collectionNid = collectionNid;
    }

    public void setRoleNid(int roleNid) {
        this.roleNid = roleNid;
    }

    public void setValueNid(int valueNid) {
        this.valueNid = valueNid;
    }

    public void setRoleGroup(int roleGroup) {
        this.roleGroup = roleGroup;
    }

    // BatchActionTask
    @Override
    public boolean execute(ConceptVersionBI c, EditCoordinate ec, ViewCoordinate vc)
            throws IOException, ContradictionException, InvalidCAB {
        int cNid = c.getNid(); // referenced component
        boolean changed = false;
        Collection<? extends RelationshipVersionBI> rels = c.getRelationshipsOutgoingActive();
        Collection<? extends RefexVersionBI<?>> negationRefex = null;
        for (RelationshipVersionBI rvbi : rels) {

            if (rvbi.getTypeNid() == roleNid && rvbi.getTargetNid() == valueNid
                    && (roleGroup == -1 || rvbi.getGroup() == roleGroup)
                    && rvbi.getCharacteristicNid() == SnomedMetadataRfx.getREL_CH_STATED_RELATIONSHIP_NID()) {
                // :!!!:???:

                int statusNid = SnomedMetadataRfx.getSTATUS_CURRENT_NID();
                negationRefex = rvbi.getActiveRefexes(vc, collectionNid);
                if (negationRefex.size() > 0) {
                    if (negationRefex.iterator().next().getStatusNid()
                            == SnomedMetadataRfx.getSTATUS_CURRENT_NID()) {
                        statusNid = SnomedMetadataRfx.getSTATUS_RETIRED_NID();
                    }
                }

                // If not already a member, then a member record is added.
                RefexCAB refexSpec = new RefexCAB(TK_REFEX_TYPE.CID, rvbi.getNid(), collectionNid);

                int normalMemberNid = ts.getConcept(
                        RefsetAuxiliary.Concept.NORMAL_MEMBER.getUids()).getConceptNid();
                refexSpec.with(RefexProperty.CNID1, normalMemberNid);

                refexSpec.with(RefexProperty.STATUS_NID, statusNid);
                refexSpec.setMemberContentUuid();
                tsSnapshot.constructIfNotCurrent(refexSpec);

                String logString = null;
                if (statusNid == SnomedMetadataRfx.getSTATUS_CURRENT_NID()) {
                    logString = "toggled rel negation (applied NOT!): "
                            + nidToName(cNid) + " :: "
                            + nidToName(roleNid) + " :: " + nidToName(valueNid);
                } else {
                    logString = "toggled rel negation (removed): "
                            + nidToName(cNid) + " :: "
                            + nidToName(roleNid) + " :: " + nidToName(valueNid);
                }
                BatchActionEventReporter.add(new BatchActionEvent(c,
                        BatchActionTaskType.LOGIC_NEGATE_RELATIONSHIP_VALUE,
                        BatchActionEventType.EVENT_SUCCESS, logString));

                // ADD UNCOMMITTED
                ConceptChronicleBI collectionConcept = ts.getConcept(collectionNid);
                if (collectionConcept.isAnnotationStyleRefex()) {
                    // Ts.get().addUncommitted(c); <-- done in BatchActionProcessor for concept
                    return true; // pass to BatchActionProcessor
                } else {
                    ts.addUncommitted(collectionConcept);
                    return false;
                }
            }
        }

        return changed;
    }
}