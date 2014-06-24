/*
 * Copyright (c) 2010 International Health Terminology Standards Development
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
package org.ihtsdo.project.refset;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.TerminologyException;

/**
 * The Class Refset.
 */
public abstract class Refset {

    /**
     * The refset concept.
     */
    protected I_GetConceptData refsetConcept;
    /**
     * The refset name.
     */
    protected String refsetName;
    /**
     * The refset id.
     */
    protected int refsetId;
    /**
     * The term factory.
     */
    protected I_TermFactory termFactory;

    /**
     * Gets the source rel target.
     *
     * @param refsetIdentityConcept the refset identity concept
     * @param config the config
     * @param relTypeNid the rel type nid
     * @return the source rel target
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws TerminologyException the terminology exception
     */
    protected static Set<? extends I_GetConceptData> getSourceRelTarget(I_GetConceptData refsetIdentityConcept,
            I_ConfigAceFrame config,
            int relTypeNid) throws IOException, TerminologyException {
        I_TermFactory tf = Terms.get();
        I_IntSet allowedTypes = tf.newIntSet();
        allowedTypes.add(relTypeNid);
//			    System.out.println("************* relTypeNid: " + relTypeNid);
        //I_GetConceptData relConcept = tf.getConcept(relTypeNid);
//			    System.out.println("************* relConcept: " + relConcept.toString());
//			    System.out.println("************* allowedTypes: " + allowedTypes);
        Set<? extends I_GetConceptData> matchingConcepts = refsetIdentityConcept.getSourceRelTargets(config.getAllowedStatus(),
                allowedTypes, config.getViewPositionSetReadOnly(), config.getPrecedence(), config.getConflictResolutionStrategy());
//			    System.out.println("************* matchingConcepts: " + matchingConcepts.size());
        return matchingConcepts;
    }

    /**
     * Instantiates a new refset.
     */
    public Refset() {
        super();
        this.termFactory = Terms.get();
    }

    /**
     * Gets the refset purpose concept.
     *
     * @param config the config
     * @return the refset purpose concept
     */
    public I_GetConceptData getRefsetPurposeConcept(I_ConfigAceFrame config) {
        try {
            I_GetConceptData refsetPurposeRel =
                    termFactory.getConcept(RefsetAuxiliary.Concept.REFSET_PURPOSE_REL.getUids());
            I_GetConceptData refsetConcept = getRefsetConcept();
            if (refsetConcept == null) {
                return null;
            }

            return getLatestSourceRelationshipTarget(refsetConcept, refsetPurposeRel, config);
        } catch (Exception e) {
            AceLog.getAppLog().alertAndLogException(e);
            return null;
        }
    }

    /**
     * Gets the refset type concept.
     *
     * @param config the config
     * @return the refset type concept
     */
    public I_GetConceptData getRefsetTypeConcept(I_ConfigAceFrame config) {
        try {
            I_GetConceptData refsetTypeRel =
                    termFactory.getConcept(RefsetAuxiliary.Concept.REFSET_TYPE_REL.getUids());
            I_GetConceptData refsetConcept = getRefsetConcept();
            if (refsetConcept == null) {
                return null;
            }

            return getLatestSourceRelationshipTarget(refsetConcept, refsetTypeRel, config);
        } catch (Exception e) {
            AceLog.getAppLog().alertAndLogException(e);
            return null;
        }
    }

    /**
     * Gets the latest specified relationship's target.
     *
     * @param concept the concept
     * @param relationshipType the relationship type
     * @param config the config
     * @return the latest source relationship target
     * @throws Exception the exception
     */
    public I_GetConceptData getLatestSourceRelationshipTarget(I_GetConceptData concept,
            I_GetConceptData relationshipType,
            I_ConfigAceFrame config)
            throws Exception {

        I_GetConceptData latestTarget = null;
        long latestVersion = Long.MIN_VALUE;

        I_IntSet allowedTypes = Terms.get().newIntSet();
        allowedTypes.add(relationshipType.getConceptNid());

        List<? extends I_RelTuple> relationships = concept.getSourceRelTuples(
                config.getAllowedStatus(), allowedTypes,
                config.getViewPositionSetReadOnly(), config.getPrecedence(), config.getConflictResolutionStrategy());
        for (I_RelTuple rel : relationships) {
            if (rel.getTime() > latestVersion) {
                latestVersion = rel.getTime();
                latestTarget = Terms.get().getConcept(rel.getC2Id());
            }
        }

        return latestTarget;
    }

