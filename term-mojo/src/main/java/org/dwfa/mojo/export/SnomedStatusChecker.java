/*
 *  Copyright 2010 International Health Terminology Standards Development
 * Organisation.
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
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.tapi.TerminologyException;

/**
 * {@code SnomedStatusChecker} is an implementation of {@link StatusChecker} that performs status related functions for
 * SCT-AU.
 * @author Matthew Edwards
 */
public final class SnomedStatusChecker implements StatusChecker {

    /** The Terminology Factory for accessing Concept information.*/
    private final I_TermFactory termFactory;
    /** The Active Concept for RF2.*/
    private final I_GetConceptData rf2ActiveConcept;
    /** The active concept. */
    private final I_GetConceptData activeConcept;
    /** The Current concept. */
    private final I_GetConceptData currentConcept;
    /** The Pending Move Concept.*/
    private final I_GetConceptData pendingMove;
    /** The Concept denoting a retired concept.*/
    private final I_GetConceptData conceptRetired;
    /** Native ID For the Moved Elsewhere status in ace.*/
    private final int aceMovedElsewhereStatusNId;

    /**
     * Constructs an instance of {@code SnomedStatusChecker} with the specified {@code rf2ActiveConcept},
     * {@code activeConcept}, {@code currentConcept}, {@code pendingMove} concept, {@code conceptRetired} concept,
     * {@code aceMovedElsewhereStatusNId} and the default {@link I_TermFactory} as returned by
     * {@code LocalVersionedTerminology.get()}.
     * @param rf2ActiveConcept An instance of {@link I_GetConceptData} that denotes an RF2 active concept.
     * @param activeConcept An instance of {@link I_GetConceptData} that denotes an active concept.
     * @param currentConcept An instance of {@link I_GetConceptData} that denotes a current concept.
     * @param pendingMove An instance of {@link I_GetConceptData} that denotes a concept that is about to be moved.
     * @param conceptRetired An instance of {@link I_GetConceptData} that denotes a retired concept.
     * @param aceMovedElsewhereStatusNId Native ID For the Moved Elsewhere status in ace
     */
    public SnomedStatusChecker(final I_GetConceptData rf2ActiveConcept, final I_GetConceptData activeConcept,
            final I_GetConceptData currentConcept, final I_GetConceptData pendingMove,
            final I_GetConceptData conceptRetired, final int aceMovedElsewhereStatusNId) {
        this(rf2ActiveConcept, activeConcept, currentConcept, pendingMove, conceptRetired, aceMovedElsewhereStatusNId,
                LocalVersionedTerminology.get());
    }

    /**
     * Constructs an instance of {@code SnomedStatusChecker} with the specified {@code rf2ActiveConcept},
     * {@code activeConcept}, {@code currentConcept}, {@code pendingMove} concept, {@code conceptRetired} concept,
     * {@code aceMovedElsewhereStatusNId} and {@link I_TermFactory}.
     *
     * @param rf2ActiveConcept An instance of {@link I_GetConceptData} that denotes an RF2 active concept.
     * @param activeConcept An instance of {@link I_GetConceptData} that denotes an active concept.
     * @param currentConcept An instance of {@link I_GetConceptData} that denotes a current concept.
     * @param pendingMove An instance of {@link I_GetConceptData} that denotes a concept that is about to be moved.
     * @param conceptRetired An instance of {@link I_GetConceptData} that denotes a retired concept.
     * @param aceMovedElsewhereStatusNId Native ID For the Moved Elsewhere status in ace
     * @param termFactory An instance of {@link I_TermFactory} to access Concept data from.
     */
    public SnomedStatusChecker(final I_GetConceptData rf2ActiveConcept, final I_GetConceptData activeConcept,
            final I_GetConceptData currentConcept, final I_GetConceptData pendingMove,
            final I_GetConceptData conceptRetired, final int aceMovedElsewhereStatusNId,
            final I_TermFactory termFactory) {
        this.rf2ActiveConcept = rf2ActiveConcept;
        this.activeConcept = activeConcept;
        this.currentConcept = currentConcept;
        this.pendingMove = pendingMove;
        this.conceptRetired = conceptRetired;
        this.aceMovedElsewhereStatusNId = aceMovedElsewhereStatusNId;
        this.termFactory = termFactory;
    }

    @Override
    public boolean isActive(final int statusNid) throws IOException, TerminologyException {
        boolean activate = false;
        I_GetConceptData statusConcept = termFactory.getConcept(statusNid);

        if (rf2ActiveConcept.isParentOf(statusConcept, null, null, null, false)) {
            activate = true;
        } else if (rf2ActiveConcept.getNid() == statusConcept.getNid()) {
            activate = true;
        } else if (activeConcept.getNid() == statusConcept.getNid()) {
            activate = true;
        } else if (currentConcept.getNid() == statusConcept.getNid()) {
            activate = true;
        }
        return activate;
    }

    @Override
    public boolean isDescriptionActive(final int statusNid) {
        boolean isActive = false;
        if (activeConcept.getNid() == statusNid) {
            isActive = true;
        } else if (currentConcept.getNid() == statusNid) {
            isActive = true;
        } else if (pendingMove.getNid() == statusNid) {
            isActive = true;
        } else if (conceptRetired.getNid() == statusNid) {
            isActive = true;
        } else if (aceMovedElsewhereStatusNId == statusNid) {
            isActive = true;
        }
        return isActive;
    }
}
