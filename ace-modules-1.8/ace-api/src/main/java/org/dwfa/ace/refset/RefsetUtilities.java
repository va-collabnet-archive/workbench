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
package org.dwfa.ace.refset;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LineageHelper;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConcept;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.tapi.TerminologyRuntimeException;
import org.dwfa.tapi.spec.ConceptSpec;

@Deprecated
public abstract class RefsetUtilities {

    public final int ID_NOT_FOUND = 0;

    protected I_GetConceptData altIsA = null;

    protected I_GetConceptData pathConcept;

    protected I_TermFactory termFactory;

    protected int retiredConceptId;
    protected int currentStatusId;
    protected int activeStatusId;
    protected int typeId;

    Map<Integer, I_GetConceptData> conceptCache = new HashMap<Integer, I_GetConceptData>();

    public int getInclusionTypeForRefset(I_ThinExtByRefVersioned part) {
        System.out.println("getInclusionTypeForRefset " + part);
        int typeId = ID_NOT_FOUND;
        I_ThinExtByRefPart latest = null;
        List<? extends I_ThinExtByRefPart> versions = part.getVersions();
        for (I_ThinExtByRefPart version : versions) {

            if (latest == null) {
                if (isActiveStatus(version.getStatus())) {
                    latest = version;
                }
            } else {
                if (latest.getVersion() < version.getVersion()) {
                    if (version.getStatus() == retiredConceptId) {
                        // member has a later retirement so exclude
                        latest = null;
                    } else {
                        latest = version;
                    }
                }
            }
        }

        if (latest != null) {
            I_ThinExtByRefPartConcept temp = (I_ThinExtByRefPartConcept) latest;
            typeId = temp.getConceptId();
        }

        System.out.println("getInclusionTypeForRefset result " + latest);

        return typeId;
    }

    public Set<Integer> getParentsOfConcept(int conceptId) throws IOException, Exception {

        Set<Integer> parents = new HashSet<Integer>();

        I_GetConceptData concept = getConcept(conceptId);
        List<I_RelTuple> parenttuples = concept.getSourceRelTuples(getStatuses(),
            (this.altIsA == null ? getIntSet(ConceptConstants.SNOMED_IS_A) : getIntSet(this.altIsA)), null, false);

        /*
         * Iterate over children
         */
        for (I_RelTuple parent : parenttuples) {

            List<I_ConceptAttributeTuple> atts = getConcept(parent.getC2Id()).getConceptAttributeTuples(null, null);
            I_ConceptAttributeTuple att = getLatestAttribute(atts);
            if (isValidStatus(att)) {
                parents.add(parent.getC2Id());
            }
        }

        return parents;
    }

    private I_IntSet getStatuses() throws Exception {
        return getIntSet(
            ArchitectonicAuxiliary.Concept.CURRENT, 
            ArchitectonicAuxiliary.Concept.ACTIVE,
            ArchitectonicAuxiliary.Concept.PENDING_MOVE,
            ArchitectonicAuxiliary.Concept.CURRENT_UNREVIEWED, 
            ArchitectonicAuxiliary.Concept.DO_NOT_EDIT_FOR_RELEASE);
    }

    private boolean isValidStatus(I_ConceptAttributeTuple att) throws TerminologyException, IOException {
        int pendingMoveStatusId = 
                termFactory.getConcept(ArchitectonicAuxiliary.Concept.PENDING_MOVE.getUids()).getConceptId();
        int currentUnreviewedStatusId = 
                termFactory.getConcept(ArchitectonicAuxiliary.Concept.CURRENT_UNREVIEWED.getUids()).getConceptId();
        int noEditStatusId = 
                termFactory.getConcept(ArchitectonicAuxiliary.Concept.DO_NOT_EDIT_FOR_RELEASE.getUids()).getConceptId();
        
        return (att.getConceptStatus() == currentStatusId ||
                att.getConceptStatus() == activeStatusId ||
                att.getConceptStatus() == pendingMoveStatusId || 
                att.getConceptStatus() == currentUnreviewedStatusId ||
                att.getConceptStatus() == noEditStatusId);
    }

