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

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.coordinate.EditCoordinate;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.cs.ChangeSetPolicy;
import org.ihtsdo.tk.api.relationship.RelationshipChronicleBI;
import org.ihtsdo.tk.binding.snomed.TermAux;
import org.ihtsdo.tk.dto.concept.TkConcept;
import org.ihtsdo.tk.dto.concept.component.attribute.TkConceptAttributes;
import org.ihtsdo.tk.dto.concept.component.refex.TkRefexAbstractMember;
import org.ihtsdo.tk.dto.concept.component.refex.type_member.TkRefexMember;
import org.ihtsdo.tk.dto.concept.component.relationship.TkRelationship;
import org.ihtsdo.tk.query.ComputeFromSpec;
import org.ihtsdo.tk.query.RefsetComputer;
import org.ihtsdo.tk.query.RefsetSpec;
import org.ihtsdo.tk.query.RefsetSpecFactory;
import org.ihtsdo.tk.query.RefsetSpecQuery;
import org.ihtsdo.tk.spec.ValidationException;

/**
 * Computes refsets from spec so that the members can be released.
 */
public class ReleaseSpecProcessor {
    private EditCoordinate editCoordinate;
    private ViewCoordinate viewCoordinate;
    private Set<Integer> refsetParentNids = new HashSet<>();
    private ChangeSetPolicy csPolicy;
    
    public ReleaseSpecProcessor(EditCoordinate editCoordinate, ViewCoordinate viewCoordinate,
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
    
    public void writeRefsetSpecMetadata(File outputDirectory) throws IOException, TerminologyException, ContradictionException{
        String fileName = "refsetMetadata.jbin";
        File eConceptsFile = new File(outputDirectory, fileName);
        eConceptsFile.getParentFile().mkdirs();
        BufferedOutputStream eConceptsBos = new BufferedOutputStream(new FileOutputStream(eConceptsFile));

        DataOutputStream eConceptDOS = new DataOutputStream(eConceptsBos);
        for (int conceptNid : refsetParentNids) {
            ConceptChronicleBI refsetConcept = Ts.get().getConcept(conceptNid);
            RefsetSpec refsetSpecHelper = new RefsetSpec(refsetConcept, true, viewCoordinate);
            HashSet<ConceptChronicleBI> metadataConcepts = new HashSet<>();
            ConceptChronicleBI refsetSpec = refsetSpecHelper.getRefsetSpecConcept();
            metadataConcepts.add(refsetSpec);
            if (refsetSpec != null) {
                for(ConceptVersionBI cv : refsetSpecHelper.getCommentsRefsetConcepts()){
                    metadataConcepts.add(cv.getChronicle());
                }
                metadataConcepts.add(refsetSpecHelper.getComputeConcept());
                metadataConcepts.add(refsetSpecHelper.getEditConcept());
                metadataConcepts.add(refsetSpecHelper.getMarkedParentRefsetConcept());
                
                for(ConceptChronicleBI c : metadataConcepts){
                    TkConcept eC = new TkConcept(c);
                    for(TkRefexAbstractMember r: eC.getRefsetMembers()){
                        r.setPathUuid(TermAux.WB_AUX_PATH.getLenient().getPrimUuid());
                    }
                    eC.writeExternal(eConceptDOS);
                }
//              write supporting metadata relationships on refset concept
                TkConcept refsetEConcept = new TkConcept();
                refsetEConcept.annotationStyleRefex = refsetConcept.isAnnotationStyleRefex();
                refsetEConcept.annotationIndexStyleRefex = refsetConcept.isAnnotationIndex();
                refsetEConcept.primordialUuid = refsetConcept.getPrimUuid();
                refsetEConcept.relationships = new ArrayList<TkRelationship>(refsetConcept.getRelationshipsOutgoing().size());

                for (RelationshipChronicleBI rel : refsetConcept.getRelationshipsOutgoing()) {
                    TkRelationship r = new TkRelationship(rel);
                    if(r.getPathUuid().equals(TermAux.WB_AUX_PATH.getLenient().getPrimUuid())){
                        refsetEConcept.relationships.add(r);
                    }
                }
                refsetEConcept.writeExternal(eConceptDOS);
            }
        }
        eConceptDOS.close();
    }
}
