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
import org.dwfa.ace.api.Terms;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.helper.descriptionlogic.DescriptionLogic;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.TerminologyBuilderBI;
import org.ihtsdo.tk.api.blueprint.ConceptCB;
import org.ihtsdo.tk.api.blueprint.InvalidCAB;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.coordinate.EditCoordinate;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.api.refex.type_nid.RefexNidVersionBI;
import org.ihtsdo.tk.api.relationship.RelationshipChronicleBI;
import org.ihtsdo.tk.dto.concept.component.refex.TK_REFEX_TYPE;

/**
 * BatchActionTaskLogicUnionRetire
 * 
 */
public class BatchActionTaskLogicUnionRetire extends BatchActionTask {

    // REFSET MEMBER
    private int collectionNid;
    // FILTER
    private TK_REFEX_TYPE refsetType;

    public BatchActionTaskLogicUnionRetire() {
        this.collectionNid = Integer.MAX_VALUE;
    }

    public void setCollectionNid(int collectionNid) {
        this.collectionNid = collectionNid;
    }

    public void setRefsetType(TK_REFEX_TYPE refsetType) {
        this.refsetType = refsetType;
    }

    // BatchActionTask
    @Override
    public boolean execute(ConceptVersionBI c, EditCoordinate ec, ViewCoordinate vc)
            throws IOException, TerminologyException, ContradictionException, InvalidCAB {
        int parentMemberTypeNid = Terms.get().getConcept(
                RefsetAuxiliary.Concept.MARKED_PARENT.getPrimoridalUid()).getConceptNid();

        Collection<? extends RefexVersionBI<?>> rm = c.getRefexesActive(vc);
        for (RefexChronicleBI<?> rcbi : rm) {
            ConceptChronicleBI cb = rcbi.getEnclosingConcept();
            Collection<? extends RelationshipChronicleBI> parents = cb.getRelationshipsOutgoing();
            for (RelationshipChronicleBI parentRel : parents) {
                if (parentRel.getTargetNid() == DescriptionLogic.getUnionSetsRefsetNid()) {
                    Collection<? extends RefexVersionBI<?>> ml = cb.getRefsetMembersActive(vc);
                    for (RefexVersionBI<?> member : ml) {
                        // member.
                        if (RefexNidVersionBI.class.isAssignableFrom(member.getClass())) {
                            int memberTypeNid = ((RefexNidVersionBI) member).getNid1();
                            if (memberTypeNid == parentMemberTypeNid) {
                                // retire marked parent concept
                                ConceptVersionBI cvbi = ts.getConceptVersion(vc,
                                        member.getReferencedComponentNid());
                                ConceptCB bp = cvbi.makeBlueprint(vc);
                                bp.setRetired();

                                TerminologyBuilderBI tc = Ts.get().getTerminologyBuilder(ec, vc);
                                ConceptChronicleBI ccbi = tc.constructIfNotCurrent(bp);
                                ts.addUncommitted(ccbi);
                                BatchActionEventReporter.add(new BatchActionEvent(c,
                                        BatchActionTaskType.LOGIC_UNION_SET_RETIRE,
                                        BatchActionEvent.BatchActionEventType.EVENT_NOOP,
                                        "retired union concept: " + nidToName(ccbi.getNid())));
                            }

                            // retire member
                            for (int editPath : ec.getEditPaths()) {
                                member.makeAnalog(RETIRED_NID,
                                        Long.MAX_VALUE,
                                        ec.getAuthorNid(),
                                        ec.getModuleNid(),
                                        editPath);
                            }
                            ts.addUncommitted(ts.getConcept(member.getRefexNid()));

                            BatchActionEventReporter.add(new BatchActionEvent(c,
                                    BatchActionTaskType.LOGIC_UNION_SET_RETIRE,
                                    BatchActionEvent.BatchActionEventType.EVENT_NOOP,
                                    "retired refset membership: " + nidToName(member.getNid())));


                        }
                    }
                }
            }
        }


        return false; // already handled in BatchActionTaskLogicUnionRetireUI
    }
}
