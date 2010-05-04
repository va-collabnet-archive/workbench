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
package org.dwfa.mojo.export.file;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.dwfa.dto.ComponentDto;
import org.dwfa.dto.Concept;
import org.dwfa.dto.ConceptDto;
import org.dwfa.dto.DescriptionDto;
import org.dwfa.dto.ExtensionDto;
import org.dwfa.dto.IdentifierDto;
import org.dwfa.dto.RelationshipDto;
import org.dwfa.maven.transform.SctIdGenerator.TYPE;
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
 * Writes out RF2 format files for both core files and reference sets.
 */
public class AceOutputHandler extends SnomedFileFormatOutputHandler {
    /** Full release directory */
    private File fullExportDirectory;
    /** Snapshot release directory */
    private File snapshotExportDirectory;
    /** Concept ids file. */
    private AceIdentifierWriter idsFile;
    /** Concepts file. */
    private AceConceptWriter conceptFile;
    /** Descriptions file. */
    private AceDescriptionWriter descriptionFile;
    /** Relationship file. */
    private AceRelationshipWriter relationshipFile;
    /** Clinical refset member id file. */
    private AceIdentifierWriter aceIdentifierCliniclFile;
    /** Structural refset member id file. */
    private AceIdentifierWriter aceIdentifierStructuralFile;
    /** Concept ids file. */
    private AceIdentifierWriter idsFileSnapShot;
    /** Concepts file. */
    private AceConceptWriter conceptFileSnapShot;
    /** Descriptions file. */
    private AceDescriptionWriter descriptionFileSnapShot;
    /** Relationship file. */
    private AceRelationshipWriter relationshipFileSnapShot;
    /** Clinical refset member id file. */
    private AceIdentifierWriter aceIdentifierCliniclFileSnapShot;
    /** Structural refset member id file. */
    private AceIdentifierWriter aceIdentifierStructuralFileSnapShot;

    /**
     * Constructor
     *
     * @param exportDirectory File
     * @param sctIdDbDirectory File
     * @throws Exception
     */
    public AceOutputHandler(File exportDirectory, Map<UUID, Map<UUID, Date>> releasePathDateMap) throws Exception {
        super(releasePathDateMap);

        exportDirectory.mkdirs();

        fullExportDirectory = new File(exportDirectory.getAbsolutePath() + File.separatorChar + "full");
        fullExportDirectory.mkdirs();

        snapshotExportDirectory = new File(exportDirectory.getAbsolutePath() + File.separatorChar + "snapshot");
        snapshotExportDirectory.mkdirs();

        idsFile = new AceIdentifierWriter(new File(fullExportDirectory + File.separator + "ids.txt"));
        conceptFile = new AceConceptWriter(new File(fullExportDirectory + File.separator + "concepts.txt"));
        descriptionFile = new AceDescriptionWriter(new File(fullExportDirectory + File.separator + "descriptions.txt"));
        relationshipFile = new AceRelationshipWriter(new File(fullExportDirectory + File.separator + "relationships.txt"));
        aceIdentifierCliniclFile = new AceIdentifierWriter(new File(fullExportDirectory + File.separator + "ids.clinical.txt"));
        aceIdentifierStructuralFile = new AceIdentifierWriter(new File(fullExportDirectory + File.separator + "ids.structural.txt"));

        idsFileSnapShot = new AceIdentifierWriter(new File(snapshotExportDirectory + File.separator + "ids.txt"));
        conceptFileSnapShot = new AceConceptWriter(new File(snapshotExportDirectory + File.separator + "concepts.txt"));
        descriptionFileSnapShot = new AceDescriptionWriter(new File(snapshotExportDirectory + File.separator + "descriptions.txt"));
        relationshipFileSnapShot = new AceRelationshipWriter(new File(snapshotExportDirectory + File.separator + "relationships.txt"));
        aceIdentifierCliniclFileSnapShot = new AceIdentifierWriter(new File(snapshotExportDirectory + File.separator + "ids.clinical.txt"));
        aceIdentifierStructuralFileSnapShot = new AceIdentifierWriter(new File(snapshotExportDirectory + File.separator + "ids.structural.txt"));
    }

