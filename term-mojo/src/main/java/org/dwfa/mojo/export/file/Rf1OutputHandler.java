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

import org.dwfa.dto.ComponentDto;
import org.dwfa.dto.Concept;
import org.dwfa.dto.ConceptDto;
import org.dwfa.dto.DescriptionDto;
import org.dwfa.dto.RelationshipDto;
import org.dwfa.maven.transform.SctIdGenerator.TYPE;
import org.dwfa.mojo.file.rf1.Rf1ConceptRow;
import org.dwfa.mojo.file.rf1.Rf1ConceptWriter;
import org.dwfa.mojo.file.rf1.Rf1DescriptionRow;
import org.dwfa.mojo.file.rf1.Rf1DescriptionWriter;
import org.dwfa.mojo.file.rf1.Rf1RelationshipRow;
import org.dwfa.mojo.file.rf1.Rf1RelationshipWriter;
import org.dwfa.util.AceDateFormat;

/**
 * Writes out RF2 format files for both core files and reference sets.
 */
public class Rf1OutputHandler extends SnomedFileFormatOutputHandler {

    private Rf1ConceptWriter conceptFile;
    private Rf1DescriptionWriter descriptionFile;
    private Rf1RelationshipWriter relationshipFile;

    /**
     * Constructor
     *
     * @param exportDirectory File
     * @param sctIdDbDirectory File
     * @throws IOException
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public Rf1OutputHandler(File exportDirectory, File sctIdDbDirectory) throws IOException, SQLException,
            ClassNotFoundException {
        super(sctIdDbDirectory);

        exportDirectory.mkdirs();
        conceptFile = new Rf1ConceptWriter(new File(exportDirectory + File.separator + "concepts.rf1.txt"));
        descriptionFile = new Rf1DescriptionWriter(new File(exportDirectory + File.separator + "descriptions.rf1.txt"));
        relationshipFile = new Rf1RelationshipWriter(new File(exportDirectory + File.separator
            + "relationships.rf1.txt"));
    }

    /**
     * @see org.dwfa.mojo.export.file.SnomedFileFormatOutputHandler#exportComponent(org.dwfa.dto.ComponentDto)
     */
    @Override
    void exportComponent(ComponentDto componentDto) throws Exception {
        conceptFile.write(getRf1ConceptRow(componentDto.getConceptDto()));

        for (DescriptionDto descriptionDto : componentDto.getDescriptionDtos()) {
            descriptionFile.write(getRf1DescriptionRow(descriptionDto));
        }

        for (RelationshipDto relationshipDto : componentDto.getRelationshipDtos()) {
            relationshipFile.write(getRf1RelationshipRow(relationshipDto));
        }
    }

    /**
     * Close all the files.
     *
     * @throws IOException if files cannot be closed.
     */
    public void closeFiles() throws IOException {
        conceptFile.close();
        descriptionFile.close();
        relationshipFile.close();
    }

    /**
     * Copy the details from ConceptDto to a Rf2ConceptRow
     *
     * @param conceptDto ConceptDto
     * @return Rf2ConceptRow
     * @throws Exception if cannot get a valid SCT id
     */
    private Rf1ConceptRow getRf1ConceptRow(ConceptDto conceptDto) throws Exception {
        Rf1ConceptRow conceptRow = new Rf1ConceptRow();

        conceptRow.setConceptSctId(getSctId(conceptDto).toString());
        conceptRow.setConceptStatus(conceptDto.getStatusCode());
        conceptRow.setCtv3Id(conceptDto.getCtv3Id());
        conceptRow.setSnomedId(conceptDto.getSnomedId());
        conceptRow.setFullySpecifiedName(conceptDto.getFullySpecifiedName());
        conceptRow.setIsPrimitve(getPrimitiveFlag(conceptDto));

        return conceptRow;
    }

    /**
     * Copy the details from DescriptionDto to a Rf1DescriptionRow.
     *
     * @param descriptionDto DescriptionDto
     * @return Rf1DescriptionRow
     * @throws Exception if cannot get a valid SCT id
     */
    private Rf1DescriptionRow getRf1DescriptionRow(DescriptionDto descriptionDto) throws Exception {
        Rf1DescriptionRow rf2DescriptionRow = new Rf1DescriptionRow();

        rf2DescriptionRow.setDescriptionSctId(getSctId(descriptionDto, descriptionDto.getDescriptionId()).toString());
        rf2DescriptionRow.setDescriptionStatus(descriptionDto.getStatusCode());
        rf2DescriptionRow.setConceptSctId(getSctId(descriptionDto, descriptionDto.getConceptId(), TYPE.CONCEPT).toString());
        rf2DescriptionRow.setTerm(descriptionDto.getDescription());
        rf2DescriptionRow.setInitialCapitalStatus(descriptionDto.getInitialCapitalStatusCode().toString());
        rf2DescriptionRow.setDescriptionType(descriptionDto.getDescriptionTypeCode().toString());
        rf2DescriptionRow.setLanaguageCode(descriptionDto.getLanguageCode());

        return rf2DescriptionRow;
    }

    /**
     * Copy the details from RelationshipDto to a Rf2RelationshipRow.
     *
     * @param relationshipDto RelationshipDto
     * @return Rf2RelationshipRow
     * @throws Exception if cannot get a valid SCT id
     */
    private Rf1RelationshipRow getRf1RelationshipRow(RelationshipDto relationshipDto) throws Exception {
        Rf1RelationshipRow relationshipRow = new Rf1RelationshipRow();

        relationshipRow.setRelationshipSctId(getSctId(relationshipDto, relationshipDto.getConceptId()).toString());
        relationshipRow.setSourceSctId(getSctId(relationshipDto, relationshipDto.getSourceId(), TYPE.CONCEPT).toString());
        relationshipRow.setRelationshipType(getSctId(relationshipDto, relationshipDto.getTypeId(), TYPE.CONCEPT).toString());
        relationshipRow.setDestinationSctId(getSctId(relationshipDto, relationshipDto.getDestinationId(), TYPE.CONCEPT).toString());
        relationshipRow.setCharacteristicType(relationshipDto.getCharacteristicTypeCode().toString());
        relationshipRow.setRefinability(relationshipDto.getRefinable().toString());
        relationshipRow.setRelationshipGroup(relationshipDto.getRelationshipGroupCode().toString());

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
        return AceDateFormat.getRf1DateOnlyDateFormat().format(concept.getDateTime());
    }
}