    protected boolean isActiveStatus(int statusId) {
        return (statusId == currentStatusId || statusId == activeStatusId);
    }
    
    public Set<Integer> getAncestorsOfConcept(int conceptId, ClosestDistanceHashSet concepts) 
            throws IOException, Exception {

        Set<Integer> ancestors = new HashSet<Integer>(); 
        for (I_GetConceptData parent : new LineageHelper().getAllAncestors(termFactory.getConcept(conceptId))) {
            ancestors.add(parent.getConceptId());
        }
        return ancestors;
    }

    public List<Integer> getChildrenOfConcept(int conceptId) throws IOException, Exception {

        List<Integer> descendants = new ArrayList<Integer>(); 
        
        LineageHelper lineageHelper = new LineageHelper();
        for (I_GetConceptData child : lineageHelper.getAllDescendants(
                termFactory.getConcept(conceptId), lineageHelper.new FirstRelationOnly())) {
            descendants.add(child.getConceptId());
        }
        return descendants;
     }
    
    public List<Integer> getDescendantsOfConcept(int conceptId) throws IOException, Exception {

       List<Integer> descendants = new ArrayList<Integer>(); 
       descendants.add(conceptId);
       for (I_GetConceptData child : new LineageHelper().getAllDescendants(termFactory.getConcept(conceptId))) {
           descendants.add(child.getConceptId());
       }
       return descendants;
    }

    public List<Integer> getSpecificationRefsets() throws Exception {

        List<Integer> allowedRefsets = new ArrayList<Integer>();

        I_IntSet status = termFactory.newIntSet();
        status.add(termFactory.getConcept(ArchitectonicAuxiliary.Concept.CURRENT.getUids()).getConceptId());
        status.add(termFactory.getConcept(ArchitectonicAuxiliary.Concept.ACTIVE.getUids()).getConceptId());
        
        I_IntSet is_a = termFactory.newIntSet();
        if (this.altIsA == null) {
            is_a.add(termFactory.getConcept(ArchitectonicAuxiliary.Concept.IS_A_REL.getUids()).getConceptId());
            is_a.add(termFactory.getConcept(ConceptConstants.SNOMED_IS_A.localize().getUids()).getConceptId());
        } else {
            is_a.add(this.altIsA.getConceptId());
        }

        I_GetConceptData refsetRoot = termFactory.getConcept(RefsetAuxiliary.Concept.REFSET_IDENTITY.getUids());

        Set<I_GetConceptData> refsetChildren = refsetRoot.getDestRelOrigins(status, is_a, null, false);
        
        int refsetPurposeRelId = 
            termFactory.getConcept(RefsetAuxiliary.Concept.REFSET_PURPOSE_REL.getUids()).getConceptId();
        
        for (I_GetConceptData refsetConcept : refsetChildren) {
            Set<I_GetConceptData> purposeConcepts = new HashSet<I_GetConceptData>();

            List<? extends I_RelVersioned> rels = refsetConcept.getSourceRels();
            for (I_RelVersioned rel : rels) {
                List<I_RelTuple> tuples = rel.getTuples();
                for (I_RelTuple tuple : tuples) {                    
                    if (isActiveStatus(tuple.getStatusId()) && tuple.getRelTypeId() == refsetPurposeRelId) {
                        purposeConcepts.add(getConcept(tuple.getC2Id()));
                    }
                }
            }

            if (purposeConcepts.size() == 1) {

                if (purposeConcepts.iterator().next().getConceptId() == termFactory.getConcept(
                    RefsetAuxiliary.Concept.INCLUSION_SPECIFICATION.getUids()).getConceptId()) {
                    if (getMemberSetConcept(refsetConcept.getConceptId()) == null) {
                        System.out.println("ERROR: inclusion specification concept does not have a defined 'generates' relationship. Skipping generation of refset "
                            + refsetConcept);
                    } else {
                        allowedRefsets.add(refsetConcept.getConceptId());
                    }
                }
            }
        }
        return allowedRefsets;
    }