    /**
     * Gets the latest specified relationship's target.
     *
     * @param concept the concept
     * @param relationshipType the relationship type
     * @param config the config
     * @return the latest destination relationship source
     * @throws Exception the exception
     */
    public I_GetConceptData getLatestDestinationRelationshipSource(I_GetConceptData concept,
            I_GetConceptData relationshipType,
            I_ConfigAceFrame config) throws Exception {

        I_GetConceptData latestSource = null;
        long latestVersion = Long.MIN_VALUE;

        I_IntSet allowedTypes = Terms.get().newIntSet();
        allowedTypes.add(relationshipType.getConceptNid());

        List<? extends I_RelTuple> relationships = concept.getDestRelTuples(
                config.getAllowedStatus(), allowedTypes,
                config.getViewPositionSetReadOnly(), config.getPrecedence(), config.getConflictResolutionStrategy());
        for (I_RelTuple rel : relationships) {
            if (rel.getTime() > latestVersion) {
                latestVersion = rel.getTime();
                latestSource = Terms.get().getConcept(rel.getC1Id());
            }
        }

        return latestSource;
    }

    /**
     * Gets the latest specified relationship's target.
     *
     * @param concept the concept
     * @param relationshipType the relationship type
     * @param config the config
     * @return the latest relationship
     * @throws Exception the exception
     */
    public I_RelTuple getLatestRelationship(I_GetConceptData concept, I_GetConceptData relationshipType,
            I_ConfigAceFrame config)
            throws Exception {

        I_RelTuple latestRel = null;
        long latestVersion = Long.MIN_VALUE;

        I_IntSet allowedTypes = Terms.get().newIntSet();
        allowedTypes.add(relationshipType.getConceptNid());

        List<? extends I_RelTuple> relationships = concept.getSourceRelTuples(
                config.getAllowedStatus(), allowedTypes,
                config.getViewPositionSetReadOnly(), config.getPrecedence(), config.getConflictResolutionStrategy());
        for (I_RelTuple rel : relationships) {
            if (rel.getTime() > latestVersion) {
                latestVersion = rel.getTime();
                latestRel = rel;
            }
        }

        return latestRel;
    }

    /**
     * Gets the refset concept.
     *
     * @return the refset concept
     */
    public I_GetConceptData getRefsetConcept() {
        if (refsetConcept == null) {
            try {
                refsetConcept = Terms.get().getConcept(refsetId);
            } catch (TerminologyException e) {
                AceLog.getAppLog().alertAndLogException(e);
            } catch (IOException e) {
                AceLog.getAppLog().alertAndLogException(e);
            }
        }
        return refsetConcept;
    }

    /**
     * Sets the refset concept.
     *
     * @param refsetConcept the new refset concept
     */
    public void setRefsetConcept(I_GetConceptData refsetConcept) {
        this.refsetConcept = refsetConcept;
    }

    /**
     * Gets the refset id.
     *
     * @return the refset id
     */
    public int getRefsetId() {
        return refsetId;
    }

    /**
     * Sets the refset id.
     *
     * @param refsetId the new refset id
     */
    public void setRefsetId(int refsetId) {
        this.refsetId = refsetId;
    }

    /**
     * Gets the refset name.
     *
     * @return the refset name
     */
    public String getRefsetName() {
        return refsetName;
    }

    /**
     * Sets the refset name.
     *
     * @param refsetName the new refset name
     */
    public void setRefsetName(String refsetName) {
        this.refsetName = refsetName;
    }
}