/*
 *  Copyright 2010 International Health Terminology Standards Development
 * Organisation
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
package org.dwfa.mojo.export.file;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.dwfa.dto.ComponentDto;
import org.dwfa.dto.ConceptDto;
import org.dwfa.dto.DescriptionDto;
import org.dwfa.dto.IdentifierDto;
import org.dwfa.dto.RelationshipDto;
import org.dwfa.mojo.file.ace.AceConceptRow;
import org.dwfa.mojo.file.ace.AceConceptWriter;
import org.dwfa.mojo.file.ace.AceDescriptionRow;
import org.dwfa.mojo.file.ace.AceDescriptionWriter;
import org.dwfa.mojo.file.ace.AceIdentifierRow;
import org.dwfa.mojo.file.ace.AceIdentifierWriter;
import org.dwfa.mojo.file.ace.AceRelationshipRow;
import org.dwfa.mojo.file.ace.AceRelationshipWriter;
import org.dwfa.util.AceDateFormat;

/**
 * Writes out AMT Release Format (whatever that is) Files.
 * @author Matthew Edwards
 */
public class AmtOutputHandler extends SnomedFileFormatOutputHandler {

    /** Concept id's file. */
    private final AceIdentifierWriter idsFile;
    /** Concepts file. */
    private final AceConceptWriter conceptFile;
    /** Descriptions file. */
    private final AceDescriptionWriter descriptionFile;
    /** Relationship file. */
    private final AceRelationshipWriter relationshipFile;
    /** Concept id's file. */
    private final AceIdentifierWriter idsFileSnapShot;
    /** Concepts file. */
    private final AceConceptWriter conceptFileSnapShot;
    /** Descriptions file. */
    private final AceDescriptionWriter descriptionFileSnapShot;
    /** Relationship file. */
    private final AceRelationshipWriter relationshipFileSnapShot;

    public AmtOutputHandler(final File exportDirectory, final Map<UUID, Map<UUID, Date>> releasePathDateMap)
            throws Exception {
        super(releasePathDateMap);
        exportDirectory.mkdirs();
        File fullExportDirectory = new File(exportDirectory, "full");
        fullExportDirectory.mkdirs();
        File snapshotExportDirectory = new File(exportDirectory, "snapshot");
        snapshotExportDirectory.mkdirs();

        idsFile = new AceIdentifierWriter(new File(fullExportDirectory, "ids.txt"));
        conceptFile = new AceConceptWriter(new File(fullExportDirectory, "concepts.txt"));
        descriptionFile = new AceDescriptionWriter(new File(fullExportDirectory, "descriptions.txt"));
        relationshipFile = new AceRelationshipWriter(new File(fullExportDirectory, "relationships.txt"));

        idsFileSnapShot = new AceIdentifierWriter(new File(snapshotExportDirectory, "ids.txt"));
        conceptFileSnapShot = new AceConceptWriter(new File(snapshotExportDirectory, "concepts.txt"));
        descriptionFileSnapShot = new AceDescriptionWriter(new File(snapshotExportDirectory, "descriptions.txt"));
        relationshipFileSnapShot = new AceRelationshipWriter(new File(snapshotExportDirectory, "relationships.txt"));
    }

    @Override
    void exportComponent(final ComponentDto componentDto) throws Exception {

        boolean isNewActiveOrRetiringLiveConcept = false;
        for (ConceptDto conceptDto : componentDto.getConceptDtos()) {
            if (conceptDto.isNewActiveOrRetiringLive()) {
                isNewActiveOrRetiringLiveConcept = true;
                conceptFile.write(getAceConceptRow(conceptDto));
                if (conceptDto.isLatest()) {
                    conceptFileSnapShot.write(getAceConceptRow(conceptDto));
                }
            }
        }

        if (isNewActiveOrRetiringLiveConcept) {
            writeIdRows(componentDto.getConceptDtos());

            for (DescriptionDto descriptionDto : componentDto.getDescriptionDtos()) {
                if (descriptionDto.isNewActiveOrRetiringLive()) {
                    descriptionFile.write(getAceDescriptionRow(descriptionDto));
                    if (descriptionDto.isLatest()) {
                        descriptionFileSnapShot.write(getAceDescriptionRow(descriptionDto));
                    }
                }
            }
            writeIdRows(componentDto.getDescriptionDtos());

            for (RelationshipDto relationshipDto : componentDto.getRelationshipDtos()) {
                if (relationshipDto.isNewActiveOrRetiringLive()) {
                    relationshipFile.write(getAceRelationshipRow(relationshipDto));
                    if (relationshipDto.isLatest()) {
                        relationshipFileSnapShot.write(getAceRelationshipRow(relationshipDto));
                    }
                }
            }
            writeIdRows(componentDto.getRelationshipDtos());
        }
    }

    /**
     * Creates a UTC time from the <code>Concept</code> time, this insures that
     * the time is exported at midnight Zulu time.
     *
     * @see org.dwfa.mojo.export.file.SnomedFileFormatOutputHandler#getReleaseDateString(org.dwfa.dto.ConceptDto)
     */
    @Override
    String getReleaseDateString(final Date concept) {
        return AceDateFormat.getAceExportDateFormat().format(concept.getTime());
    }

    @Override
    void closeFiles() throws IOException {
        idsFile.close();
        conceptFile.close();
        descriptionFile.close();
        relationshipFile.close();

        idsFileSnapShot.close();
        conceptFileSnapShot.close();
        descriptionFileSnapShot.close();
        relationshipFileSnapShot.close();
    }

