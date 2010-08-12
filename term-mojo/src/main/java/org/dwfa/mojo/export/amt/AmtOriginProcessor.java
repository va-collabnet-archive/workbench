/*
 *  Copyright 2010 International Health Terminology Standards Development  *  Organisation..
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

package org.dwfa.mojo.export.amt;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.mojo.PositionDescriptor;
import org.dwfa.mojo.export.AbstractOriginProcessor;
import org.dwfa.mojo.export.DatabaseExportUtility;
import org.dwfa.mojo.export.OriginProcessor;
import org.dwfa.mojo.export.Position;

/**
 *
 * @author Matthew Edwards
 */
public final class AmtOriginProcessor extends AbstractOriginProcessor implements OriginProcessor {
    /**Utility for performing DatabaseExport Functions.*/
    private final DatabaseExportUtility helper;

    public AmtOriginProcessor(I_GetConceptData currentConcept,
            PositionDescriptor releasePosition, PositionDescriptor[] originsForExport) {
        super(currentConcept, releasePosition, originsForExport);
        this.helper = new AmtExportUtility();
    }

    @Override
    public Map<UUID, Map<UUID, Date>> addOriginPositions(Map<UUID, Map<UUID, Date>> releasePathDateMap,
            List<Position> positions, PositionDescriptor[] excludedPositions)
            throws Exception {

        I_IntSet allowedStatus = termFactory.newIntSet();
        allowedStatus.add(currentConcept.getNid());

        for (I_GetConceptData descendent : helper.getOriginDescendents(originsForExport)) {
            final UUID pathUuid = descendent.getUids().get(0);
            if (!isExcludedPath(pathUuid, excludedPositions)) {
                positions.add(helper.getLatestPosition(descendent, termFactory));
                if (!descendent.isParentOf(termFactory.getConcept(pathUuid), allowedStatus, null, null, false)) {
                    helper.addUuidToReleaseDateMap(releasePathDateMap, pathUuid, releasePosition);
                }
            }
        }
        return releasePathDateMap;
    }
}
