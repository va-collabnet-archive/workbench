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
package org.dwfa.ace.task.commit.validator;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.task.commit.AbstractConceptTest;

/**
 *The {@code ConceptDescriptionFacade} class encapsulates the logic to retrieve
 * the {@link I_DescriptionVersioned} instances for a given
 * {@link I_GetConceptData}
 * 
 * @author Matthew Edwards
 */
public final class ConceptDescriptionFacade {

    private final I_TermFactory termFactory;
    private final AbstractConceptTest conceptTest;

    public ConceptDescriptionFacade(final I_TermFactory termFactory, final AbstractConceptTest conceptTest) {
        this.termFactory = termFactory;
        this.conceptTest = conceptTest;
    }

    /**
     * Retrieves all Descriptions from the {@code concept} object.
     * This includes
     * <ul>
     * <li>Uncommitted Descriptions</li>
     * <li>Versioned Descriptions</li>
     * </ul>
     * 
     * @param concept I_GetConceptData the concept to retrieve the descriptions
     *            from.
     * @return List<I_DescriptionVersioned> a List of Descriptions for the
     *         concept.
     * @throws Exception when there is an error retrieving the Descriptions from
     *             the {@code concept}
     */
    public List<I_DescriptionVersioned> getAllDescriptions(final I_GetConceptData concept) throws Exception {
        Logger.getLogger(this.getClass().getName()).log(Level.FINEST,
            String.format("Getting all descriptions for %1$s", concept.getConceptId()));
        List<I_DescriptionVersioned> descriptions = new ArrayList<I_DescriptionVersioned>();
        descriptions.addAll(getVersionedDescriptions(concept));
        descriptions.addAll(getUncommittedDescriptions(concept));
        return descriptions;
    }

    /**
     * Retrieves the Uncommitted Descriptions from the {@code concept} object.
     * 
     * @param concept I_GetConceptData the concept to retrieve the uncommitted
     *            descriptions from.
     * @return List<I_DescriptionVersioned> a List of Uncommitted Descriptions
     *         for the concept.
     * @throws Exception when there is an error retrieving the Uncommitted
     *             Descriptions from the {@code concept}
     */
    public List<I_DescriptionVersioned> getUncommittedDescriptions(final I_GetConceptData concept) throws Exception {
        return concept.getUncommittedDescriptions();
    }

    /**
     * Retrieves the Version Descriptions from the {@code concept} object.
     * 
     * @param concept I_GetConceptData the concept to retrieve the versioned
     *            Descriptions from.
     * @return List<I_DescriptionVersioned> a List of Versioned Descriptions for
     *         the concept.
     * @throws Exception when there is an error retrieving the Versioned
     *             Descriptions from the {@code concept}
     */
    public List<I_DescriptionVersioned> getVersionedDescriptions(final I_GetConceptData concept) throws Exception {
        List<I_DescriptionVersioned> descriptions = new ArrayList<I_DescriptionVersioned>();
        List<I_DescriptionTuple> descriptionTuples = getDescriptionTuples(concept);
        for (I_DescriptionTuple desc : descriptionTuples) {
            descriptions.add(desc.getDescVersioned());
        }
        return descriptions;
    }

    /**
     * Retrieves the Description Tuples from the {@code concept} object.
     * 
     * @param concept I_GetConceptData the concept to retrieve the description
     *            tuples from.
     * @return List<I_DescriptionTuple> a List of Description Tuples for the
     *         concept.
     * @throws Exception when there is an error retrieving the Description
     *             Tuples from the {@code concept}
     */
    public List<I_DescriptionTuple> getDescriptionTuples(final I_GetConceptData concept) throws Exception {
        I_ConfigAceFrame activeProfile = termFactory.getActiveAceFrameConfig();
        Set<I_Position> allPositions = conceptTest.getPositions(termFactory);

        return concept.getDescriptionTuples(activeProfile.getAllowedStatus(), null, allPositions, true);
    }
}
