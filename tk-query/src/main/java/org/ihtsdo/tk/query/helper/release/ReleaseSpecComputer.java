/*
 * Copyright 2013 International Health Terminology Standards Development Organisation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ihtsdo.tk.query.helper.release;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ConceptFetcherBI;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.NidBitSetBI;
import org.ihtsdo.tk.api.ProcessUnfetchedConceptDataBI;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.coordinate.EditCoordinate;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.cs.ChangeSetPolicy;
import org.ihtsdo.tk.query.ComputeFromSpec;
import org.ihtsdo.tk.query.RefsetComputer;
import org.ihtsdo.tk.query.RefsetSpec;
import org.ihtsdo.tk.query.RefsetSpecFactory;
import org.ihtsdo.tk.query.RefsetSpecQuery;
import org.ihtsdo.tk.spec.ValidationException;

/**
 * Computes refsets from spec so that the members can be released.
 */
public class ReleaseSpecComputer {
    private EditCoordinate editCoordinate;
    private ViewCoordinate viewCoordinate;
    private Set<Integer> refsetParentNids = new HashSet<>();
    private ChangeSetPolicy csPolicy;
    
    public ReleaseSpecComputer(EditCoordinate editCoordinate, ViewCoordinate viewCoordinate,
            ChangeSetPolicy csPolicy, int... refsetParentNid) throws IOException, ValidationException, ContradictionException {
        this.editCoordinate = editCoordinate;
        this.viewCoordinate = viewCoordinate;
        this.csPolicy = csPolicy;
        for(int i = 0; i < refsetParentNid.length; i++){
            Set<Integer> children = Ts.get().getChildren(refsetParentNid[i], viewCoordinate);
            refsetParentNids.addAll(children);
        }
    }

    public void process() throws Exception {
        for (int conceptNid : refsetParentNids) {
            ConceptChronicleBI refsetConcept = Ts.get().getConcept(conceptNid);
            RefsetSpec refsetSpecHelper = new RefsetSpec(refsetConcept, true, viewCoordinate);
            ConceptChronicleBI refsetSpec = refsetSpecHelper.getRefsetSpecConcept();
            if (refsetSpec != null) {
                    System.out.println("### PROCESSING REFSET: " + refsetConcept.toUserString());
                    RefsetComputer.ComputeType computeType = RefsetComputer.ComputeType.CONCEPT; // default
                    if (refsetSpecHelper.isDescriptionComputeType()) {
                        computeType = RefsetComputer.ComputeType.DESCRIPTION;
                    } else if (refsetSpecHelper.isRelationshipComputeType()) {
                        //throw something
                    }
                    RefsetSpecQuery query = RefsetSpecFactory.createQuery(viewCoordinate,
                            refsetSpec,
                            refsetConcept,
                            computeType);
                    ComputeFromSpec.computeRefset(query, viewCoordinate, editCoordinate, conceptNid, csPolicy);
                    System.out.println("### FINISHED PROCESSING REFSET: " + refsetConcept.toUserString());
            }
        }
    }
}
