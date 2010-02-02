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
import java.util.HashMap;
import java.util.Map;

import org.dwfa.dto.ComponentDto;
import org.dwfa.dto.ConceptDto;
import org.dwfa.dto.DescriptionDto;
import org.dwfa.dto.ExtensionDto;
import org.dwfa.dto.RelationshipDto;
import org.dwfa.mojo.file.rf2.Rf2ConceptRow;
import org.dwfa.mojo.file.rf2.Rf2ConceptWriter;
import org.dwfa.mojo.file.rf2.Rf2DescriptionRow;
import org.dwfa.mojo.file.rf2.Rf2DescriptionWriter;
import org.dwfa.mojo.file.rf2.Rf2ReferenceSetRow;
import org.dwfa.mojo.file.rf2.Rf2ReferenceSetWriter;
import org.dwfa.mojo.file.rf2.Rf2RelationshipRow;
import org.dwfa.mojo.file.rf2.Rf2RelationshipWriter;
import org.dwfa.util.AceDateFormat;

/**
 * Writes out RF2 format files for both core files and reference sets.
 */
public class Rf2OutputHandler extends SnomedFileFormatOutputHandler {

    private File exportDirectory;
    private Rf2ConceptWriter conceptFile;
    private Rf2DescriptionWriter descriptionFile;
    private Rf2RelationshipWriter relationshipFile;
    private Map<Long, Rf2ReferenceSetWriter> referenceSetFileMap;

    public Rf2OutputHandler(File exportDirectory, File SctIdDbDirectory) throws IOException, SQLException,
            ClassNotFoundException {
        super(SctIdDbDirectory);

        this.exportDirectory = exportDirectory;
        conceptFile = new Rf2ConceptWriter(new File(exportDirectory + File.separator + "concepts.rf2.txt"));
        descriptionFile = new Rf2DescriptionWriter(new File(exportDirectory + File.separator + "descriptions.rf2.txt"));
        relationshipFile = new Rf2RelationshipWriter(new File(exportDirectory + File.separator
            + "relationships.rf2.txt"));

        referenceSetFileMap = new HashMap<Long, Rf2ReferenceSetWriter>();
    }

    @Override
    void exportComponent(ComponentDto componentDto) throws Exception {
        conceptFile.write(getRf2ConceptRow(componentDto.getConceptDto()));

        for (DescriptionDto descriptionDto : componentDto.getDescriptionDtos()) {
            descriptionFile.write(getRf2DescriptionRow(descriptionDto));
        }

        for (RelationshipDto relationshipDto : componentDto.getRelationshipDtos()) {
            relationshipFile.write(getRf2RelationshipRow(relationshipDto));
        }

        for (ExtensionDto extensionDto : componentDto.getConceptExtensionDtos()) {
            getReferenceSetWriter(extensionDto).write(getRf2ExtensionRow(extensionDto));
        }

        for (ExtensionDto extensionDto : componentDto.getDescriptionExtensionDtos()) {
            getReferenceSetWriter(extensionDto).write(getRf2ExtensionRow(extensionDto));
        }

        for (ExtensionDto extensionDto : componentDto.getRelationshipExtensionDtos()) {
            getReferenceSetWriter(extensionDto).write(getRf2ExtensionRow(extensionDto));
        }
    }

    /**
     * @see java.lang.Object#finalize()
     */
    @Override
    protected void finalize() throws Throwable {
        super.finalize();

        conceptFile.close();
        descriptionFile.close();
        relationshipFile.close();

        for (Rf2ReferenceSetWriter rf2ReferenceSetWriter : referenceSetFileMap.values()) {
            rf2ReferenceSetWriter.close();
        }
    }

    /**
     * For a given refset id get the file writer.
     *
     * The refset file name is the refset name, could write logic here to get
     * the full file name or just get the assembly project to fix them.
     *
     * @param extensionDto ExtensionDto
     * @return Rf2ReferenceSetWriter
     * @throws Exception cannot get an SCT id
     */
    private Rf2ReferenceSetWriter getReferenceSetWriter(ExtensionDto extensionDto) throws Exception {
        Long sctId = getSctId(extensionDto);

        if (!referenceSetFileMap.containsKey(sctId)) {
            referenceSetFileMap.put(sctId, new Rf2ReferenceSetWriter(
                new File(exportDirectory + File.separator + sctId +  extensionDto.getFullySpecifiedName() + ".txt")));
        }

        return referenceSetFileMap.get(sctId);
    }

    /**
     * Copy the details from ConceptDto to a Rf2ConceptRow
     *
     * @param conceptDto ConceptDto
     * @return Rf2ConceptRow
     * @throws Exception if cannot get a valid SCT id
     */
    private Rf2ConceptRow getRf2ConceptRow(ConceptDto conceptDto) throws Exception {
        Rf2ConceptRow conceptRow = new Rf2ConceptRow();

        conceptRow.setConceptSctId(getSctId(conceptDto).toString());
        conceptRow.setEffectiveTime(getReleaseDate(conceptDto));
        conceptRow.setModuleSctId(getModuleId(conceptDto).toString());
        conceptRow.setDefiniationStatusSctId(getSctId(conceptDto, conceptDto.getStatus()).toString());
        conceptRow.setActive(getActiveFlag(conceptDto));

        return conceptRow;
    }

