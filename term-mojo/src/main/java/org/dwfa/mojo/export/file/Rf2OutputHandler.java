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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dwfa.dto.ComponentDto;
import org.dwfa.dto.Concept;
import org.dwfa.dto.ConceptDto;
import org.dwfa.dto.DescriptionDto;
import org.dwfa.dto.ExtensionDto;
import org.dwfa.dto.IdentifierDto;
import org.dwfa.dto.RelationshipDto;
import org.dwfa.maven.transform.SctIdGenerator.TYPE;
import org.dwfa.mojo.file.rf2.Rf2ConceptRow;
import org.dwfa.mojo.file.rf2.Rf2ConceptWriter;
import org.dwfa.mojo.file.rf2.Rf2DescriptionRow;
import org.dwfa.mojo.file.rf2.Rf2DescriptionWriter;
import org.dwfa.mojo.file.rf2.Rf2IdentifierRow;
import org.dwfa.mojo.file.rf2.Rf2IdentifierWriter;
import org.dwfa.mojo.file.rf2.Rf2ReferenceSetRow;
import org.dwfa.mojo.file.rf2.Rf2ReferenceSetWriter;
import org.dwfa.mojo.file.rf2.Rf2RelationshipRow;
import org.dwfa.mojo.file.rf2.Rf2RelationshipWriter;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.AceDateFormat;

/**
 * Writes out RF2 format files for both core files and reference sets.
 */
public class Rf2OutputHandler extends SnomedFileFormatOutputHandler {

    private File exportDirectory;
    private Rf2IdentifierWriter identifierFile;
    private Rf2ConceptWriter conceptFile;
    private Rf2DescriptionWriter descriptionFile;
    private Rf2RelationshipWriter relationshipFile;
    private Map<Long, Rf2ReferenceSetWriter> referenceSetFileMap;

    public Rf2OutputHandler(File exportDirectory, File SctIdDbDirectory) throws IOException, SQLException,
            ClassNotFoundException {
        super(SctIdDbDirectory);

        this.exportDirectory = exportDirectory;

        identifierFile = new Rf2IdentifierWriter(new File(exportDirectory + File.separator + "ids.rf2.txt"));
        conceptFile = new Rf2ConceptWriter(new File(exportDirectory + File.separator + "concepts.rf2.txt"));
        descriptionFile = new Rf2DescriptionWriter(new File(exportDirectory + File.separator + "descriptions.rf2.txt"));
        relationshipFile = new Rf2RelationshipWriter(new File(exportDirectory + File.separator
            + "relationships.rf2.txt"));

        referenceSetFileMap = new HashMap<Long, Rf2ReferenceSetWriter>();
    }

    @Override
    void exportComponent(ComponentDto componentDto) throws Exception {
        conceptFile.write(getRf2ConceptRow(componentDto.getConceptDto()));
        identifierFile.write(getRf2IdentifierRows(componentDto.getConceptDto()));

        for (DescriptionDto descriptionDto : componentDto.getDescriptionDtos()) {
            descriptionFile.write(getRf2DescriptionRow(descriptionDto));
            identifierFile.write(getRf2IdentifierRows(descriptionDto));
        }

        for (RelationshipDto relationshipDto : componentDto.getRelationshipDtos()) {
            relationshipFile.write(getRf2RelationshipRow(relationshipDto));
            identifierFile.write(getRf2IdentifierRows(relationshipDto));
        }

        for (ExtensionDto extensionDto : componentDto.getConceptExtensionDtos()) {
            writeExtensionRow(extensionDto);
        }

        for (ExtensionDto extensionDto : componentDto.getDescriptionExtensionDtos()) {
            writeExtensionRow(extensionDto);
        }

        for (ExtensionDto extensionDto : componentDto.getRelationshipExtensionDtos()) {
            writeExtensionRow(extensionDto);
        }
    }

    /**
     * Writes an refset member to file.
     *
     * @param extensionDto ExtensionDto
     * @throws Exception
     * @throws IOException
     * @throws TerminologyException
     */
    private void writeExtensionRow(ExtensionDto extensionDto) throws Exception, IOException, TerminologyException {
        Rf2ReferenceSetRow referenceSetRow = getRf2ExtensionRow(extensionDto);
        getReferenceSetWriter(extensionDto).write(referenceSetRow);
        identifierFile.write(getRf2MemberIdentifierRow(extensionDto));
    }

    /**
     * @see java.lang.Object#finalize()
     */
    @Override
    protected void finalize() throws Throwable {
        super.finalize();

        identifierFile.close();
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
        Long sctId = getSctId(extensionDto, extensionDto.getConceptId(), TYPE.CONCEPT);

        if (!referenceSetFileMap.containsKey(sctId)) {
            referenceSetFileMap.put(sctId, new Rf2ReferenceSetWriter(new File(exportDirectory + File.separator + sctId
                + extensionDto.getFullySpecifiedName() + ".txt")));
        }

        return referenceSetFileMap.get(sctId);
    }

