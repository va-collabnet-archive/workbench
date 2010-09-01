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
import java.util.Map;
import java.util.UUID;
import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.mojo.PositionDescriptor;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.AceDateFormat;

/**
 *  {@code DatabaseExportUtility} is a utility class for performing DatabaseExport Functions.
 * @author Matthew Edwards
 */
public interface DatabaseExportUtility {

    /**
     * Utility method to add the Path {@code Uuid} to the Release Path Date Map if it does not already exist in the Map.
     * @param releasePathDateMap the map containing already released path {@code Uuid}s.
     * @param pathUuid the path {@code UUID} to add to the map.
     * @param releasePostion the target path for the release.
     * @throws ParseException if the release position Time String cannot be parsed to an Ace Date Format.
     * @see AceDateFormat#getVersionHelperDateFormat()
     */
    void addUuidToReleaseDateMap(Map<UUID, Map<UUID, Date>> releasePathDateMap,
            UUID pathUuid, PositionDescriptor releasePosition) throws ParseException;

    /**
     * Utility Method to return the latest {@link Position} for an instance of {@link I_GetConceptData}.
     * @param conceptData the {@link I_GetConceptData} to get the latest position from.
     * @param termFactory the {@link I_TermFactory} for accessing Terminology data.
     * @return a new instance of {@link Position} representing the latest position for the {@link I_GetConceptData}.
     * @throws Exception if there are any errors returning the latest position.
     */
    Position getLatestPosition(I_GetConceptData conceptData, I_TermFactory termFactory) throws Exception;

    /**
     * Utility Method to get the Descendent Concepts from a anyone of a number of Export Origins.
     * @param exportOrigin the origin paths to export data from.
     * @return a collection of {@link I_GetConceptData} that are descendents from any of the {@code exportOrigin}s.
     * @throws Exception if there are any errors accessing descendents from the {@code exportOrigin}.
     */
    Collection<I_GetConceptData> getOriginDescendents(PositionDescriptor... exportOrigin) throws Exception;

    /**
     * Utility Method to get the Point in time that an instance of {@link I_GetConceptData} was created.
     *
     * @param conceptData the {@link I_GetConceptData} to get the Time Point from.
     * @return a Date representing the Time the latest {@link I_GetConceptData} attribute was created.
     * @throws IOException If there are any errors contacting the Terminology Database.
     * @throws TerminologyException If there are any errors retrieving the concept Information from Terminology
     * Database.
     */
    Date getTimePoint(I_GetConceptData conceptData) throws IOException, TerminologyException;

    /**
     * Set the matching tuples for concepts, descriptions and relationships
     *
     * @param concept I_GetConceptData
     * @param exportableConcept boolean
     * @param matchingConceptTuples Collection of I_ConceptAttributeTuple to update
     * @param matchingDescriptionTuples Collection of I_DescriptionTuple to update
     * @param matchingRelationshipTuples Collection of I_RelTuple to update
     * @param position to Export
     *
     * @return true if exportableConcept is true or there are matching concept tuples
     *
     * @throws IOException
     * @throws TerminologyException
     */
    void addComponentTuplesToMaps(I_GetConceptData concept,
            Collection<I_ConceptAttributeTuple> matchingConceptTuples,
            Collection<I_DescriptionTuple> matchingDescriptionTuples,
            Collection<I_RelTuple> matchingRelationshipTuples, Position position) throws IOException,
            TerminologyException;

}
