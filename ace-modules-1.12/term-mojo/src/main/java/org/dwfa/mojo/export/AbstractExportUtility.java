/*
 * Copyright (c) 2009 International Health Terminology Standards Development
 * Organisation.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */
package org.dwfa.mojo.export;

import java.io.IOException;
import java.text.ParseException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.LineageHelper;
import org.dwfa.ace.util.TupleVersionPart;
import org.dwfa.mojo.PositionDescriptor;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.AceDateFormat;
import org.dwfa.vodb.bind.ThinVersionHelper;

/**
 * {@code AbstractExportUtility} is an abstract implementation of {@link DatabaseExportUtility} that contains common
 * export functions for use within DatabaseExport.
 * @author Matthew Edwards
 */
public abstract class AbstractExportUtility implements DatabaseExportUtility {


    @Override
    public final void addUuidToReleaseDateMap(final Map<UUID, Map<UUID, Date>> releasePathDateMap,
            final UUID pathUuid, final PositionDescriptor releasePosition) throws ParseException {
        if (!releasePathDateMap.containsKey(pathUuid)) {
            Map<UUID, Date> mappedModuleDate = new HashMap<UUID, Date>(1);
            releasePathDateMap.put(pathUuid, mappedModuleDate);
            mappedModuleDate.put(UUID.fromString(releasePosition.getPath().getUuid()),
                    AceDateFormat.getVersionHelperDateFormat().parse(releasePosition.getTimeString()));
        }
    }

    @Override
    public final Collection<I_GetConceptData> getOriginDescendents(final PositionDescriptor... exportOrigin)
            throws Exception {
        final LineageHelper lineage = new LineageHelper();
        final Set<I_GetConceptData> descendents = new HashSet<I_GetConceptData>();
        for (PositionDescriptor pd : exportOrigin) {
            descendents.addAll(lineage.getAllDescendants(pd.getPath().getVerifiedConcept()));
        }
        return descendents;
    }

    @Override
    public final Date getTimePoint(final I_GetConceptData conceptData) throws IOException, TerminologyException {
        final long timeLong = ThinVersionHelper.convert(
                TupleVersionPart.getLatestPart(conceptData.getConceptAttributeTuples(false)).getVersion());

        return new Date(timeLong);
    }

    @Override
    public final void addComponentTuplesToMaps(final I_GetConceptData concept,
            final Collection<I_ConceptAttributeTuple> matchingConceptTuples,
            final Collection<I_DescriptionTuple> matchingDescriptionTuples,
            final Collection<I_RelTuple> matchingRelationshipTuples, final Position position) throws IOException,
            TerminologyException {
        matchingConceptTuples.addAll(
                position.getMatchingTuples(concept.getConceptAttributeTuples(null, null, false, false)));

        matchingDescriptionTuples.addAll(
                position.getMatchingTuples(concept.getDescriptionTuples(null, null, null, false)));

        matchingRelationshipTuples.addAll(
                position.getMatchingTuples(concept.getSourceRelTuples(null, null, null, false, false)));
    }
}
