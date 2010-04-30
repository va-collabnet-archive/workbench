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
package org.dwfa.ace.task.refset.members.export;

import java.util.List;

import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.ebr.I_ExtendByRefPart;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCid;
import org.dwfa.ace.task.refset.members.RefsetUtil;

public final class RefsetTextWriterImpl implements RefsetTextWriter {

    private final RefsetUtil refsetUtil;
    private final I_TermFactory termFactory;

    public RefsetTextWriterImpl(final RefsetUtil refsetUtil, final I_TermFactory termFactory) {
        this.refsetUtil = refsetUtil;
        this.termFactory = termFactory;
    }

    public void writeRefset(final I_GetConceptData concept, final List<I_DescriptionTuple> descriptionTuples,
            final DescriptionWriter descriptionWriter, final I_ExtendByRefPart part,
            final NoDescriptionWriter noDescriptionWriter) throws Exception {

        I_GetConceptData value = termFactory.getConcept(((I_ExtendByRefPartCid) part).getC1id());

        if (descriptionTuples.isEmpty()) {
            noDescriptionWriter.write(concept);
            return;
        }

        if (value.getConceptId() != refsetUtil.getLocalizedParentMarkerNid()) {
            descriptionWriter.write(concept, descriptionTuples);
        }
    }
}
