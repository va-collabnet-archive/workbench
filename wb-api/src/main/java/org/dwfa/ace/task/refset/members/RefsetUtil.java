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
package org.dwfa.ace.task.refset.members;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.dwfa.ace.api.I_ConceptAttributePart;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.ebr.I_ExtendByRefPart;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.tapi.TerminologyException;

public interface RefsetUtil {

    I_ConceptAttributePart getLastestAttributePart(I_GetConceptData refsetConcept) throws IOException;

    I_IntSet createIntSet(I_TermFactory termFactory, Collection<UUID> uuid) throws Exception;

    I_ExtendByRefPart getLatestVersion(I_ExtendByRef ext, I_TermFactory termFactory)
            throws TerminologyException, IOException;

    String getSnomedId(int nid, I_TermFactory termFactory) throws Exception;

    <T> T assertExactlyOne(Collection<T> collection);

    I_ExtendByRefPart getLatestVersionIfCurrent(I_ExtendByRef ext, I_TermFactory termFactory)
            throws Exception;

    int getLocalizedParentMarkerNid();

    int getLocalizedConceptExtensionNid() throws Exception;

    int getLocalizedCurrentConceptNid() throws Exception;

    List<? extends I_DescriptionTuple> getDescriptionTuples(final I_GetConceptData concept, I_IntSet allowedStatuses,
            I_IntSet allowedTypes) throws Exception;

    I_IntSet createCurrentStatus(I_TermFactory termFactory) throws Exception;

    I_IntSet createFullySpecifiedName(I_TermFactory termFactory) throws Exception;

    I_IntSet createPreferredTerm(I_TermFactory termFactory) throws Exception;

    List<? extends I_DescriptionTuple> getFSNDescriptionsForConceptHavingCurrentStatus(I_TermFactory termFactory, int conceptId)
            throws Exception;

    List<? extends I_DescriptionTuple> getPTDescriptionsForConceptHavingCurrentStatus(I_TermFactory termFactory, int conceptId)
            throws Exception;
}
