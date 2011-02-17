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

import java.io.IOException;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;

/**
 *
 * @author Matthew Edwards
 */
public final class SnomedExportUtility extends AbstractExportUtility implements DatabaseExportUtility {
    /** International release path. */
    private final I_GetConceptData snomedReleasePath;
    final I_TermFactory termFactory;

    public SnomedExportUtility(final I_TermFactory termFactory) throws Exception {
        this.termFactory = termFactory;
        snomedReleasePath = termFactory.getConcept(
            ArchitectonicAuxiliary.Concept.SNOMED_CORE.localize().getUids().iterator().next());
    }
    /**
     * Utility Method to return the latest {@link Position} for an instance of {@link I_GetConceptData}.
     * @param conceptData the {@link I_GetConceptData} to get the latest position from.
     * @param termFactory the {@link I_TermFactory} for accessing Terminology data.
     * @return a new instance of {@link Position} representing the latest position for the {@link I_GetConceptData}.
     * @throws Exception if there are any errors returning the latest position.
     */
    @Override
    public Position getLatestPosition(final I_GetConceptData conceptData, final I_TermFactory termFactory)
            throws Exception {
        final Position position = new Position(termFactory.getConcept(conceptData.getConceptId()),
                getTimePoint(conceptData));
        position.setLastest(true);
        return position;
    }

    /**
     * Is the path the SNOMED international path
     * @param pathNid int
     *
     * @return true if path is snomedReleasePath or child of
     *
     * @throws IOException DB errors
     * @throws TerminologyException DB errors
     */
    public boolean isInternationalPath(int pathNid) throws IOException, TerminologyException {
        boolean internationPath = false;
        I_GetConceptData pathConcept = termFactory.getConcept(pathNid);

        if (snomedReleasePath.isParentOf(pathConcept, null, null, null, false)) {
            internationPath = true;
        } else if (snomedReleasePath.equals(pathConcept)) {
            internationPath = true;
        }

        return internationPath;
    }


}