    private List<Rf2IdentifierRow> getRf2IdentifierRows(ConceptDto conceptDto) throws Exception {
        List<Rf2IdentifierRow> rf2IdentifierRows = new ArrayList<Rf2IdentifierRow>(conceptDto.getIdentifierDtos()
            .size());

        for (IdentifierDto identifierDto : conceptDto.getIdentifierDtos()) {
            Rf2IdentifierRow rf2IdentifierRow = new Rf2IdentifierRow();
            rf2IdentifierRow.setActive(identifierDto.isActive() ? "1" : "0");
            rf2IdentifierRow.setAlternateIdentifier(identifierDto.getConceptId().toString());
            rf2IdentifierRow.setEffectiveTime(getReleaseDate(identifierDto));
            rf2IdentifierRow.setIdentifierSchemeSctId(getSctId(identifierDto, identifierDto.getIdentifierSchemeUuid()).toString());
            rf2IdentifierRow.setModuleSctId(getModuleId(identifierDto).toString());
            rf2IdentifierRow.setReferencedComponentSctId(identifierDto.getReferencedSctId().toString());

            rf2IdentifierRows.add(rf2IdentifierRow);
        }

        return rf2IdentifierRows;
    }

    /**
     * Sets the member sctid, if one exists otherwise one is generated.
     *
     * @param extensionDto ExtensionDto
     * @return Rf2IdentifierRow
     * @throws Exception
     */
    private Rf2IdentifierRow getRf2MemberIdentifierRow(ExtensionDto extensionDto) throws Exception {
        Rf2IdentifierRow rf2IdentifierRow = new Rf2IdentifierRow();
        rf2IdentifierRow.setActive(extensionDto.isActive() ? "1" : "0");
        rf2IdentifierRow.setAlternateIdentifier(extensionDto.getMemberId().toString());
        rf2IdentifierRow.setEffectiveTime(getReleaseDate(extensionDto));
        rf2IdentifierRow.setModuleSctId(getModuleId(extensionDto).toString());

        rf2IdentifierRow.setIdentifierSchemeSctId(
            getSctId(extensionDto, Rf2IdentifierRow.SCT_ID_IDENTIFIER_SCHEME, TYPE.CONCEPT).toString());
        if (!extensionDto.getIdentifierDtos().isEmpty()) {
            rf2IdentifierRow.setReferencedComponentSctId(
                extensionDto.getIdentifierDtos().get(0).getReferencedSctId().toString());
        } else {
            rf2IdentifierRow.setReferencedComponentSctId(
                getSctId(extensionDto, extensionDto.getMemberId(), TYPE.REFSET).toString());
        }

        return rf2IdentifierRow;
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
        conceptRow.setDefiniationStatusSctId(getSctId(conceptDto, conceptDto.getStatusId()).toString());
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

        rf2DescriptionRow.setConceptSctId(getSctId(descriptionDto, descriptionDto.getConceptId(), TYPE.CONCEPT).toString());
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
        relationshipRow.setSourceSctId(getSctId(relationshipDto, relationshipDto.getSourceId(), TYPE.CONCEPT).toString());
        relationshipRow.setDestinationSctId(getSctId(relationshipDto, relationshipDto.getDestinationId(), TYPE.CONCEPT).toString());
        relationshipRow.setModuleSctId(getModuleId(relationshipDto).toString());
        relationshipRow.setEffectiveTime(getReleaseDate(relationshipDto));
        relationshipRow.setActive(getActiveFlag(relationshipDto));
        relationshipRow.setCharacteristicSctId(getSctId(relationshipDto, relationshipDto.getCharacteristicTypeId(), TYPE.CONCEPT).toString());
        relationshipRow.setModifierSctId(getSctId(relationshipDto, relationshipDto.getModifierId()).toString());
        relationshipRow.setRelationshipSctId(getSctId(relationshipDto, relationshipDto.getConceptId()).toString());
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

        referenceSetRow.setRefsetId(getSctId(extensionDto, extensionDto.getConceptId(), TYPE.CONCEPT).toString());
        referenceSetRow.setMemberId(getSctId(extensionDto, extensionDto.getMemberId(), TYPE.REFSET).toString());
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
     * @param concept Concept
     * @return String 1 or 0 1 is active
     */
    private String getActiveFlag(Concept concept) {
        return concept.isActive() ? "1" : "0";
    }

    /**
     * @see org.dwfa.mojo.export.file.SnomedFileFormatOutputHandler#getReleaseDate(org.dwfa.dto.ConceptDto)
     */
    @Override
    String getReleaseDate(Concept concept) {
        return AceDateFormat.getRf2DateFormat().format(concept.getDateTime());
    }
}
