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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
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
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.AceDateFormat;

/**
 * Writes out RF2 format files for both core files and reference sets.
 */
public class AceOutputHandler extends SnomedFileFormatOutputHandler {
    /** For converting to midnight UTC. */
    private Calendar aceTime = new GregorianCalendar();
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

    /**
     * Constructor
     *
     * @param exportDirectory File
     * @param sctIdDbDirectory File
     * @throws IOException
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public AceOutputHandler(File exportDirectory, Map<UUID, Map<UUID, Date>> releasePathDateMap) throws IOException, SQLException,
            ClassNotFoundException {
        super(releasePathDateMap);

        exportDirectory.mkdirs();
        idsFile = new AceIdentifierWriter(new File(exportDirectory + File.separator + "ids.txt"));
        conceptFile = new AceConceptWriter(new File(exportDirectory + File.separator + "concepts.txt"));
        descriptionFile = new AceDescriptionWriter(new File(exportDirectory + File.separator + "descriptions.txt"));
        relationshipFile = new AceRelationshipWriter(new File(exportDirectory + File.separator
            + "relationships.txt"));
        aceIdentifierCliniclFile = new AceIdentifierWriter(new File(exportDirectory + File.separator + "ids.clinical.txt"));
        aceIdentifierStructuralFile = new AceIdentifierWriter(new File(exportDirectory + File.separator + "ids.structural.txt"));
    }

    /**
     * @see org.dwfa.mojo.export.file.SnomedFileFormatOutputHandler#exportComponent(org.dwfa.dto.ComponentDto)
     */
    @Override
    void exportComponent(ComponentDto componentDto) throws Exception {
        synchronized (conceptFile) {
            conceptFile.write(getAceConceptRow(componentDto.getConceptDto()));
        }
        synchronized (idsFile) {
            idsFile.write(getAceIdentifierRows(componentDto.getConceptDto()));
        }

        for (DescriptionDto descriptionDto : componentDto.getDescriptionDtos()) {
            synchronized (descriptionFile) {
                descriptionFile.write(getAceDescriptionRow(descriptionDto));
            }
            synchronized (idsFile) {
                idsFile.write(getAceIdentifierRows(descriptionDto));
            }
        }

        for (RelationshipDto relationshipDto : componentDto.getRelationshipDtos()) {
            synchronized (relationshipFile) {
                relationshipFile.write(getAceRelationshipRow(relationshipDto));
            }
            synchronized (idsFile) {
                idsFile.write(getAceIdentifierRows(relationshipDto));
            }
        }

        for (ExtensionDto extensionDto : componentDto.getConceptExtensionDtos()) {
            writeExtensionIdentifierRow(extensionDto);
        }

        for (ExtensionDto extensionDto : componentDto.getDescriptionExtensionDtos()) {
            writeExtensionIdentifierRow(extensionDto);
        }

        for (ExtensionDto extensionDto : componentDto.getRelationshipExtensionDtos()) {
            writeExtensionIdentifierRow(extensionDto);
        }
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
    }

    /**
     * Get the Rf2IdentifierRows from the ConceptDto.
     *
     * @param conceptDto ConceptDto
     * @return List of Rf2IdentifierRow
     * @throws Exception
     */
    private List<AceIdentifierRow> getAceIdentifierRows(ConceptDto conceptDto) throws Exception {
        List<AceIdentifierRow> aceIdentifierRows = new ArrayList<AceIdentifierRow>(conceptDto.getIdentifierDtos().size());

        for (IdentifierDto identifierDto : conceptDto.getIdentifierDtos()) {
            AceIdentifierRow aceIdentifierRow = new AceIdentifierRow();
            aceIdentifierRow.setEffectiveDate(getReleaseDate(identifierDto));
            aceIdentifierRow.setPathUuid(getModuleUuid(identifierDto).toString());
            aceIdentifierRow.setPrimaryUuid(identifierDto.getConceptId().keySet().iterator().next().toString());
            aceIdentifierRow.setSourceId(identifierDto.getReferencedSctId().toString());
            aceIdentifierRow.setSourceSystemUuid(identifierDto.getIdentifierSchemeUuid().toString());
            aceIdentifierRow.setStatusUuid(identifierDto.getStatusId().toString());

            aceIdentifierRows.add(aceIdentifierRow);
        }

        return aceIdentifierRows;
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
        rf2DescriptionRow.setLanguageUuid(descriptionDto.getLanguageId().toString());
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
        relationshipRow.setRelationshipGroup(relationshipDto.getRelationshipGroupCode().toString());
        relationshipRow.setRelationshipstatusUuid(relationshipDto.getStatusId().toString());
        relationshipRow.setRelationshiptypeUuid(relationshipDto.getTypeId().toString());

        return relationshipRow;
    }

    /**
     * Write the ids for the extension for importing back into the ACE Berkeley database.
     *
     * @param extensionDto ExtensionDto
     * @throws IOException
     * @throws TerminologyException
     * @throws Exception
     */
    private void writeExtensionIdentifierRow(ExtensionDto extensionDto) throws IOException, TerminologyException,
            Exception {
        if (extensionDto.isClinical()) {
            synchronized (aceIdentifierCliniclFile) {
                aceIdentifierCliniclFile.write(getAceMemberIdentifierRow(extensionDto));
            }
        } else {
            synchronized (aceIdentifierStructuralFile) {
                aceIdentifierStructuralFile.write(getAceMemberIdentifierRow(extensionDto));
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
        synchronized (aceTime) {
            aceTime.setTime(concept);
            aceTime.setTimeZone(TimeZone.getTimeZone("UTC"));
            aceTime.set(Calendar.HOUR, 0);
            aceTime.set(Calendar.MINUTE, 0);
            aceTime.set(Calendar.SECOND, 0);
            return AceDateFormat.getRf2TimezoneDateFormat().format(aceTime.getTime());
        }
    }
}