    /**
     * Copy the details from DescriptionDto to a Rf2DescriptionRow.
     *
     * @param descriptionDto DescriptionDto
     * @return Rf2DescriptionRow
     * @throws Exception if cannot get a valid SCT id
     */
    private Rf2DescriptionRow getRf2DescriptionRow(DescriptionDto descriptionDto) throws Exception {
        Rf2DescriptionRow rf2DescriptionRow = new Rf2DescriptionRow();

        rf2DescriptionRow.setConceptSctId(getSctId(descriptionDto, descriptionDto.getConceptId()).toString());
        rf2DescriptionRow.setDescriptionSctId(getSctId(descriptionDto, descriptionDto.getDescriptionId()).toString());
        rf2DescriptionRow.setModuleSctId(getModuleId(descriptionDto).toString());
        rf2DescriptionRow.setEffectiveTime(getReleaseDate(descriptionDto));
        rf2DescriptionRow.setActive(getActiveFlag(descriptionDto));
        rf2DescriptionRow.setCaseSignificaceSctId(getSctId(descriptionDto, descriptionDto.getCaseSignificanceId()).toString());
        rf2DescriptionRow.setLanaguageCode(getSctId(descriptionDto, descriptionDto.getLanguageId()).toString());
        rf2DescriptionRow.setTerm(descriptionDto.getDescription());
        rf2DescriptionRow.setTypeSctId(getSctId(descriptionDto, descriptionDto.getTypeId()).toString());

        return rf2DescriptionRow;
    }

    /**
     * Copy the details from RelationshipDto to a Rf2RelationshipRow.
     *
     * @param relationshipDto RelationshipDto
     * @return Rf2RelationshipRow
     * @throws Exception if cannot get a valid SCT id
     */
    private Rf2RelationshipRow getRf2RelationshipRow(RelationshipDto relationshipDto) throws Exception {
        Rf2RelationshipRow relationshipRow = new Rf2RelationshipRow();

        relationshipRow.setRelationshipSctId(getSctId(relationshipDto, relationshipDto.getConceptId()).toString());
        relationshipRow.setSourceSctId(getSctId(relationshipDto, relationshipDto.getSourceId()).toString());
        relationshipRow.setDestinationSctId(getSctId(relationshipDto, relationshipDto.getDestinationId()).toString());
        relationshipRow.setModuleSctId(getModuleId(relationshipDto).toString());
        relationshipRow.setEffectiveTime(getReleaseDate(relationshipDto));
        relationshipRow.setActive(getActiveFlag(relationshipDto));
        relationshipRow.setCharacteristicSctId(getSctId(relationshipDto, relationshipDto.getCharacteristicTypeId()).toString());
        relationshipRow.setModifierSctId(getSctId(relationshipDto, relationshipDto.getModifierId()).toString());
        relationshipRow.setRelationshipGroup(getSctId(relationshipDto, relationshipDto.getRelationshipGroupId()).toString());
        relationshipRow.setRelationshipSctId(getSctId(relationshipDto, relationshipDto.getRelationshipId()).toString());
        relationshipRow.setTypeSctId(getSctId(relationshipDto, relationshipDto.getTypeId()).toString());

        return relationshipRow;
    }

    /**
     * Copy the details from ExtensionDto to a Rf2ReferenceSetRow
     *
     * @param extensionDto ExtensionDto
     * @return Rf2ReferenceSetRow
     * @throws Exception if cannot get a valid SCT id
     */
    private Rf2ReferenceSetRow getRf2ExtensionRow(ExtensionDto extensionDto) throws Exception {
        Rf2ReferenceSetRow referenceSetRow = new Rf2ReferenceSetRow();

        referenceSetRow.setRefsetId(getSctId(extensionDto, extensionDto.getConceptId()).toString());
        referenceSetRow.setMemberId(getSctId(extensionDto, extensionDto.getMemberId()).toString());
        referenceSetRow.setReferencedComponentId(getSctId(extensionDto, extensionDto.getConcept1Id()).toString());
        referenceSetRow.setModuleId(getModuleId(extensionDto).toString());
        referenceSetRow.setEffectiveTime(getReleaseDate(extensionDto));
        referenceSetRow.setActive(getActiveFlag(extensionDto));
        referenceSetRow.setReferencedComponentId(getSctId(extensionDto, extensionDto.getConcept1Id()).toString());

        if (extensionDto.getConcept2Id() != null) {
            referenceSetRow.setReferencedComponentId2(getSctId(extensionDto, extensionDto.getConcept2Id()).toString());
        }
        if (extensionDto.getConcept3Id() != null) {
            referenceSetRow.setReferencedComponentId3(getSctId(extensionDto, extensionDto.getConcept3Id()).toString());
        }
        if (extensionDto.getValue() != null) {
            referenceSetRow.setValue(extensionDto.getValue());
        }

        return referenceSetRow;
    }

    /**
     * Gets the rf2 flag string for the ConceptDto
     *
     * @param conceptDto ConceptDto
     * @return String 1 or 0 1 is active
     */
    private String getActiveFlag(ConceptDto conceptDto) {
        return conceptDto.isActive() ? "1" : "0";
    }

    /**
     * @see org.dwfa.mojo.export.file.SnomedFileFormatOutputHandler#getReleaseDate(org.dwfa.dto.ConceptDto)
     */
    @Override
    String getReleaseDate(ConceptDto conceptDto) {
        return AceDateFormat.getRf2DateFormat().format(conceptDto.getDateTime());
    }
}
