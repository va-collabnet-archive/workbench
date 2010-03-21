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
import org.dwfa.ace.api.I_IdVersioned;
import org.dwfa.ace.api.I_ImageTuple;
import org.dwfa.ace.api.I_ImageVersioned;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_ManageConflict;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.ebr.I_ThinExtByRefTuple;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.tapi.TerminologyException;

public abstract class ConflictManagementStrategy implements I_ManageConflict {

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
     * @see org.dwfa.ace.api.I_ManageConflict#isInConflict(org.dwfa.ace.api.I_GetConceptData,
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

        for (I_ThinExtByRefVersioned id : concept.getExtensions()) {
            if (isInConflict(id)) {
                return true;
            }
        }

        for (I_ImageVersioned image : concept.getImages()) {
            if (isInConflict(image)) {
                return true;
            }
        }

        if (isInConflict(concept.getId())) {
            return true;
        }

        return false;
    }

    public String toString() {
        return getDisplayName();
    }

    private I_ConfigAceFrame getConfig() throws TerminologyException, IOException {
        return LocalVersionedTerminology.get().getActiveAceFrameConfig();
    }

    public boolean isInConflict(I_GetConceptData concept) throws IOException, TerminologyException {
        I_ConfigAceFrame config = getConfig();

        if (isNull(concept, config)) {
            return false;
        }

        List<I_ConceptAttributeTuple> tuples = concept.getConceptAttributeTuples(config.getAllowedStatus(),
            config.getViewPositionSet());
        return doesConflictExist(tuples);
    }

    public boolean isInConflict(I_ConceptAttributeVersioned conceptAttribute) throws TerminologyException, IOException {
        I_ConfigAceFrame config = getConfig();

        if (isNull(conceptAttribute, config)) {
            return false;
        }

        return doesConflictExist(conceptAttribute.getTuples(config.getAllowedStatus(), config.getViewPositionSet()));
    }

    public boolean isInConflict(I_IdVersioned id) throws IOException {
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

        image.addTuples(config.getAllowedStatus(), null, config.getViewPositionSet(), matchingTuples);

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
            config.getViewPositionSet(), matchingTuples, true);

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
            config.getViewPositionSet(), matchingTuples, true);

        return doesConflictExist(matchingTuples);
    }

    public boolean isInConflict(I_ThinExtByRefVersioned extension) throws TerminologyException, IOException {
        I_ConfigAceFrame config = getConfig();

        if (isNull(extension, config)) {
            return false;
        }

        List<I_ThinExtByRefTuple> matchingTuples = new ArrayList<I_ThinExtByRefTuple>();

        extension.addTuples(null, null, matchingTuples, true, false);

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

    /**
     * Force implementation of equals by subclasses so that the conflict panel
     * drop box can refer to them
     * simply by generic instance
     */
    @Override
    public abstract boolean equals(Object o);

    /**
     * Because equals must be implemented, so must hashCode
     */
    @Override
    public abstract int hashCode();
}
