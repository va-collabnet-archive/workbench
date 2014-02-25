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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import org.ihtsdo.batch.BatchActionEvent.BatchActionEventType;
import static org.ihtsdo.batch.BatchActionTask.nidToName;
import static org.ihtsdo.batch.BatchActionTask.tsSnapshot;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.blueprint.InvalidCAB;
import org.ihtsdo.tk.api.blueprint.RefexCAB;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.coordinate.EditCoordinate;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.description.DescriptionChronicleBI;
import org.ihtsdo.tk.api.description.DescriptionVersionBI;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.dto.concept.component.refex.TK_REFEX_TYPE;

/**
 * BatchActionTaskDescriptionRefsetAddMember
 *
 */
public class BatchActionTaskDescriptionRefsetAddMember
        extends AbstractBatchActionTaskDescription {

    // REFSET MEMBER
    private Object refsetSetValue;

    public BatchActionTaskDescriptionRefsetAddMember() {
        super();
        refsetSetValue = null;
    }

    public void setRefsetSetValue(Object refsetValue) {
        this.refsetSetValue = refsetValue;
    }

    // BatchActionTask
    @Override
    public boolean execute(ConceptVersionBI c, EditCoordinate ec, ViewCoordinate vc)
            throws IOException, InvalidCAB, ContradictionException {
        boolean changed = false;
        Collection<? extends DescriptionChronicleBI> descriptions = c.getDescriptions();
        ArrayList<DescriptionChronicleBI> descList = new ArrayList<>();
        for (DescriptionChronicleBI dcbi : descriptions) {
            boolean criteriaPass;
            DescriptionVersionBI dvbi = null;
            try {
                dvbi = dcbi.getVersion(vc);
            } catch (ContradictionException ex) {
                BatchActionEventReporter.add(new BatchActionEvent(c,
                        BatchActionTaskType.DESCRIPTION_INITIAL_CHAR_CASE_SENSITIVITY,
                        BatchActionEventType.EVENT_ERROR,
                        "ERROR: multiple active versions"));
            }

            if (dvbi == null) {
                continue; // nothing to change
            }

            criteriaPass = testCriteria(dvbi, vc);

            // DO EDIT
            if (criteriaPass) {
                Collection<? extends RefexVersionBI<?>> currentRefexes = dvbi.getRefexMembersActive(vc, collectionNid);

                if (currentRefexes != null) {
                    for (RefexVersionBI rvbi : currentRefexes) {
                        if (rvbi.getRefexNid() == collectionNid) {
                            BatchActionEventReporter.add(new BatchActionEvent(c, BatchActionTaskType.DESCRIPTION_REFSET_ADD_MEMBER,
                                    BatchActionEventType.EVENT_NOOP, "already member of: " + nidToName(collectionNid)));
                            return false;
                        }
                    }
                }

                RefexCAB refexSpec;
                if (refsetSetValue == null) {
                    refexSpec = new RefexCAB(TK_REFEX_TYPE.MEMBER, dvbi.getNid(), collectionNid);
                } else {
                    refexSpec = new RefexCAB(TK_REFEX_TYPE.CID, dvbi.getNid(), collectionNid);
                }

                refexSpec.setMemberContentUuid();
                tsSnapshot.constructIfNotCurrent(refexSpec);

                BatchActionEventReporter.add(new BatchActionEvent(c, BatchActionTaskType.DESCRIPTION_REFSET_ADD_MEMBER,
                        BatchActionEventType.EVENT_SUCCESS, "member added to: " + nidToName(collectionNid)));

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

        if (!changed) {
            BatchActionEventReporter.add(new BatchActionEvent(c,
                    BatchActionTaskType.DESCRIPTION_INITIAL_CHAR_CASE_SENSITIVITY,
                    BatchActionEventType.EVENT_NOOP,
                    "description initial caps not changed on concept : "
                    + nidToName(c.getConceptNid())));
        }

        return changed;
    }
}
