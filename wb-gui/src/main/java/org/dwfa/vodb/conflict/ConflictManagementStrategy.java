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
package org.dwfa.vodb.conflict;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.dwfa.ace.api.I_AmPart;
import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.I_ConceptAttributeVersioned;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_Identify;
import org.dwfa.ace.api.I_ImageTuple;
import org.dwfa.ace.api.I_ImageVersioned;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_ManageContradiction;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.api.ebr.I_ExtendByRefVersion;
import org.dwfa.tapi.TerminologyException;

public abstract class ConflictManagementStrategy implements I_ManageContradiction {

    private static final long serialVersionUID = 1L;

    /**
     * Base implementation of the method that determines if a concept is in
     * conflict.
     * This method can check just the concept attributes, or if passed the
     * instruction
     * to check the dependent entities of the concept (descriptions,
     * relationships,
     * extensions etc) it will check through each.
     * 
     * @see org.dwfa.ace.api.I_ManageContradiction#isInConflict(org.dwfa.ace.api.I_GetConceptData,
     *      boolean)
     */
    public boolean isInConflict(I_GetConceptData concept, boolean includeDependentEntities) throws IOException,
            TerminologyException {

        if (isInConflict(concept)) {
            return true;
        }

        if (!includeDependentEntities) {
            return false;
        }

        for (I_DescriptionVersioned description : concept.getDescriptions()) {
            if (isInConflict(description)) {
                return true;
            }
        }

        for (I_RelVersioned relationship : concept.getSourceRels()) {
            if (isInConflict(relationship)) {
                return true;
            }
        }

        for (I_ExtendByRef id : concept.getExtensions()) {
            if (isInConflict(id)) {
                return true;
            }
        }

        for (I_ImageVersioned image : concept.getImages()) {
            if (isInConflict(image)) {
                return true;
            }
        }

        if (isInConflict(concept.getIdentifier())) {
            return true;
        }

        return false;
    }

    public String toString() {
        return getDisplayName();
    }

    private I_ConfigAceFrame getConfig() throws TerminologyException, IOException {
        return Terms.get().getActiveAceFrameConfig();
    }

    public boolean isInConflict(I_GetConceptData concept) throws IOException, TerminologyException {
        I_ConfigAceFrame config = getConfig();

        if (isNull(concept, config)) {
            return false;
        }

        List<? extends I_ConceptAttributeTuple> tuples = concept.getConceptAttributeTuples(config.getAllowedStatus(),
            config.getViewPositionSetReadOnly(), config.getPrecedence(), config.getConflictResolutionStrategy());
        return doesConflictExist(tuples);
    }

    public boolean isInConflict(I_ConceptAttributeVersioned conceptAttribute) throws TerminologyException, IOException {
        I_ConfigAceFrame config = getConfig();

        if (isNull(conceptAttribute, config)) {
            return false;
        }

        return doesConflictExist(conceptAttribute.getTuples(config.getAllowedStatus(), config.getViewPositionSet()));
    }

    public boolean isInConflict(I_Identify id) throws IOException {
        // TODO - must think of a better way to deal with IDs
        // the problem is that if the id parts are treated the same way as the
        // other entities parts then an entity with more than one identifier
        // is immediately in conflict in the default strategy, which is not
        // what is desired.
        return false;
    }

    public boolean isInConflict(I_ImageVersioned image) throws IOException, TerminologyException {
        I_ConfigAceFrame config = getConfig();

        if (isNull(image, config)) {
            return false;
        }

        List<I_ImageTuple> matchingTuples = new ArrayList<I_ImageTuple>();

        image.addTuples(config.getAllowedStatus(), null, config.getViewPositionSetReadOnly(), 
            matchingTuples, config.getPrecedence(), config.getConflictResolutionStrategy());

        return doesConflictExist(matchingTuples);
    }

    public boolean isInConflict(I_RelVersioned relationship) throws IOException, TerminologyException {
        I_ConfigAceFrame config = getConfig();

        if (isNull(relationship, config)) {
            return false;
        }

        List<I_RelTuple> matchingTuples = new ArrayList<I_RelTuple>();

        I_IntSet srcTypes = config.getSourceRelTypes();
        relationship.addTuples(config.getAllowedStatus(), srcTypes.getSetValues().length == 0 ? null : srcTypes,
            config.getViewPositionSetReadOnly(), matchingTuples, config.getPrecedence(), config.getConflictResolutionStrategy());

        return doesConflictExist(matchingTuples);
    }

    public boolean isInConflict(I_DescriptionVersioned description) throws IOException, TerminologyException {
        I_ConfigAceFrame config = getConfig();

        if (isNull(description, config)) {
            return false;
        }

        List<I_DescriptionTuple> matchingTuples = new ArrayList<I_DescriptionTuple>();

        I_IntSet descTypes = config.getDescTypes();
        description.addTuples(config.getAllowedStatus(), descTypes.getSetValues().length == 0 ? null : descTypes,
            config.getViewPositionSetReadOnly(), matchingTuples, config.getPrecedence(), config.getConflictResolutionStrategy());

        return doesConflictExist(matchingTuples);
    }

    public boolean isInConflict(I_ExtendByRef extension) throws TerminologyException, IOException {
        I_ConfigAceFrame config = getConfig();

        if (isNull(extension, config)) {
            return false;
        }

        List<I_ExtendByRefVersion> matchingTuples = new ArrayList<I_ExtendByRefVersion>();

        extension.addTuples(null, null, matchingTuples, config.getPrecedence(), config.getConflictResolutionStrategy());

        return doesConflictExist(matchingTuples);
    }

    protected abstract <T extends I_AmPart> boolean doesConflictExist(List<T> versions);

    private boolean isNull(Object... obj) {
        if (obj == null) {
            return true;
        }
        for (int i = 0; i < obj.length; i++) {
            if (obj[i] == null)
                return true;
        }
        return false;
    }
}
