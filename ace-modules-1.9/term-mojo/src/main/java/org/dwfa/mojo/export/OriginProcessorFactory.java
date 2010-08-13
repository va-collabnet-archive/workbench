/*
 *  Copyright 2010 International Health Terminology Standards Development
 *  Organisation.
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

import org.dwfa.mojo.export.amt.AmtOriginProcessor;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.mojo.ConceptDescriptor;
import org.dwfa.mojo.PositionDescriptor;

/**
 * {@code OriginProcessorFactory} is a factory class for determining what type of {@link OriginProcessor} to instantiate
 * at runtime.
 * @author Matthew Edwards
 */
final class OriginProcessorFactory {

    /**Error Message for use when an illegal argument passed in.*/
    private static final String ERROR_MESSAGE = "Acceptable origin processor types are '%1$s' or '%2$s'";
    /** The active concept. */
    private final I_GetConceptData currentConcept;
    /**The release position.*/
    private final PositionDescriptor releasePosition;
    /**Origins to be Exported.*/
    private final PositionDescriptor[] originsForExport;
    /**The concept that groups all the maintained modules.*/
    private final ConceptDescriptor maintainedModuleParent;

    /**
     * Constructs an instance of {@code OriginProcessorFactory} for constructing instances of {@link OriginProcessor}.
     * @param currentConcept the {@link I_GetConceptData} that denotes a Concept is current.
     * @param releasePosition the position to release concepts to.
     * @param originsForExport the {@link I_GetConceptData} origins for export.
     * @param maintainedModuleParent the Maintained Module Parent.
     */
    OriginProcessorFactory(final I_GetConceptData currentConcept, final PositionDescriptor releasePosition,
            final PositionDescriptor[] originsForExport,
            final ConceptDescriptor maintainedModuleParent) {
        this.currentConcept = currentConcept;
        this.releasePosition = releasePosition;
        this.originsForExport = originsForExport;
        this.maintainedModuleParent = maintainedModuleParent;
    }

    /**
     * Returns an instance of {@link OriginProcessor} as determined by the {@code originProcessorType} flag. Allowable
     * types are (not case sensitive):
     * <ul>
     * <li>{@code snomed} - returns an instance of {@link SnomedOriginProcessor}</li>
     * <li>{@code amt} - returns an instance of {@link AmtOriginProcessor}</li>
     * </ul>
     * @param originProcessorType a String flag that determines what type of {@link OriginProcessor} to construct.
     * @return depending on the {@code originProcessorType} flag, this method will return:
     * <ul>
     * <li>{@code snomed} - returns an instance of {@link SnomedOriginProcessor}</li>
     * <li>{@code amt} - returns an instance of {@link AmtOriginProcessor}</li>
     * </ul>
     * @throws Exception if there are any errors constructing the {@link OriginProcessor}.
     */
    OriginProcessor getInstance(final String originProcessorType) throws Exception {
        validate(originProcessorType);

        if (originProcessorType.equalsIgnoreCase(OriginProcessorType.AMT.toString())) {
            return new AmtOriginProcessor(currentConcept, releasePosition, originsForExport);
        } else if (originProcessorType.equalsIgnoreCase(OriginProcessorType.SNOMED.toString())) {
            return new SnomedOriginProcessor(currentConcept, releasePosition, originsForExport, maintainedModuleParent);
        } else {
            throw new IllegalArgumentException(
                String.format(ERROR_MESSAGE, OriginProcessorType.AMT, OriginProcessorType.SNOMED));
        }
    }

    /**
     * Confirms that the {@code originProcessorType} flag is not null, and is a valid input value.
     * @param originProcessorType a String flag that determines what type of {@link OriginProcessor} to construct.
     */
    private void validate(final String originProcessorType) {
        if (originProcessorType == null) {
            throw new IllegalArgumentException(
                String.format(ERROR_MESSAGE, OriginProcessorType.AMT, OriginProcessorType.SNOMED));
        }

        if (!originProcessorType.equalsIgnoreCase(OriginProcessorType.AMT.toString())
                && !originProcessorType.equalsIgnoreCase(OriginProcessorType.SNOMED.toString())) {
            throw new IllegalArgumentException(
                String.format(ERROR_MESSAGE, OriginProcessorType.AMT, OriginProcessorType.SNOMED));
        }
    }

   /**
     * Signifies the OriginProcessor type to construct.
     * This is private because the input should only be a String value.
     */
    private enum OriginProcessorType {

        /** AMT Flag for creating an {@link AmtOriginProcessor}.*/
        AMT,
        /** SNOMED Flag for creating an {@link SnomedOriginProcessor}.*/
        SNOMED
    }
}
