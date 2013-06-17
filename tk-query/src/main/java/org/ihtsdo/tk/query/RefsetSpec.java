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
package org.ihtsdo.tk.query;

import org.ihtsdo.tk.query.helper.RefsetHelper;
import java.io.IOException;
import java.util.Collection;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.TerminologyStoreDI;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.coordinate.EditCoordinate;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.api.refex.type_long.RefexLongVersionBI;

public class RefsetSpec {

    private ConceptChronicleBI spec;
    private TerminologyStoreDI ts;
    private ViewCoordinate vc;
    private ConceptChronicleBI refsetConcept;

    /**
     * Use this constructor if you wish to input the refset spec concept.
     * 
     * @param spec
     */
    public RefsetSpec(ConceptChronicleBI spec, ViewCoordinate vc) {
        this.spec = spec;
        ts = Ts.get();
        this.vc = vc;
    }

    /**
     * Use this constructor if you wish to input the member refset concept,
     * rather than the refset spec concept.
     * 
     * @param concept
     * @param memberRefsetInputted
     */
    public RefsetSpec(ConceptChronicleBI concept, boolean memberRefsetInputted, ViewCoordinate vc) {
        ts = Ts.get();
        this.vc = vc;
        if (memberRefsetInputted) {
            try {
                int specifiesRefsetRelNid =
                        ts.getNidForUuids(RefsetAuxiliary.Concept.SPECIFIES_REFSET.getUids());
                Collection<? extends ConceptVersionBI> relSources = concept.getVersion(vc).getRelationshipsIncomingSourceConceptsActive(
                                                                                                           specifiesRefsetRelNid);
                if(relSources != null && !relSources.isEmpty()){
                    this.spec = relSources.iterator().next().getChronicle();
                }else{
                    this.spec = null;
                }
                this.refsetConcept = concept;
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            this.spec = concept;
        }
    }

    public boolean isConceptComputeType() {
        try {
            int refsetComputeTypeRelNid =
                    ts.getNidForUuids(RefsetAuxiliary.Concept.REFSET_COMPUTE_TYPE_REL.getUids());
            if(spec == null){
                return true;
            }
            ConceptVersionBI version = spec.getVersion(vc);
            Collection<? extends ConceptVersionBI> relTargets = null;
            if(version != null){
                relTargets = spec.getVersion(vc).getRelationshipsOutgoingTargetConceptsActive(refsetComputeTypeRelNid);
            }
            ConceptChronicleBI computeType = null;
            if(relTargets != null){
                  computeType = relTargets.iterator().next();
            }
            if (computeType == null) {
                // backwards compatibility - if no compute type has been specified, then a default compute type of
                // concept is used
                return true;
            } else {
                if (computeType.getConceptNid() == ts.getNidForUuids(
                    RefsetAuxiliary.Concept.CONCEPT_COMPUTE_TYPE.getUids())) {
                    return true;
                } else {
                    return false;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public String getComputeTypeString() {
        String computeTypeString = "";
        if (isConceptComputeType()) {
            computeTypeString = "concept";
        } else if (isDescriptionComputeType()) {
            computeTypeString = "description";
        } else {
            computeTypeString = "unknown";
        }
        return computeTypeString;
    }

    public boolean isDescriptionComputeType() {
        try {
            int refsetComputeTypeRelNid =
                    ts.getNidForUuids(RefsetAuxiliary.Concept.REFSET_COMPUTE_TYPE_REL.getUids());
            if(spec == null){
                return false;
            }
            ConceptVersionBI version = spec.getVersion(vc);
            Collection<? extends ConceptVersionBI> relTargets = null;
            if(version != null){
                relTargets = spec.getVersion(vc).getRelationshipsOutgoingTargetConceptsActive(refsetComputeTypeRelNid);
            }
            ConceptChronicleBI computeType = null;
            if(relTargets != null){
                  computeType = relTargets.iterator().next();
            }
            if (computeType == null) {
                // backwards compatibility - if no compute type has been specified, then a default compute type of
                // concept is used
                return false;
            } else {
                if (computeType.getConceptNid() == ts.getNidForUuids(
                    RefsetAuxiliary.Concept.DESCRIPTION_COMPUTE_TYPE.getUids())) {
                    return true;
                } else {
                    return false;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean isRelationshipComputeType() {
        try {
            int refsetComputeTypeRelNid =
                    ts.getNidForUuids(RefsetAuxiliary.Concept.REFSET_COMPUTE_TYPE_REL.getUids());
            ConceptChronicleBI computeType = spec.getVersion(vc).getRelationshipsOutgoingTargetConceptsActive(
                    refsetComputeTypeRelNid).iterator().next();

            if (computeType == null) {
                // backwards compatibility - if no compute type has been specified, then a default compute type of
                // concept is used
                return false;
            } else {
                if (computeType.getConceptNid() == ts.getNidForUuids(
                    RefsetAuxiliary.Concept.RELATIONSHIP_COMPUTE_TYPE.getUids())) {
                    return true;
                } else {
                    return false;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public ConceptChronicleBI getRefsetSpecConcept() {
        return spec;
    }

    public ConceptChronicleBI getMemberRefsetConcept() {
        try {
            if(refsetConcept != null){
                return refsetConcept;
            }
            int specifiesRefsetRelNid = ts.getNidForUuids(RefsetAuxiliary.Concept.SPECIFIES_REFSET.getUids());
            Collection<? extends ConceptVersionBI> specConcepts = getRefsetSpecConcept().getVersion(vc).getRelationshipsOutgoingTargetConceptsActive(
                                                                                                       specifiesRefsetRelNid);
            if(!specConcepts.isEmpty()){
                refsetConcept = specConcepts.iterator().next().getChronicle();
            }
            return refsetConcept;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public ConceptChronicleBI getMarkedParentRefsetConcept() {
        try {
            int markedParentRelNid =
                    ts.getNidForUuids(RefsetAuxiliary.Concept.MARKED_PARENT_REFSET.getUids());
            ConceptChronicleBI memberRefsetConcept = getMemberRefsetConcept();
            if (memberRefsetConcept == null) {
                return null;
            }

            return memberRefsetConcept.getVersion(vc).getRelationshipsOutgoingTargetConceptsActive(
                    markedParentRelNid).iterator().next().getChronicle();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public ConceptChronicleBI getPromotionRefsetConcept() {
        try {
            int promotionRelNid =
                    ts.getNidForUuids(RefsetAuxiliary.Concept.PROMOTION_REL.getUids());
            ConceptChronicleBI memberRefsetConcept = getMemberRefsetConcept();
            if (memberRefsetConcept == null) {
                return null;
            }
            Collection<? extends ConceptVersionBI> promotionConcepts = 
                    memberRefsetConcept.getVersion(vc).getRelationshipsOutgoingTargetConceptsActive(promotionRelNid);
            if(promotionConcepts.isEmpty()){
                return null;
            }
            return promotionConcepts.iterator().next().getChronicle();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void setLastComputeTime(Long time, EditCoordinate ec) throws Exception {
        if (spec == null) {
            return; // not a correct refset spec (i.e. doesn't have an associated spec)
        }

        ConceptChronicleBI lastComputeTimeConcept = getComputeConcept();
        if (lastComputeTimeConcept == null) {
            return; // 
        }

        RefsetHelper helper = new RefsetHelper(vc, ec);

        if (spec != null && lastComputeTimeConcept != null) {
            helper.newLongRefsetExtension(lastComputeTimeConcept.getConceptNid(), spec.getConceptNid(), time);
        }
        ts.addUncommittedNoChecks(lastComputeTimeConcept);
    }

    public ConceptChronicleBI getEditConcept() throws TerminologyException, IOException {
        try {
            int editTimeRelNid = ts.getNidForUuids(RefsetAuxiliary.Concept.EDIT_TIME_REL.getUids());
            ConceptChronicleBI memberRefsetConcept = getMemberRefsetConcept();
            if (memberRefsetConcept == null) {
                return null;
            }

            return memberRefsetConcept.getVersion(vc).getRelationshipsOutgoingTargetConceptsActive(
                    editTimeRelNid).iterator().next().getChronicle();
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    public ConceptChronicleBI getComputeConcept() throws TerminologyException, IOException {
        try {
            int computeTimeRelNid = ts.getNidForUuids(RefsetAuxiliary.Concept.COMPUTE_TIME_REL.getUids());
            ConceptChronicleBI memberRefsetConcept = getMemberRefsetConcept();
            if (memberRefsetConcept == null) {
                return null;
            }

            return memberRefsetConcept.getVersion(vc).getRelationshipsOutgoingTargetConceptsActive(
                    computeTimeRelNid).iterator().next().getChronicle();
        } catch (Exception e) {
            throw new IOException(e);
        }
    }
    
    public Collection<? extends ConceptVersionBI> getCommentsRefsetConcepts() throws IOException {
        try {
            int computeTimeRelNid = ts.getNidForUuids(RefsetAuxiliary.Concept.COMMENTS_REL.getUids());
            ConceptChronicleBI memberRefsetConcept = getMemberRefsetConcept();
            if (memberRefsetConcept == null) {
                return null;
            }

            return memberRefsetConcept.getVersion(vc).getRelationshipsOutgoingTargetConceptsActive(
                    computeTimeRelNid);
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    public void setLastEditTime(Long time, EditCoordinate ec) throws Exception {
        if (getRefsetSpecConcept() == null) {
            return; // not a correct refset spec (i.e. doesn't have an associated spec)
        }
        try {

            RefsetHelper helper = new RefsetHelper(vc, ec);
            ConceptChronicleBI lastEditTimeConcept = getEditConcept();

            if (spec != null && lastEditTimeConcept != null) {
                helper.newLongRefsetExtension(lastEditTimeConcept.getConceptNid(), spec.getConceptNid(), time);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Error setting the last edit time of refset : " + e.getLocalizedMessage());
        }
    }

    public Long getLastComputeTime() throws TerminologyException, IOException {
        if (getRefsetSpecConcept() == null) {
            return null; // not a correct refset spec (i.e. doesn't have an associated spec)
        }
        ConceptChronicleBI lastComputeTimeConcept = getComputeConcept();
        Long latestTime = null;
        Collection<? extends RefexVersionBI<?>> refsetMembersActive = lastComputeTimeConcept.getRefsetMembersActive(vc);
        for(RefexVersionBI r : refsetMembersActive){
            RefexLongVersionBI rl = (RefexLongVersionBI) r;
            if(latestTime == null){
                latestTime = rl.getLong1();
            }else if(rl.getLong1() > latestTime){
                latestTime = rl.getLong1();
            }
        }
        return latestTime;
    }

    private Long getLastEditTime() throws TerminologyException, IOException {
        if (getRefsetSpecConcept() == null) {
            return null; // not a correct refset spec (i.e. doesn't have an associated spec)
        }
        ConceptChronicleBI lastEditTimeConcept = getEditConcept();
        Long latestTime = null;
        Collection<? extends RefexVersionBI<?>> refsetMembersActive = lastEditTimeConcept.getRefsetMembersActive(vc);
        for(RefexVersionBI r : refsetMembersActive){
            RefexLongVersionBI rl = (RefexLongVersionBI) r;
            if(latestTime == null){
                latestTime = rl.getLong1();
            }else if(rl.getLong1() > latestTime){
                latestTime = rl.getLong1();
            }
        }
        return latestTime;
    }

    public boolean needsCompute() throws TerminologyException, IOException {
        if (getRefsetSpecConcept() == null) {
            return false; // not a correct refset spec (i.e. doesn't have an associated spec)
        }
        Long lastComputeTime = getLastComputeTime();
        Long lastEditTime = getLastEditTime();
        if (lastEditTime == null) {
            return false; // hasn't ever been edited i.e. empty spec
        }
        if (lastComputeTime == null) {
            return true; // hasn't ever been computed, but it has been edited
        }

        return lastEditTime > lastComputeTime;
    }

}
