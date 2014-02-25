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
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.batch.BatchActionEvent.BatchActionEventType;
import static org.ihtsdo.batch.BatchActionTask.nidToName;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.TerminologyBuilderBI;
import org.ihtsdo.tk.api.blueprint.InvalidCAB;
import org.ihtsdo.tk.api.blueprint.RefexCAB;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.coordinate.EditCoordinate;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.description.DescriptionChronicleBI;
import org.ihtsdo.tk.api.description.DescriptionVersionBI;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.api.refex.type_boolean.RefexBooleanVersionBI;
import org.ihtsdo.tk.api.refex.type_int.RefexIntVersionBI;
import org.ihtsdo.tk.api.refex.type_nid.RefexNidVersionBI;
import org.ihtsdo.tk.api.refex.type_string.RefexStringVersionBI;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRfx;
import org.ihtsdo.tk.dto.concept.component.refex.TK_REFEX_TYPE;

/**
 * BatchActionTaskDescriptionTextFindCreate
 *
 */
public class BatchActionTaskDescriptionTextFindCreate
        extends AbstractBatchActionTaskDescription {

    String replacementText;

    public BatchActionTaskDescriptionTextFindCreate() {
        super();
        replacementText = null;
    }

    void setReplacementText(String replaceTextStr) {
        this.replacementText = replaceTextStr;
    }

    // BatchActionTask
    @Override
    public boolean execute(ConceptVersionBI c, EditCoordinate ec, ViewCoordinate vc)
            throws IOException, InvalidCAB, ContradictionException, TerminologyException {
        boolean changed = false;
        Collection<? extends DescriptionChronicleBI> descriptions = c.getDescriptions();
        ArrayList<DescriptionChronicleBI> descriptionList = new ArrayList<>();
        for (DescriptionChronicleBI dcbi : descriptions) {
            descriptionList.add(dcbi);
        }
        for (DescriptionChronicleBI dcbi : descriptionList) {
            boolean criteriaPass;
            DescriptionVersionBI dvbi = null;
            try {
                dvbi = dcbi.getVersion(vc);
            } catch (ContradictionException ex) {
                BatchActionEventReporter.add(new BatchActionEvent(c,
                        BatchActionTaskType.DESCRIPTION_TEXT_FIND_CREATE,
                        BatchActionEventType.EVENT_ERROR,
                        "ERROR: multiple active versions"));
            }

            if (dvbi == null) {
                continue; // nothing to change
            }

            criteriaPass = testCriteria(dvbi, vc);

            if (criteriaPass) {
                changed = true;
                if (replacementText == null) {
                    replacementText = "";
                }
                String newText = dvbi.getText();
                switch (searchByTextConstraint) {
                    case 1: // contains
                        if (isSearchCaseSensitive) {
                            newText = newText.replace(searchText, replacementText);
                        } else {
                            int idxSearchText = newText.toUpperCase().indexOf(searchText.toUpperCase());
                            String textBefore = newText.substring(0, idxSearchText);
                            String textAfter = newText.substring(idxSearchText + searchText.length(), newText.length());
                            newText = textBefore + replacementText + textAfter;
                        }
                        break;
                    case 2: // begins with
                        newText = replacementText + newText.substring(searchText.length());
                        break;
                    case 3: // ends with
                        newText = newText.substring(0, newText.length() - searchText.length()) + replacementText;
                        break;
                }

                try {
                    // similar to CloneAction
                    I_GetConceptData concept = Terms.get().getConceptForNid(dvbi.getConceptNid());
                    DescriptionVersionBI desc = (DescriptionVersionBI) dvbi;
                    I_GetConceptData typeConcept = Terms.get().getConcept(SnomedMetadataRfx.getDES_SYNONYM_NID());
                    I_GetConceptData statusConcept = Terms.get().getConcept(desc.getStatusNid());
                    Collection<? extends RefexVersionBI<?>> oldRefexes = desc.getRefexesActive(vc);
                    I_DescriptionVersioned newDesc = Terms.get().newDescription(
                            UUID.randomUUID(),
                            concept,
                            desc.getLang(),
                            newText,
                            typeConcept,
                            config,
                            statusConcept,
                            Long.MAX_VALUE);
                    newDesc.setInitialCaseSignificant(desc.isInitialCaseSignificant());
                    TerminologyBuilderBI tc = Ts.get().getTerminologyBuilder(ec, vc);
                    int dosNid = SnomedMetadataRfx.getDES_SYNONYM_NID();
                    for (RefexVersionBI refex : oldRefexes) {
                        if (refex.getRefexNid() != dosNid) { //not cloning degree of synonymy refeset membership
                            RefexCAB newSpec;
                            if (RefexNidVersionBI.class.isAssignableFrom(refex.getClass())) {
                                newSpec = new RefexCAB(
                                        TK_REFEX_TYPE.CID,
                                        newDesc.getNid(),
                                        refex.getRefexNid());
                                RefexNidVersionBI cv
                                        = (RefexNidVersionBI) refex.getVersion(vc);
                                int typeNid = cv.getNid1();
                                if (typeNid == SnomedMetadataRfx.getDESC_PREFERRED_NID()) {
                                    typeNid = SnomedMetadataRfx.getDESC_ACCEPTABLE_NID();
                                } 
                                newSpec.put(RefexCAB.RefexProperty.CNID1, typeNid);
                            } else if (RefexBooleanVersionBI.class.isAssignableFrom(refex.getClass())) {
                                newSpec = new RefexCAB(
                                        TK_REFEX_TYPE.BOOLEAN,
                                        newDesc.getNid(),
                                        refex.getRefexNid());
                                RefexBooleanVersionBI bv
                                        = (RefexBooleanVersionBI) refex.getVersion(vc);
                                boolean boolean1 = bv.getBoolean1();
                                newSpec.put(RefexCAB.RefexProperty.BOOLEAN1, boolean1);
                            } else if (RefexStringVersionBI.class.isAssignableFrom(refex.getClass())) {
                                newSpec = new RefexCAB(
                                        TK_REFEX_TYPE.STR,
                                        newDesc.getNid(),
                                        refex.getRefexNid());
                                RefexStringVersionBI sv
                                        = (RefexStringVersionBI) refex.getVersion(vc);
                                String string1 = sv.getString1();
                                newSpec.put(RefexCAB.RefexProperty.STRING1, string1);
                            } else if (RefexIntVersionBI.class.isAssignableFrom(refex.getClass())) {
                                newSpec = new RefexCAB(
                                        TK_REFEX_TYPE.INT,
                                        newDesc.getNid(),
                                        refex.getRefexNid());
                                RefexIntVersionBI iv
                                        = (RefexIntVersionBI) refex.getVersion(vc);
                                int int1 = iv.getInt1();
                                newSpec.put(RefexCAB.RefexProperty.INTEGER1, int1);
                            } else {
                                throw new UnsupportedOperationException("can't handle refex type: "
                                        + refex);
                            }

                            tc.construct(newSpec);
                        }

                    }

                } catch (PropertyVetoException | TerminologyException ex) {
                    BatchActionEventReporter.add(new BatchActionEvent(c,
                            BatchActionTaskType.DESCRIPTION_TEXT_FIND_CREATE,
                            BatchActionEventType.EVENT_ERROR,
                            "could not create description : " + newText));
                    return false;
                }
                BatchActionEventReporter.add(new BatchActionEvent(c,
                        BatchActionTaskType.DESCRIPTION_TEXT_FIND_CREATE,
                        BatchActionEventType.EVENT_SUCCESS,
                        "description replaced: " + newText));
            }
        }

        if (!changed) {
            BatchActionEventReporter.add(new BatchActionEvent(c,
                    BatchActionTaskType.DESCRIPTION_TEXT_FIND_CREATE,
                    BatchActionEventType.EVENT_NOOP,
                    "description text not replaced on concept : "
                    + nidToName(c.getConceptNid())));
        }

        return changed;
    }

}