    /**
     * @see org.dwfa.mojo.export.file.SnomedFileFormatOutputHandler#exportComponent(org.dwfa.dto.ComponentDto)
     */
    @Override
    void exportComponent(ComponentDto componentDto) throws Exception {
        boolean isNewActiveOrRetiringLiveConcept = false;
        for (ConceptDto conceptDto : componentDto.getConceptDtos()) {
            if (conceptDto.isNewActiveOrRetiringLive()) {
                isNewActiveOrRetiringLiveConcept = true;
                synchronized (conceptFile) {
                    conceptFile.write(getAceConceptRow(conceptDto));
                }
                if(conceptDto.isLatest()){
                    synchronized (conceptFileSnapShot) {
                        conceptFileSnapShot.write(getAceConceptRow(conceptDto));
                    }
                }
            }
        }
        writeIdRows(componentDto.getConceptDtos());

        for (DescriptionDto descriptionDto : componentDto.getDescriptionDtos()) {
            if (isNewActiveOrRetiringLiveConcept && descriptionDto.isNewActiveOrRetiringLive()) {
                synchronized (descriptionFile) {
                    descriptionFile.write(getAceDescriptionRow(descriptionDto));
                }
                if(descriptionDto.isLatest()){
                    synchronized (descriptionFileSnapShot) {
                        descriptionFileSnapShot.write(getAceDescriptionRow(descriptionDto));
                    }
                }
            }
        }
        writeIdRows(componentDto.getDescriptionDtos());

        for (RelationshipDto relationshipDto : componentDto.getRelationshipDtos()) {
            if (isNewActiveOrRetiringLiveConcept && relationshipDto.isNewActiveOrRetiringLive()) {
                synchronized (relationshipFile) {
                    relationshipFile.write(getAceRelationshipRow(relationshipDto));
                }
                if(relationshipDto.isLatest()){
                    synchronized (relationshipFileSnapShot) {
                        relationshipFileSnapShot.write(getAceRelationshipRow(relationshipDto));
                    }
                }
            }
        }
        writeIdRows(componentDto.getRelationshipDtos());

        writeExtensionIdRows(componentDto.getConceptExtensionDtos());
        writeExtensionIdRows(componentDto.getDescriptionExtensionDtos());
        writeExtensionIdRows(componentDto.getRelationshipExtensionDtos());
    }

    /**
     * Close all the files.
     *
     * @throws IOException if files cannot be closed.
     */
    public void closeFiles() throws IOException {
        idsFile.close();
        conceptFile.close();
        descriptionFile.close();
        relationshipFile.close();
        aceIdentifierCliniclFile.close();
        aceIdentifierStructuralFile.close();

        idsFileSnapShot.close();
        conceptFileSnapShot.close();
        descriptionFileSnapShot.close();
        relationshipFileSnapShot.close();
        aceIdentifierCliniclFileSnapShot.close();
        aceIdentifierStructuralFileSnapShot.close();
    }

    /**
     * Sets the member sctid, if one exists otherwise one is generated.
     *
     * @param extensionDto ExtensionDto
     * @return AceIdentifierRow
     * @throws Exception
     */
    private AceIdentifierRow getAceMemberIdentifierRow(ExtensionDto extensionDto) throws Exception {
        AceIdentifierRow aceIdentifierRow = new AceIdentifierRow();

        aceIdentifierRow.setEffectiveDate(getReleaseDate(extensionDto));
        aceIdentifierRow.setPathUuid(getModuleUuid(extensionDto).toString());
        aceIdentifierRow.setPrimaryUuid(extensionDto.getMemberId().toString());
        if (!extensionDto.getIdentifierDtos().isEmpty()) {
            aceIdentifierRow.setSourceId(
                extensionDto.getIdentifierDtos().get(0).getReferencedSctId().toString());
        } else {
            aceIdentifierRow.setSourceId(
                getSctId(extensionDto, extensionDto.getMemberId(), TYPE.REFSET).toString());
        }
        aceIdentifierRow.setSourceSystemUuid(AceIdentifierRow.SCT_ID_IDENTIFIER_SCHEME.toString());
        aceIdentifierRow.setStatusUuid(extensionDto.getStatusId().toString());

        return aceIdentifierRow;
    }