    public I_IntSet getIntSet(ArchitectonicAuxiliary.Concept... concepts) throws Exception {
        I_IntSet status = termFactory.newIntSet();

        for (ArchitectonicAuxiliary.Concept concept : concepts) {
            status.add(termFactory.getConcept(concept.getUids()).getConceptId());
        }
        assert status.getSetValues().length > 0 : "getIntSet returns an empty set";
        return status;
    }

    public I_IntSet getIntSet(ConceptSpec... concepts) throws Exception {
        I_IntSet status = termFactory.newIntSet();

        for (ConceptSpec concept : concepts) {
            status.add(concept.localize().getNid());
        }

        return status;
    }

    public I_IntSet getIntSet(I_GetConceptData... concepts) throws Exception {
        I_IntSet status = termFactory.newIntSet();

        for (I_GetConceptData concept : concepts) {
            status.add(concept.getConceptId());
        }
        assert status.getSetValues().length > 0 : "getIntSet returns an empty set";
        return status;
    }

    protected <T> T assertExactlyOne(Collection<T> collection) {
        assert collection.size() == 1 : "Exactly one element expected, encountered " + collection;

        return collection.iterator().next();
    }

    public void addToNestedSet(Map<Integer, ClosestDistanceHashSet> nestedList,
            ConceptRefsetInclusionDetails conceptDetails, Integer refset) {
        ClosestDistanceHashSet conceptsInRefset = nestedList.get(refset);
        if (conceptsInRefset == null) {
            conceptsInRefset = new ClosestDistanceHashSet();
            nestedList.put(refset, conceptsInRefset);
        }
        conceptsInRefset.add(conceptDetails);
    }

    public I_GetConceptData getMemberSetConcept(int refsetId) throws Exception {
        I_IntSet activeStatusIntSet = 
            getIntSet(ArchitectonicAuxiliary.Concept.CURRENT, ArchitectonicAuxiliary.Concept.ACTIVE);
        I_IntSet generatesRelIntSet = getIntSet(ConceptConstants.GENERATES_REL);

        I_GetConceptData memberSetSpecConcept = assertOneOrNone(getConcept(refsetId).getSourceRelTargets(activeStatusIntSet,
            generatesRelIntSet, null, false));
        return memberSetSpecConcept;
    }

    protected <T> T assertOneOrNone(Collection<T> collection) {
        assert collection.size() <= 1 : "Exactly one element expected, encountered " + collection;

        if (collection.size() == 1) {
            return collection.iterator().next();
        } else {
            return null;
        }
    }

    /**
     * Retires the latest version of a specified extension.
     * 
     * @param extensionPart The extension to check.
     * @throws Exception
     */
    public void retireLatestExtension(I_ThinExtByRefVersioned extensionPart) throws Exception {

        if (extensionPart != null) {
            I_ThinExtByRefPart latestVersion = getLatestVersion(extensionPart);

            I_ThinExtByRefPart clone = latestVersion.duplicatePart();
            clone.setStatus(retiredConceptId);
            clone.setVersion(Integer.MAX_VALUE);
            extensionPart.addVersion(clone);

            termFactory.addUncommitted(extensionPart);
        }

    }

    public I_ConceptAttributeTuple getLatestAttribute(List<I_ConceptAttributeTuple> atts) {
        I_ConceptAttributeTuple latest = null;
        for (I_ConceptAttributeTuple att : atts) {
            if (latest == null) {
                latest = att;
            } else {
                if (latest.getVersion() < att.getVersion()) {
                    latest = att;
                }
            }
        }
        return latest;
    }

    public I_ThinExtByRefPart getLatestVersion(I_ThinExtByRefVersioned ext) {
        I_ThinExtByRefPart latest = null;
        List<? extends I_ThinExtByRefPart> versions = ext.getVersions();
        for (I_ThinExtByRefPart version : versions) {

            if (latest == null) {
                latest = version;
            } else {
                if (latest.getVersion() < version.getVersion()) {
                    latest = version;
                }
            }
        }
        return latest;
    }