    /**
     * Copy the details from ConceptDto to a Rf2ConceptRow
     *
     * @param conceptDto ConceptDto
     * @return Rf2ConceptRow
     * @throws Exception if cannot get a valid SCT id
     */
    private AceConceptRow getAceConceptRow(final ConceptDto conceptDto) throws Exception {
        AceConceptRow conceptRow = new AceConceptRow();

        conceptRow.setConceptUuid(conceptDto.getConceptId().keySet().iterator().next().toString());
        conceptRow.setConceptStatusUuid(conceptDto.getStatusId().toString());
        conceptRow.setEffectiveTime(getReleaseDate(conceptDto, conceptDto.getDateTime()));
        conceptRow.setIsPrimitve(getPrimitiveFlag(conceptDto));
        conceptRow.setPathUuid(getModuleUuid(conceptDto).toString());

        return conceptRow;
    }

    /**
     * Copy the details from DescriptionDto to a Rf1DescriptionRow.
     *
     * @param descriptionDto DescriptionDto
     * @return Rf1DescriptionRow
     * @throws Exception if cannot get a valid SCT id
     */
    private AceDescriptionRow getAceDescriptionRow(final DescriptionDto descriptionDto) throws Exception {
        AceDescriptionRow rf2DescriptionRow = new AceDescriptionRow();

        rf2DescriptionRow.setDescriptionUuid(descriptionDto.getDescriptionId().toString());
        rf2DescriptionRow.setConceptUuid(descriptionDto.getConceptId().keySet().iterator().next().toString());
        rf2DescriptionRow.setInitialCapitalStatusCode(descriptionDto.getInitialCapitalStatusCode().toString());
        rf2DescriptionRow.setDescriptionstatusUuid(descriptionDto.getStatusId().toString());
        rf2DescriptionRow.setEffectiveTime(getReleaseDate(descriptionDto, descriptionDto.getDateTime()));
        rf2DescriptionRow.setLanguageCode(descriptionDto.getLanguageCode());
        rf2DescriptionRow.setPathUuid(getModuleUuid(descriptionDto).toString());
        rf2DescriptionRow.setTerm(descriptionDto.getDescription());
        rf2DescriptionRow.setDescriptiontypeUuid(descriptionDto.getTypeId().toString());

        return rf2DescriptionRow;
    }

    /**
     * Copy the details from RelationshipDto to a Rf2RelationshipRow.
     *
     * @param relationshipDto RelationshipDto
     * @return Rf2RelationshipRow
     * @throws Exception if cannot get a valid SCT id
     */
    private AceRelationshipRow getAceRelationshipRow(final RelationshipDto relationshipDto) throws Exception {
        AceRelationshipRow relationshipRow = new AceRelationshipRow();

        relationshipRow.setRelationshipUuid(relationshipDto.getConceptId().keySet().iterator().next().toString());
        relationshipRow.setConceptUuid1(relationshipDto.getSourceId().toString());
        relationshipRow.setConceptUuid2(relationshipDto.getDestinationId().keySet().iterator().next().toString());
        relationshipRow.setCharacteristicTypeUuid(relationshipDto.getCharacteristicTypeId().toString());
        relationshipRow.setEffectiveTime(getReleaseDate(relationshipDto, relationshipDto.getDateTime()));
        relationshipRow.setPathUuid(getModuleUuid(relationshipDto).toString());
        relationshipRow.setRefinabilityUuid(relationshipDto.getRefinabilityId().toString());
        relationshipRow.setRelationshipGroup(relationshipDto.getRelationshipGroup().toString());
        relationshipRow.setRelationshipstatusUuid(relationshipDto.getStatusId().toString());
        relationshipRow.setRelationshiptypeUuid(relationshipDto.getTypeId().toString());

        return relationshipRow;
    }

    /**
     * Write the unique id and snapshot id files.
     *
     * @param conceptDtos List of <? extends ConceptDto>
     * @throws Exception
     */
    private void writeIdRows(final List<? extends ConceptDto> conceptDtos) throws Exception {
        Map<UUID, Long> exportedIds = new HashMap<UUID, Long>();

        for (final ConceptDto conceptDto : conceptDtos) {
            for (final IdentifierDto identifierDto : conceptDto.getIdentifierDtos()) {
                UUID uuid = identifierDto.getConceptId().keySet().iterator().next();
                AceIdentifierRow aceIdentifierRow = new AceIdentifierRow();
                aceIdentifierRow.setEffectiveDate(getReleaseDate(identifierDto, identifierDto.getDateTime()));
                aceIdentifierRow.setPathUuid(getModuleUuid(identifierDto).toString());
                aceIdentifierRow.setPrimaryUuid(uuid.toString());
                aceIdentifierRow.setSourceId(identifierDto.getReferencedSctId().toString());
                aceIdentifierRow.setSourceSystemUuid(identifierDto.getIdentifierSchemeUuid().toString());
                aceIdentifierRow.setStatusUuid(identifierDto.getStatusId().toString());

                if (!exportedIds.containsKey(uuid)) {
                    exportedIds.put(uuid, identifierDto.getReferencedSctId());

                    idsFile.write(aceIdentifierRow);
                }

                if (identifierDto.isLatest() && conceptDto.isNewActiveOrRetiringLive()) {
                    idsFileSnapShot.write(aceIdentifierRow);
                }
            }
        }
    }

    /**
     * Gets the rf1 primitive flag for the ConceptDto
     *
     * @param conceptDto ConceptDto
     * @return String 1 or 0 1 is primitive
     */
    private String getPrimitiveFlag(final ConceptDto conceptDto) {
        return conceptDto.isPrimative() ? "1" : "0";
    }
}
