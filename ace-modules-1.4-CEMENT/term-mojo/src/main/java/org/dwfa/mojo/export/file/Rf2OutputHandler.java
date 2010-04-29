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
import java.util.ArrayList;
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

    private File fullExportDirectory;
    private File snapShotExportDirectory;
    private File exportClinicalRefsetDirectory;
    private File exportClinicalRefsetDirectorySnapShot;
    private File exportStructuralRefsetDirectory;
    private File exportStructuralRefsetDirectorySnapShot;
    private Rf2IdentifierWriter identifierFileFull;
    private Rf2IdentifierWriter identifierCliniclFileFull;
    private Rf2IdentifierWriter identifierStructuralFileFull;
    private Rf2ConceptWriter conceptFileFull;
    private Rf2DescriptionWriter descriptionFileFull;
    private Rf2RelationshipWriter relationshipFileFull;
    private Rf2IdentifierWriter identifierFileSnapShot;
    private Rf2IdentifierWriter identifierCliniclFileSnapShot;
    private Rf2IdentifierWriter identifierStructuralFileSnapShot;
    private Rf2ConceptWriter conceptFileSnapShot;
    private Rf2DescriptionWriter descriptionFileSnapShot;
    private Rf2RelationshipWriter relationshipFileSnapShot;
    private Map<Long, Rf2ReferenceSetWriter> referenceSetFileMap;
    private Map<Long, Rf2ReferenceSetWriter> referenceSetFileMapSnapShot;

    /**
     * Constructor
     *
     * @param fullExportDirectory File
     * @param SctIdDbDirectory File
     * @throws Exception
     */
    public Rf2OutputHandler(File exportDirectoryToSet, Map<UUID, Map<UUID, Date>> releasePathDateMap) throws Exception {
        super(releasePathDateMap);

        fullExportDirectory = new File(exportDirectoryToSet.getAbsolutePath() + File.separatorChar + "full");
        fullExportDirectory.mkdirs();

        snapShotExportDirectory = new File(exportDirectoryToSet.getAbsolutePath() + File.separatorChar + "snapshot");
        snapShotExportDirectory.mkdirs();

        exportClinicalRefsetDirectory = new File(this.fullExportDirectory.getAbsolutePath() + File.separatorChar + "refsets"
            + File.separatorChar + "clinical" + File.separatorChar);
        exportClinicalRefsetDirectorySnapShot = new File(this.snapShotExportDirectory.getAbsolutePath() + File.separatorChar + "refsets"
                + File.separatorChar + "clinical" + File.separatorChar);
        exportStructuralRefsetDirectory = new File(this.fullExportDirectory.getAbsolutePath() + File.separatorChar
            + "refsets" + File.separatorChar + "structural" + File.separatorChar);
        exportStructuralRefsetDirectorySnapShot = new File(this.snapShotExportDirectory.getAbsolutePath() + File.separatorChar
            + "refsets" + File.separatorChar + "structural" + File.separatorChar);
        exportClinicalRefsetDirectory.mkdirs();
        exportClinicalRefsetDirectorySnapShot.mkdirs();
        exportStructuralRefsetDirectory.mkdirs();
        exportStructuralRefsetDirectorySnapShot.mkdirs();

        identifierFileFull = new Rf2IdentifierWriter(new File(fullExportDirectory + File.separator + "ids.rf2.txt"));
        identifierCliniclFileFull = new Rf2IdentifierWriter(new File(fullExportDirectory + File.separator + "ids.clinical.rf2.txt"));
        identifierStructuralFileFull = new Rf2IdentifierWriter(new File(fullExportDirectory + File.separator + "ids.structural.rf2.txt"));
        conceptFileFull = new Rf2ConceptWriter(new File(fullExportDirectory + File.separator + "concepts.rf2.txt"));
        descriptionFileFull = new Rf2DescriptionWriter(new File(fullExportDirectory + File.separator + "descriptions.rf2.txt"));
        relationshipFileFull = new Rf2RelationshipWriter(new File(fullExportDirectory + File.separator
            + "relationships.rf2.txt"));

        identifierFileSnapShot = new Rf2IdentifierWriter(new File(snapShotExportDirectory + File.separator + "ids.rf2.txt"));
        identifierCliniclFileSnapShot = new Rf2IdentifierWriter(new File(snapShotExportDirectory + File.separator + "ids.clinical.rf2.txt"));
        identifierStructuralFileSnapShot = new Rf2IdentifierWriter(new File(snapShotExportDirectory + File.separator + "ids.structural.rf2.txt"));
        conceptFileSnapShot = new Rf2ConceptWriter(new File(snapShotExportDirectory + File.separator + "concepts.rf2.txt"));
        descriptionFileSnapShot = new Rf2DescriptionWriter(new File(snapShotExportDirectory + File.separator + "descriptions.rf2.txt"));
        relationshipFileSnapShot = new Rf2RelationshipWriter(new File(snapShotExportDirectory + File.separator
                + "relationships.rf2.txt"));

        referenceSetFileMap = new HashMap<Long, Rf2ReferenceSetWriter>();
        referenceSetFileMapSnapShot = new HashMap<Long, Rf2ReferenceSetWriter>();
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
                synchronized (conceptFileFull) {
                    conceptFileFull.write(getRf2ConceptRow(conceptDto));
                }
                if(conceptDto.isLatest()){
                    synchronized (conceptFileSnapShot) {
                        conceptFileSnapShot.write(getRf2ConceptRow(conceptDto));
                    }
                }
            }
        }
        synchronized (identifierFileFull) {
            identifierFileFull.write(getRf2IdentifierRows(componentDto.getConceptDtos(), false));
        }
        synchronized (identifierFileSnapShot) {
            identifierFileSnapShot.write(getRf2IdentifierRows(componentDto.getConceptDtos(), true));
        }

        for (DescriptionDto descriptionDto : componentDto.getDescriptionDtos()) {
            if (isNewActiveOrRetiringLiveConcept && descriptionDto.isNewActiveOrRetiringLive()) {
                synchronized (descriptionFileFull) {
                    descriptionFileFull.write(getRf2DescriptionRow(descriptionDto));
                }
                if(descriptionDto.isLatest() && !descriptionDto.isActive() && descriptionDto.isLive()){
                    synchronized (descriptionFileSnapShot) {
                        descriptionFileSnapShot.write(getRf2DescriptionRow(descriptionDto));
                    }
                } else if(descriptionDto.isLatest() && descriptionDto.isActive()){
                    synchronized (descriptionFileSnapShot) {
                        descriptionFileSnapShot.write(getRf2DescriptionRow(descriptionDto));
                    }
                }
            }
        }
        synchronized (identifierFileFull) {
            identifierFileFull.write(getRf2IdentifierRows(componentDto.getDescriptionDtos(), false));
        }
        synchronized (identifierFileSnapShot) {
            identifierFileSnapShot.write(getRf2IdentifierRows(componentDto.getDescriptionDtos(), true));
        }

        for (RelationshipDto relationshipDto : componentDto.getRelationshipDtos()) {
            if (isNewActiveOrRetiringLiveConcept && relationshipDto.isNewActiveOrRetiringLive()) {
                synchronized (relationshipFileFull) {
                    relationshipFileFull.write(getRf2RelationshipRow(relationshipDto));
                }
                if(relationshipDto.isLatest()){
                    synchronized (relationshipFileSnapShot) {
                        relationshipFileSnapShot.write(getRf2RelationshipRow(relationshipDto));
                    }
                }
            }
        }
        synchronized (identifierFileFull) {
            identifierFileFull.write(getRf2IdentifierRows(componentDto.getRelationshipDtos(), false));
        }
        synchronized (identifierFileSnapShot) {
            identifierFileSnapShot.write(getRf2IdentifierRows(componentDto.getRelationshipDtos(), true));
        }

        if (isNewActiveOrRetiringLiveConcept) {
            // group all the matching members together.
            for (ExtensionDto extensionDto : componentDto.getConceptExtensionDtos()) {
                if (extensionDto.isNewActiveOrRetiringLive()) {
                    writeExtensionRow(extensionDto);
                }
            }
            writeExtensionIdRows(componentDto.getConceptExtensionDtos());

            for (ExtensionDto extensionDto : componentDto.getDescriptionExtensionDtos()) {
                if (extensionDto.isNewActiveOrRetiringLive()) {
                    writeExtensionRow(extensionDto);
                }
            }
            writeExtensionIdRows(componentDto.getDescriptionExtensionDtos());

            for (ExtensionDto extensionDto : componentDto.getRelationshipExtensionDtos()) {
                if (extensionDto.isNewActiveOrRetiringLive()) {
                    writeExtensionRow(extensionDto);
                }
            }
            writeExtensionIdRows(componentDto.getRelationshipExtensionDtos());
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

        Rf2ReferenceSetWriter referenceSetWriter = getReferenceSetWriter(extensionDto, false);
        synchronized (referenceSetWriter) {
            referenceSetWriter.write(referenceSetRow);
        }
        if (extensionDto.isLatest()) {
            referenceSetWriter = getReferenceSetWriter(extensionDto, true);
            synchronized (referenceSetWriter) {
                referenceSetWriter.write(referenceSetRow);
            }
        }
    }

    /**
     * Writes a row into the id file for the extension member.
     *
     * Checks that the id row has not been written before to avoid duplicates.
     *
     * @param extensionDto ExtensionDto

     * @throws Exception
     */
    private void writeExtensionIdRows(List<ExtensionDto> extensionDtos) throws Exception {
        Collections.sort(extensionDtos);
        ExtensionDto lastExtensionDto = null;
        for (ExtensionDto extensionDto : extensionDtos) {
            if (extensionDto.isNewActiveOrRetiringLive()) {
                if (lastExtensionDto == null || !lastExtensionDto.getMemberId().equals(extensionDto.getMemberId())) {
                    if (extensionDto.isClinical()) {
                        synchronized (identifierCliniclFileFull) {
                            identifierCliniclFileFull.write(getRf2MemberIdentifierRow(extensionDto));
                        }
                        if (extensionDto.isLatest()) {
                            synchronized (identifierCliniclFileSnapShot) {
                                identifierCliniclFileSnapShot.write(getRf2MemberIdentifierRow(extensionDto));
                            }
                        }
                    } else {
                        synchronized (identifierStructuralFileFull) {
                            identifierStructuralFileFull.write(getRf2MemberIdentifierRow(extensionDto));
                        }
                        if (extensionDto.isLatest()) {
                            synchronized (identifierStructuralFileSnapShot) {
                                identifierStructuralFileSnapShot.write(getRf2MemberIdentifierRow(extensionDto));
                            }
                        }
                    }
                }
                lastExtensionDto = extensionDto;
            }
        }
    }

    /**
     * Close all the files.
     *
     * @throws IOException if files cannot be closed.
     */
    public void closeFiles() throws IOException {
        identifierFileFull.close();
        conceptFileFull.close();
        descriptionFileFull.close();
        relationshipFileFull.close();
        identifierCliniclFileFull.close();
        identifierStructuralFileFull.close();

        identifierFileSnapShot.close();
        identifierCliniclFileSnapShot.close();
        identifierStructuralFileSnapShot.close();
        conceptFileSnapShot.close();
        descriptionFileSnapShot.close();
        relationshipFileSnapShot.close();

        for (Rf2ReferenceSetWriter rf2ReferenceSetWriter : referenceSetFileMap.values()) {
            rf2ReferenceSetWriter.close();
        }
        for (Rf2ReferenceSetWriter rf2ReferenceSetWriter : referenceSetFileMapSnapShot.values()) {
            rf2ReferenceSetWriter.close();
        }
    }

    /**
     * For a given refset id get the file writer.
     *
     * The refset file name is the refset name, could write logic here to get
     * the full file name or just get the assembly project to fix them.
     *
     * @param conceptDto ConceptDto
     * @param extensionDto ExtensionDto
     * @return Rf2ReferenceSetWriter
     * @throws Exception cannot get an SCT id
     */
    private Rf2ReferenceSetWriter getReferenceSetWriter(ExtensionDto extensionDto, boolean latest) throws Exception {
        Long sctId = getSctId(extensionDto, extensionDto.getConceptId(), TYPE.CONCEPT);

        if (!getReferenceSetFileMap(latest).containsKey(sctId)) {
            Rf2ReferenceSetWriter newReferenceSetWriter;
            if (extensionDto.isClinical()) {
                newReferenceSetWriter = new Rf2ReferenceSetWriter(new File(getReferenceSetExportDirectory(extensionDto.isClinical(), latest)
                    + File.separator + convertToCamelCase(extensionDto.getFullySpecifiedName()) + ".txt"));
            } else {
                newReferenceSetWriter = new Rf2ReferenceSetWriter(new File(getReferenceSetExportDirectory(extensionDto.isClinical(), latest)
                    + File.separator + convertToCamelCase(extensionDto.getFullySpecifiedName()) + ".txt"));
            }

            getReferenceSetFileMap(latest).put(sctId, newReferenceSetWriter);
        }

        return getReferenceSetFileMap(latest).get(sctId);
    }

    /**
     * Get the file map for the full or snapshot
     *
     * @param latest boolean
     * @return Map of Long to Rf2ReferenceSetWriter
     */
    private Map<Long, Rf2ReferenceSetWriter> getReferenceSetFileMap(boolean latest){
        Map<Long, Rf2ReferenceSetWriter> refsetFileMap = referenceSetFileMap;
        if(latest){
            refsetFileMap = referenceSetFileMapSnapShot;
        }

        return refsetFileMap;
    }

    /**
     * Get the directory for the refset file
     *
     * @param clinical boolean
     * @param latest boolean
     * @return File
     */
    private File getReferenceSetExportDirectory(boolean clinical, boolean latest){
        File exportDirectory;

        if(latest){
            if(clinical){
                exportDirectory = exportClinicalRefsetDirectorySnapShot;
            } else {
                exportDirectory = exportStructuralRefsetDirectorySnapShot;
            }
        } else {
            if(clinical){
                exportDirectory = exportClinicalRefsetDirectory;
            } else {
                exportDirectory = exportStructuralRefsetDirectory;
            }
        }

        return exportDirectory;
    }

    /**
     * Camel case with first letter capped.
     *
     * @param string String
     *
     * @return String
     */
    private String convertToCamelCase(String string) {
        StringBuffer sb = new StringBuffer("");
        String[] str = string.replaceAll("/", "").split(" ");
        for (String temp : str) {
            if (temp.length() > 1) {
                sb.append(Character.toUpperCase(temp.charAt(0)));
                sb.append(temp.substring(1).toLowerCase());
            }
        }
        return sb.toString();
    }

    /**
     * Get the Rf2IdentifierRows from the ConceptDto.
     *
     * Returns a unique list of UUID, SCTID rows.
     *
     * @param conceptDtos List of ConceptDto
     * @return List of Rf2IdentifierRow
     * @throws Exception
     */
    private List<Rf2IdentifierRow> getRf2IdentifierRows(List<? extends ConceptDto> conceptDtos, boolean latest) throws Exception {
        List<Rf2IdentifierRow> rf2IdentifierRows = new ArrayList<Rf2IdentifierRow>();
        Map<UUID, Long> exportedIds = new HashMap<UUID, Long>();

        for (ConceptDto conceptDto : conceptDtos) {
            if(conceptDto.isNewActiveOrRetiringLive()){
                for (IdentifierDto identifierDto : conceptDto.getIdentifierDtos()) {
                    UUID uuid  = identifierDto.getConceptId().keySet().iterator().next();

                    if (!exportedIds.containsKey(uuid)) {
                        Rf2IdentifierRow rf2IdentifierRow = new Rf2IdentifierRow();
                        rf2IdentifierRow.setActive(identifierDto.isActive() ? "1" : "0");
                        rf2IdentifierRow.setAlternateIdentifier(uuid.toString());
                        rf2IdentifierRow.setEffectiveTime(getReleaseDate(identifierDto));
                        rf2IdentifierRow.setIdentifierSchemeSctId(getSctId(identifierDto,
                            identifierDto.getIdentifierSchemeUuid(), TYPE.CONCEPT).toString());
                        rf2IdentifierRow.setModuleSctId(getModuleId(identifierDto).toString());
                        rf2IdentifierRow.setReferencedComponentSctId(identifierDto.getReferencedSctId().toString());

                        if (!latest) {
                            rf2IdentifierRows.add(rf2IdentifierRow);
                            exportedIds.put(uuid, identifierDto.getReferencedSctId());
                        } else if((latest && identifierDto.isLatest() && conceptDto.isActive())) {
                            rf2IdentifierRows.add(rf2IdentifierRow);
                            exportedIds.put(uuid, identifierDto.getReferencedSctId());
                        } else if((latest && identifierDto.isLatest() && !conceptDto.isActive()) && conceptDto.isLive()) {
                            rf2IdentifierRows.add(rf2IdentifierRow);
                            exportedIds.put(uuid, identifierDto.getReferencedSctId());
                        }
                    }
                }
            }
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

        String lanuageCode = descriptionDto.getLanguageCode();
        if (lanuageCode.length() > 2) {
            lanuageCode = lanuageCode.substring(0, 2);
        }

        rf2DescriptionRow.setDescriptionSctId(getSctId(descriptionDto, descriptionDto.getDescriptionId(), descriptionDto.getIdentifierDtos(), descriptionDto.getType()).toString());
        rf2DescriptionRow.setConceptSctId(getSctId(descriptionDto, descriptionDto.getConceptId(), TYPE.CONCEPT).toString());
        rf2DescriptionRow.setModuleSctId(getModuleId(descriptionDto).toString());
        rf2DescriptionRow.setEffectiveTime(getReleaseDate(descriptionDto));
        rf2DescriptionRow.setActive(getActiveFlag(descriptionDto));
        rf2DescriptionRow.setCaseSignificaceSctId(getSctId(descriptionDto, descriptionDto.getCaseSignificanceId(), TYPE.CONCEPT).toString());
        rf2DescriptionRow.setLanaguageCode(lanuageCode);
        rf2DescriptionRow.setTerm(descriptionDto.getDescription());
        rf2DescriptionRow.setTypeSctId(getSctId(descriptionDto, descriptionDto.getRf2TypeId(), TYPE.CONCEPT).toString());

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

        relationshipRow.setRelationshipSctId(getSctId(relationshipDto, relationshipDto.getIdentifierDtos()).toString());
        relationshipRow.setSourceSctId(getSctId(relationshipDto, relationshipDto.getSourceId(), TYPE.CONCEPT).toString());
        relationshipRow.setDestinationSctId(getSctId(relationshipDto, relationshipDto.getDestinationId(), TYPE.CONCEPT).toString());
        relationshipRow.setModuleSctId(getModuleId(relationshipDto).toString());
        relationshipRow.setEffectiveTime(getReleaseDate(relationshipDto));
        relationshipRow.setActive(getActiveFlag(relationshipDto));
        relationshipRow.setCharacteristicSctId(getSctId(relationshipDto, relationshipDto.getCharacteristicTypeId(), TYPE.CONCEPT).toString());
        relationshipRow.setModifierSctId(getSctId(relationshipDto, relationshipDto.getModifierId(), TYPE.CONCEPT).toString());
        relationshipRow.setTypeSctId(getSctId(relationshipDto, relationshipDto.getTypeId(), TYPE.CONCEPT).toString());
        relationshipRow.setRelationshipGroup(relationshipDto.getRelationshipGroup().toString());

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

        referenceSetRow.setMemberId(getSctId(extensionDto, extensionDto.getMemberId(), extensionDto.getIdentifierDtos(), TYPE.REFSET).toString());
        referenceSetRow.setRefsetId(getSctId(extensionDto, extensionDto.getConceptId(), TYPE.CONCEPT).toString());
        referenceSetRow.setReferencedComponentId(getSctId(extensionDto, extensionDto.getReferencedConceptId(), extensionDto.getType()).toString());
        referenceSetRow.setModuleId(getModuleId(extensionDto).toString());
        referenceSetRow.setEffectiveTime(getReleaseDate(extensionDto));
        referenceSetRow.setActive(getActiveFlag(extensionDto));

        TYPE valueComponentType = getValueComponentExtensionType(extensionDto);
        if (extensionDto.getConcept1Id() != null) {
            referenceSetRow.setComponentId1(getSctId(extensionDto, extensionDto.getConcept1Id(), valueComponentType).toString());
        }
        if (extensionDto.getConcept2Id() != null) {
            referenceSetRow.setComponentId2(getSctId(extensionDto, extensionDto.getConcept2Id(), valueComponentType).toString());
        }
        if (extensionDto.getConcept3Id() != null) {
            referenceSetRow.setComponentId3(getSctId(extensionDto, extensionDto.getConcept3Id(), valueComponentType).toString());
        }
        if (extensionDto.getValue() != null) {
            referenceSetRow.setValue(extensionDto.getValue());
        }

        return referenceSetRow;
    }

    /**
     * Check if the refset is a structural refset. If so then the TYPE of the
     * refset value is a concept eg Description Inactivation Indicator is a
     * concept.
     *
     * @param extensionDto ExtensionDto
     * @return TYPE
     */
    private TYPE getValueComponentExtensionType(ExtensionDto extensionDto) {
        TYPE type = TYPE.CONCEPT;

        if(extensionDto.isClinical()){
            type = extensionDto.getType();
        }

        return type;
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
     * @see org.dwfa.mojo.export.file.SnomedFileFormatOutputHandler#getReleaseDateString(org.dwfa.dto.ConceptDto)
     */
    @Override
    String getReleaseDateString(Date concept) {
        return AceDateFormat.getRf2DateFormat().format(concept);
    }
}