    /**
     * Adds a particular concept to the member set.
     * 
     * @param conceptId the concept id of the concept we wish to add to the
     *            member set.
     * @param includeTypeConceptId
     * @throws Exception
     */
    public void addToMemberSet(int conceptId, int includeTypeConceptId, int memberSetId) throws Exception {
        I_ThinExtByRefVersioned ext = getExtensionForComponent(conceptId, memberSetId, getMembershipType(includeTypeConceptId));
        if (ext != null) {

            I_ThinExtByRefPart clone = getLatestVersion(ext).duplicatePart();
            I_ThinExtByRefPartConcept conceptClone = (I_ThinExtByRefPartConcept) clone;
            conceptClone.setPathId(pathConcept.getConceptId());
            conceptClone.setConceptId(getMembershipType(includeTypeConceptId));
            conceptClone.setStatus(currentStatusId);
            conceptClone.setVersion(Integer.MAX_VALUE);
            ext.addVersion(conceptClone);
            termFactory.addUncommitted(ext);

        } else {
            int memberId = termFactory.uuidToNativeWithGeneration(UUID.randomUUID(),
                ArchitectonicAuxiliary.Concept.UNSPECIFIED_UUID.localize().getNid(), termFactory.getPaths(),
                Integer.MAX_VALUE);

            I_ThinExtByRefVersioned newExtension = termFactory.newExtension(memberSetId, memberId, conceptId, typeId);

            I_ThinExtByRefPartConcept conceptExtension = termFactory.newConceptExtensionPart();

            conceptExtension.setPathId(pathConcept.getConceptId());
            conceptExtension.setStatus(currentStatusId);
            conceptExtension.setVersion(Integer.MAX_VALUE);
            conceptExtension.setConceptId(getMembershipType(includeTypeConceptId));

            newExtension.addVersion(conceptExtension);

            termFactory.addUncommitted(newExtension);
        }
    }

    /**
     * Adds a particular concept to the member set.
     * 
     * @param conceptId the concept id of the concept we wish to add to the
     *            member set.
     * @param includeTypeConceptId
     * @throws Exception
     */
    public void addToMemberSetAsParent(int conceptId, int memberSetId) throws Exception {

        I_ThinExtByRefVersioned ext = 
            getExtensionForComponent(conceptId, memberSetId, ConceptConstants.PARENT_MARKER.localize().getNid());
        
        if (ext != null) {
            I_ThinExtByRefPart clone = getLatestVersion(ext).duplicatePart();
            I_ThinExtByRefPartConcept conceptClone = (I_ThinExtByRefPartConcept) clone;
            conceptClone.setPathId(pathConcept.getConceptId());
            conceptClone.setConceptId(ConceptConstants.PARENT_MARKER.localize().getNid());
            conceptClone.setStatus(currentStatusId);
            conceptClone.setVersion(Integer.MAX_VALUE);
            ext.addVersion(conceptClone);
            termFactory.addUncommitted(ext);

        } else {

            int memberId = termFactory.uuidToNativeWithGeneration(UUID.randomUUID(),
                ArchitectonicAuxiliary.Concept.UNSPECIFIED_UUID.localize().getNid(), termFactory.getPaths(),
                Integer.MAX_VALUE);

            I_ThinExtByRefVersioned newExtension = termFactory.newExtension(memberSetId, memberId, conceptId, typeId);

            I_ThinExtByRefPartConcept conceptExtension = termFactory.newConceptExtensionPart();

            conceptExtension.setPathId(pathConcept.getConceptId());
            conceptExtension.setStatus(currentStatusId);
            conceptExtension.setVersion(Integer.MAX_VALUE);
            conceptExtension.setConceptId(ConceptConstants.PARENT_MARKER.localize().getNid());

            newExtension.addVersion(conceptExtension);
            termFactory.addUncommitted(newExtension);

        }

    }

    protected I_ThinExtByRefVersioned getExtensionForComponent(int conceptId, int refset, int extType) throws IOException {

        List<I_ThinExtByRefVersioned> exts = termFactory.getAllExtensionsForComponent(conceptId);
        I_ThinExtByRefVersioned candidate = null; 
        for (I_ThinExtByRefVersioned ext : exts) {
            if (ext.getRefsetId() == refset) {
                // make sure the extension is not retired
                I_ThinExtByRefPartConcept latestVersion = (I_ThinExtByRefPartConcept) getLatestVersion(ext);
                if ((latestVersion.getC1id() == extType) && 
                    (latestVersion.getStatusId() != retiredConceptId)) {
                    if (candidate != null) {
                        // cannot handle more than one (we already have a candidate)
                        throw new TerminologyRuntimeException( 
                            "Found more than one active extension for refset '" +
                            conceptToString(refset) + "' on concept '" + conceptToString(conceptId) + "'.");
                    }
                    candidate = ext;
                }
            }
        }
        return candidate;
    }

