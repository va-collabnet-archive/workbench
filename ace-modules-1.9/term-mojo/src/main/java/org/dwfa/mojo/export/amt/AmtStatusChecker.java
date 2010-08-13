/*
 *  Copyright 2010 International Health Terminology Standards Development
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

package org.dwfa.mojo.export.amt;

import java.io.IOException;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.mojo.export.StatusChecker;
import org.dwfa.tapi.TerminologyException;

/**
 * {@code AmtStatusChecker} is an implementation of {@link StatusChecker} that performs status related functions for
 * AMT.
 * @author Matthew Edwards
 */
public final class AmtStatusChecker implements StatusChecker {
    /** The Terminology Factory for accessing Concept information.*/
    private final I_TermFactory termFactory;
    /** The active concept. */
    private final I_GetConceptData activeConcept;
    /** The Current concept. */
    private final I_GetConceptData currentConcept;
    /** The Erroneous concept. */
    private final I_GetConceptData erroneous;
    /** The Concept Retired concept. */
    private final I_GetConceptData conceptRetired;

    /**
     * Constructs an instance of {@code AmtStatusChecker} with the specified {@code activeConcept},
     * {@code currentConcept}, {@code erroneousConcept}, {@code conceptRetired} concept and the default
     * {@link I_TermFactory} as returned by {@code LocalVersionedTerminology.get()}.
     * @param activeConcept An instance of {@link I_GetConceptData} that denotes an active concept.
     * @param currentConcept An instance of {@link I_GetConceptData} that denotes a current concept.
     * @param erroneous An instance of {@link I_GetConceptData} that denotes an erroneous concept.
     * @param conceptRetired An instance of {@link I_GetConceptData} that denotes a retired concept.
     */
    public AmtStatusChecker(final I_GetConceptData activeConcept,
            final I_GetConceptData currentConcept, final I_GetConceptData erroneous,
            final I_GetConceptData conceptRetired) {
        this(activeConcept, currentConcept, erroneous, conceptRetired, LocalVersionedTerminology.get());
    }
    /**
     * Constructs an instance of {@code AmtStatusChecker} with the specified {@code activeConcept},
     * {@code currentConcept}, {@code erroneousConcept}, {@code conceptRetired}, and {@link I_TermFactory}.
     * @param activeConcept An instance of {@link I_GetConceptData} that denotes an active concept.
     * @param currentConcept An instance of {@link I_GetConceptData} that denotes a current concept.
     * @param erroneous An instance of {@link I_GetConceptData} that denotes an erroneous concept.
     * @param conceptRetired An instance of {@link I_GetConceptData} that denotes a retired concept.
     * @param termFactory An instance of {@link I_TermFactory} to access Concept data from.
     */
    public AmtStatusChecker(final I_GetConceptData activeConcept,
            final I_GetConceptData currentConcept, final I_GetConceptData erroneous,
            final I_GetConceptData conceptRetired, final  I_TermFactory termFactory) {
        this.activeConcept = activeConcept;
        this.currentConcept = currentConcept;
        this.erroneous = erroneous;
        this.conceptRetired = conceptRetired;
        this.termFactory = termFactory;
    }

    @Override
    public boolean isActive(final int statusNid) throws IOException, TerminologyException {
        boolean isActive = false;
        I_GetConceptData statusConcept = termFactory.getConcept(statusNid);
        if (activeConcept.isParentOf(statusConcept, null, null, null, false)) {
            isActive = true;
        } else if (erroneous.getNid() == statusNid) {
            isActive = true;
        }
        return isActive;
    }


    @Override
    public boolean isDescriptionActive(final int statusNid) {
        boolean isActive = false;
         if (activeConcept.getNid() == statusNid) {
            isActive = true;
        } else if (currentConcept.getNid() == statusNid) {
            isActive = true;
        } else if (erroneous.getNid() == statusNid) {
            isActive = true;
        } else if (conceptRetired.getNid() == statusNid) {
            isActive = true;
        }
        return isActive;
    }
}
