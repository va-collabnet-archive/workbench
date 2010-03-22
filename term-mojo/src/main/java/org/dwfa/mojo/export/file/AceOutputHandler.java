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
import java.util.List;

import org.dwfa.dto.ComponentDto;
import org.dwfa.dto.Concept;
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
 * Writes out RF2 format files for both core files and reference sets.
 */
public class AceOutputHandler extends SnomedFileFormatOutputHandler {

    private AceIdentifierWriter idsFile;
    private AceConceptWriter conceptFile;
    private AceDescriptionWriter descriptionFile;
    private AceRelationshipWriter relationshipFile;

    /**
     * Constructor
     *
     * @param exportDirectory File
     * @param sctIdDbDirectory File
     * @throws IOException
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public AceOutputHandler(File exportDirectory) throws IOException, SQLException,
            ClassNotFoundException {
        super();

        exportDirectory.mkdirs();
        idsFile = new AceIdentifierWriter(new File(exportDirectory + File.separator + "ids.txt"));
        conceptFile = new AceConceptWriter(new File(exportDirectory + File.separator + "concepts.txt"));
        descriptionFile = new AceDescriptionWriter(new File(exportDirectory + File.separator + "descriptions.txt"));
        relationshipFile = new AceRelationshipWriter(new File(exportDirectory + File.separator
            + "relationships.txt"));
    }

    /**
     * @see org.dwfa.mojo.export.file.SnomedFileFormatOutputHandler#exportComponent(org.dwfa.dto.ComponentDto)
     */
    @Override
    void exportComponent(ComponentDto componentDto) throws Exception {
        conceptFile.write(getAceConceptRow(componentDto.getConceptDto()));
        idsFile.write(getAceIdentifierRows(componentDto.getConceptDto()));

        for (DescriptionDto descriptionDto : componentDto.getDescriptionDtos()) {
            descriptionFile.write(getAceDescriptionRow(descriptionDto));
            idsFile.write(getAceIdentifierRows(descriptionDto));
        }

        for (RelationshipDto relationshipDto : componentDto.getRelationshipDtos()) {
            relationshipFile.write(getAceRelationshipRow(relationshipDto));
            idsFile.write(getAceIdentifierRows(relationshipDto));
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
            aceIdentifierRow.setPathUuid(identifierDto.getPathId().toString());
            aceIdentifierRow.setPrimaryUuid(identifierDto.getConceptId().toString());
            aceIdentifierRow.setSourceId(identifierDto.getReferencedSctId().toString());
            aceIdentifierRow.setSourceSystemUuid(identifierDto.getIdentifierSchemeUuid().toString());
            aceIdentifierRow.setStatusUuid(identifierDto.getStatusId().toString());

            aceIdentifierRows.add(aceIdentifierRow);
        }

        return aceIdentifierRows;
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

        conceptRow.setConceptUuid(conceptDto.getConceptId().toString());
        conceptRow.setConceptStatusUuid(conceptDto.getStatusId().toString());
        conceptRow.setEffectiveTime(getReleaseDate(conceptDto));
        conceptRow.setIsPrimitve(getPrimitiveFlag(conceptDto));
        conceptRow.setPathUuid(conceptDto.getPathId().toString());

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
        rf2DescriptionRow.setConceptUuid(descriptionDto.getConceptId().toString());
        rf2DescriptionRow.setCasesensitivityUuid(descriptionDto.getCaseSignificanceId().toString());
        rf2DescriptionRow.setDescriptionstatusUuid(descriptionDto.getStatusId().toString());
        rf2DescriptionRow.setEffectiveTime(getReleaseDate(descriptionDto));
        rf2DescriptionRow.setLanguageUuid(descriptionDto.getLanguageId().toString());
        rf2DescriptionRow.setPathUuid(descriptionDto.getPathId().toString());
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

        relationshipRow.setRelationshipUuid(relationshipDto.getConceptId().toString());
        relationshipRow.setConceptUuid1(relationshipDto.getSourceId().toString());
        relationshipRow.setConceptUuid2(relationshipDto.getDestinationId().toString());
        relationshipRow.setCharacteristicTypeUuid(relationshipDto.getCharacteristicTypeId().toString());
        relationshipRow.setEffectiveTime(getReleaseDate(relationshipDto));
        relationshipRow.setPathUuid(relationshipDto.getPathId().toString());
        relationshipRow.setRefinabilityUuid(relationshipDto.getRefinabilityId().toString());
        relationshipRow.setRelationshipGroup(relationshipDto.getRelationshipGroupCode().toString());
        relationshipRow.setRelationshipstatusUuid(relationshipDto.getStatusId().toString());
        relationshipRow.setRelationshiptypeUuid(relationshipDto.getTypeId().toString());

        return relationshipRow;
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
     * @see org.dwfa.mojo.export.file.SnomedFileFormatOutputHandler#getReleaseDate(org.dwfa.dto.ConceptDto)
     */
    @Override
    String getReleaseDate(Concept concept) {
        return AceDateFormat.getRf2TimezoneDateFormat().format(concept.getDateTime());
    }
}