    /**
     * Get the target of a particular type of source relation on a concept.
     * The source relationship must be current and there must be only one of
     * that type present.
     */
    public int getRelTypeTarget(int conceptId, ConceptSpec relType) throws Exception {
        I_GetConceptData concept = getConcept(conceptId);

        List<I_RelTuple> srcRels = concept.getSourceRelTuples(null, getIntSet(relType), null, false, true);

        // ConceptId, Version
        HashMap<Integer, Integer> targets = new HashMap<Integer, Integer>();
        
        for (I_RelTuple srcRel : srcRels) {
            int srcRelTargetId = srcRel.getC2Id();
            int srcRelVersion = srcRel.getVersion();
            if (targets.containsKey(srcRelTargetId)) {
                int latestVersion = targets.get(srcRelTargetId);
                if (srcRel.getVersion() > latestVersion) {
                    if (isActiveStatus(srcRel.getStatusId())) {
                        targets.put(srcRelTargetId, latestVersion);
                    } else {
                        targets.remove(srcRelTargetId);
                    }
                }
            } else {
                if (isActiveStatus(srcRel.getStatusId())) {
                    targets.put(srcRelTargetId, srcRelVersion);
                }
            }
        }
        
        if (targets.size() == 0) {
            throw new TerminologyException("A current source relationship of type '" + relType.getDescription()
                + "' was not found for concept " + concept.getId().getUIDs().iterator().next());
        }

        if (targets.size() > 1) {
            System.out.println("More than one current source relationship of type '" + relType.getDescription()
                + "' was found for concept " + concept.getId().getUIDs().iterator().next());
        }

        int latestVersion = 0;
        int latestTargetId = ID_NOT_FOUND;
        for (Integer targetId : targets.keySet()) {
            int version = targets.get(targetId);
            if ((version > latestVersion) || (latestTargetId == ID_NOT_FOUND)) {
                latestTargetId = targetId;
                latestVersion = version;
            }
        }
        
        return latestTargetId;
    }

    public int getMembershipType(int includeTypeConceptId) throws Exception {
        return getRelTypeTarget(includeTypeConceptId, ConceptConstants.CREATES_MEMBERSHIP_TYPE);
    }

    public int getExcludeMembersRefset(int specRefsetConceptId) {
        try {
            return getRelTypeTarget(specRefsetConceptId, ConceptConstants.EXCLUDE_MEMBERS_REL_TYPE);
        } catch (Exception ex) {
            return ID_NOT_FOUND;
        }
    }

    public I_GetConceptData getPathConcept() {
        return pathConcept;
    }

    public void setPathConcept(I_GetConceptData pathConcept) {
        this.pathConcept = pathConcept;
    }

    public I_GetConceptData getConcept(int id) throws TerminologyException, IOException {
//        I_GetConceptData concept = conceptCache.get(id);
//        if (concept == null) {
//            concept = termFactory.getConcept(id);
//            conceptCache.put(id, concept);
//        }
//        return concept;
        
        return termFactory.getConcept(id);
    }

    public void setAltIsA(I_GetConceptData altIsA) {
        this.altIsA = altIsA;
    }

    public String getConceptName(int id) throws TerminologyException, IOException {
        StringBuffer name = new StringBuffer();
        I_GetConceptData conceptData = getConcept(id);
        name.append("Concept[").append(conceptData.getUids().iterator().next());
        name.append(",\"").append(conceptData).append("\"]");
        return name.toString();
    }
    
    private String conceptToString(int id) {
        try {
            return termFactory.getConcept(id).getInitialText();
        } catch (Exception e) {
            return Integer.toString(id);
        }
    }
    
}