    /**
     * Copy the details from ConceptDto to a Rf2ConceptRow
     *
     * @param conceptDto ConceptDto
     * @return Rf2ConceptRow
     * @throws Exception if cannot get a valid SCT id
     */
    private AceConceptRow getAceConceptRow(ConceptDto conceptDto) throws Exception {
        AceConceptRow conceptRow = new AceConceptRow();

        conceptRow.setConceptUuid(conceptDto.getConceptId().keySet().iterator().next().toString());
        conceptRow.setConceptStatusUuid(conceptDto.getStatusId().toString());
        conceptRow.setEffectiveTime(getReleaseDate(conceptDto));
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
    private AceDescriptionRow getAceDescriptionRow(DescriptionDto descriptionDto) throws Exception {
        AceDescriptionRow rf2DescriptionRow = new AceDescriptionRow();

        rf2DescriptionRow.setDescriptionUuid(descriptionDto.getDescriptionId().toString());
        rf2DescriptionRow.setConceptUuid(descriptionDto.getConceptId().keySet().iterator().next().toString());
        rf2DescriptionRow.setCasesensitivityUuid(descriptionDto.getCaseSignificanceId().toString());
        rf2DescriptionRow.setDescriptionstatusUuid(descriptionDto.getStatusId().toString());
        rf2DescriptionRow.setEffectiveTime(getReleaseDate(descriptionDto));
        rf2DescriptionRow.setLanguageUuid(descriptionDto.getTypeId().toString());
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
    private AceRelationshipRow getAceRelationshipRow(RelationshipDto relationshipDto) throws Exception {
        AceRelationshipRow relationshipRow = new AceRelationshipRow();

        relationshipRow.setRelationshipUuid(relationshipDto.getConceptId().keySet().iterator().next().toString());
        relationshipRow.setConceptUuid1(relationshipDto.getSourceId().toString());
        relationshipRow.setConceptUuid2(relationshipDto.getDestinationId().keySet().iterator().next().toString());
        relationshipRow.setCharacteristicTypeUuid(relationshipDto.getCharacteristicTypeId().toString());
        relationshipRow.setEffectiveTime(getReleaseDate(relationshipDto));
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
    private void writeIdRows(List<? extends ConceptDto> conceptDtos) throws Exception {
        Map<UUID, Long> exportedIds = new HashMap<UUID, Long>();

        for (ConceptDto conceptDto : conceptDtos) {
            for (IdentifierDto identifierDto : conceptDto.getIdentifierDtos()) {
                UUID uuid  = identifierDto.getConceptId().keySet().iterator().next();
                AceIdentifierRow aceIdentifierRow = new AceIdentifierRow();
                aceIdentifierRow.setEffectiveDate(getReleaseDate(identifierDto));
                aceIdentifierRow.setPathUuid(getModuleUuid(identifierDto).toString());
                aceIdentifierRow.setPrimaryUuid(uuid.toString());
                aceIdentifierRow.setSourceId(identifierDto.getReferencedSctId().toString());
                aceIdentifierRow.setSourceSystemUuid(identifierDto.getIdentifierSchemeUuid().toString());
                aceIdentifierRow.setStatusUuid(identifierDto.getStatusId().toString());

                if (!exportedIds.containsKey(uuid)) {
                    exportedIds.put(uuid, identifierDto.getReferencedSctId());

                    synchronized (idsFile) {
                        idsFile.write(aceIdentifierRow);
                    }
                }

                if (identifierDto.isLatest() && conceptDto.isNewActiveOrRetiringLive()) {
                    synchronized (idsFileSnapShot) {
                        idsFileSnapShot.write(aceIdentifierRow);
                    }
                }
            }
        }
    }

    /**
     * Writes a row into the id file for the extension member
     *
     * Checks that the id row has not been written before to avoid duplicates.
     *
     * @param extensionDto ExtensionDto
     *
     * @throws Exception
     */
    private void writeExtensionIdRows(List<ExtensionDto> extensionDtos) throws Exception {
        Collections.sort(extensionDtos);
        ExtensionDto lastExtensionDto = null;
        for (ExtensionDto extensionDto : extensionDtos) {
            if (extensionDto.isNewActiveOrRetiringLive()) {
                if (lastExtensionDto == null || !lastExtensionDto.getMemberId().equals(extensionDto.getMemberId())) {
                    if (extensionDto.isClinical()) {
                        synchronized (aceIdentifierCliniclFile) {
                            aceIdentifierCliniclFile.write(getAceMemberIdentifierRow(extensionDto));
                        }
                        synchronized (aceIdentifierCliniclFileSnapShot) {
                            aceIdentifierCliniclFileSnapShot.write(getAceMemberIdentifierRow(extensionDto));
                        }
                    } else {
                        synchronized (aceIdentifierStructuralFile) {
                            aceIdentifierStructuralFile.write(getAceMemberIdentifierRow(extensionDto));
                        }
                        synchronized (aceIdentifierStructuralFileSnapShot) {
                            aceIdentifierStructuralFileSnapShot.write(getAceMemberIdentifierRow(extensionDto));
                        }
                    }
                }
                lastExtensionDto = extensionDto;
            }
        }
    }

    /**
     * Gets the rf1 primitive flag for the ConceptDto
     *
     * @param concept Concept
     * @return String 1 or 0 1 is active
     */
    private String getPrimitiveFlag(Concept concept) {
        return concept.isActive() ? "1" : "0";
    }

    /**
     * Creates a UTC time from the <code>Concept</code> time, this insures that
     * the time is exported at midnight Zulu time.
     *
     * @see org.dwfa.mojo.export.file.SnomedFileFormatOutputHandler#getReleaseDateString(org.dwfa.dto.ConceptDto)
     */
    @Override
    String getReleaseDateString(Date concept) {
        return AceDateFormat.getAceExportDateFormat().format(concept.getTime());

    }
}
