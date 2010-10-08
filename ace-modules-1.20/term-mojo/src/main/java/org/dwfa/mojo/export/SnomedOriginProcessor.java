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
package org.dwfa.mojo.export;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.refset.ConceptConstants;
import org.dwfa.ace.task.path.PromoteToPath;
import org.dwfa.mojo.ConceptDescriptor;
import org.dwfa.mojo.PositionDescriptor;
import org.dwfa.util.AceDateFormat;

/**
 *
 * @author Matthew Edwards
 */
public class SnomedOriginProcessor extends AbstractOriginProcessor implements OriginProcessor {

    /**Promotes to concepts for calculating promotion origins.*/
    private final I_GetConceptData promotesToConcept;
    /**The concept that groups all the maintained modules.*/
    private final ConceptDescriptor maintainedModuleParent;
    /**Utility for performing DatabaseExport Functions.*/
    private final DatabaseExportUtility helper;

    public SnomedOriginProcessor(final I_GetConceptData currentConcept,
            final List<PositionDescriptor> releasePositions, final PositionDescriptor[] originsForExport,
            final ConceptDescriptor maintainedModuleParent) throws Exception {
        super(currentConcept, releasePositions, originsForExport);
        this.maintainedModuleParent = maintainedModuleParent;
        this.helper = new SnomedExportUtility();
        this.promotesToConcept = termFactory.getConcept(ConceptConstants.PROMOTES_TO.localize().getNid());
    }

    @Override
    public Map<UUID, Map<UUID, Date>> addOriginPositions(Map<UUID, Map<UUID, Date>> releasePathDateMap,
            List<Position> positions, PositionDescriptor[] excludedPositions)
            throws Exception {

        if (originsForExport != null) {
            I_GetConceptData matainedModuleParent = termFactory.getConcept(UUID.fromString(maintainedModuleParent.
                    getUuid()));
            I_IntSet allowedStatus = termFactory.newIntSet();
            allowedStatus.add(currentConcept.getNid());

            I_ConfigAceFrame config = termFactory.getActiveAceFrameConfig();
            I_IntSet currentStatus = config.getAllowedStatus();
            config.setAllowedStatus(allowedStatus);

            for (PositionDescriptor positionDescriptor : originsForExport) {
            	for (I_Position iPosition : PromoteToPath.getPositionsToCopy(positionDescriptor.getPath().getVerifiedConcept(), promotesToConcept, termFactory)) {
                    UUID pathUuid = termFactory.getUids(iPosition.getPath().getConceptId()).iterator().next();
                    if (!isExcludedPath(pathUuid, excludedPositions)) {
                        Date timePoint = new Date();
                        timePoint.setTime(iPosition.getTime());
                        Position position = new Position(termFactory.getConcept(iPosition.getPath().getConceptId()),
                                timePoint);
                        position.setLastest(true);
                        positions.add(position);

                        if (!matainedModuleParent.isParentOf(termFactory.getConcept(pathUuid), allowedStatus, null, null, false)) {
                            if (!releasePathDateMap.containsKey(pathUuid)) {
                                Map<UUID, Date> mappedModuleDate = new HashMap<UUID, Date>(1);
                                releasePathDateMap.put(pathUuid, mappedModuleDate);

                                mappedModuleDate.put(UUID.fromString(positionDescriptor.getPath().getUuid()),
                                        AceDateFormat.getVersionHelperDateFormat().parse(positionDescriptor.getTimeString()));
                            }
                        }
                    }
				}
            }

            config.setAllowedStatus(currentStatus);

        }
        return releasePathDateMap;
    }
}
