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
/**
 * Apache License.
 *
 * Mojo to convert Ace file to rf2 file.
 */
package org.dwfa.mojo.file;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;
import java.util.logging.Logger;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.maven.I_ReadAndTransform;
import org.dwfa.maven.sctid.transform.UuidToSctConIdWithGeneration;
import org.dwfa.maven.sctid.transform.UuidToSctDescIdWithGeneration;
import org.dwfa.maven.sctid.transform.UuidToSctIdWithGeneration;
import org.dwfa.maven.sctid.transform.UuidToSctRelIdWithGeneration;
import org.dwfa.maven.transform.CaseSensitivityToUuidTransform;
import org.dwfa.maven.transform.SctIdGenerator.NAMESPACE;
import org.dwfa.maven.transform.SctIdGenerator.PROJECT;
import org.dwfa.mojo.ConceptConstants;
import org.dwfa.mojo.file.AceConceptReader.AceConceptRow;
import org.dwfa.mojo.file.AceDescriptionReader.AceDescriptionRow;
import org.dwfa.mojo.file.AceRelationshipReader.AceRelationshipRow;
import org.dwfa.mojo.file.rf2.Rf2ConceptRow;
import org.dwfa.mojo.file.rf2.Rf2ConceptWriter;
import org.dwfa.mojo.file.rf2.Rf2DescriptionRow;
import org.dwfa.mojo.file.rf2.Rf2DescriptionWriter;
import org.dwfa.mojo.file.rf2.Rf2IdentifierRow;
import org.dwfa.mojo.file.rf2.Rf2IdentifierWriter;
import org.dwfa.mojo.file.rf2.Rf2RelationshipRow;
import org.dwfa.mojo.file.rf2.Rf2RelationshipWriter;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.tapi.TerminologyRuntimeException;
import org.dwfa.util.AceDateFormat;

/**
 * @goal transformAce2Rf2
 */
public class Ace2Rf2 extends AbstractMojo {

    private static final Collection<UUID> HISTORICAL_CHARACTERISTIC_UUIDS = ArchitectonicAuxiliary.Concept.HISTORICAL_CHARACTERISTIC.getUids();

    /**
     * Class logger.
     */
    private Logger logger = Logger.getLogger(Ace2Rf2.class.getName());

    /**
     * @parameter
     * @required
     */
    private String idAceFile;

    /**
     * @parameter
     * @required
     */
    private String conceptAceFile;

    /**
     * @parameter
     * @required
     */
    private String descriptionAceFile;

    /**
     * @parameter
     * @required
     */
    private String relationshipAceFile;

    /**
     * @parameter
     * @required
     */
    private String identifierRf2File;

    /**
     * @parameter
     * @required
     */
    private String conceptRf2File;

    /**
     * @parameter
     * @required
     */
    private String descriptionRf2File;

    /**
     * @parameter
     * @required
     */
    private String relationshipRf2File;

    /**
     * @parameter
     * @required
     */
    private String sourceDirectory;

    /**
     * @parameter
     * @required
     */
    private String buildDirectory;

    /**
     * @parameter
     */
    private String hasHeader = Boolean.FALSE.toString();

    /**
     * For converting uuids to sctid for concepts.
     */
    private UuidToSctIdWithGeneration uuidToSctIdConcept = new UuidToSctConIdWithGeneration();

    /**
     * For converting uuids to sctid for descriptions.
     */
    private UuidToSctIdWithGeneration uuidToSctIdDescription = new UuidToSctDescIdWithGeneration();

    /**
     * For converting uuids to sctid for relationship.
     */
    private UuidToSctIdWithGeneration uuidToSctIdRelationship = new UuidToSctRelIdWithGeneration();

    /**
     * Converts case sensitivity to a uuid.
     */
    private I_ReadAndTransform caseSensitivityToUuidTransform = new CaseSensitivityToUuidTransform();

    /**
     * RF2 date format. 20091101T000000ZT000000Z
     */
    private DateFormat rf2DateFormat = AceDateFormat.getRf2DateFormat();

    /**
     * RF2 date format. 2009-11-01T00:00:00ZT
     */
    private DateFormat exportDateFormat = AceDateFormat.getOldAceExportDateFormat();

    /**
     * Sct id for a primative concepts
     */
    private String primationSctId;

    /**
     * Sct id for a well definied concept.
     */
    private String fullyDefinedSctId;

    /**
     * SctId for fully specified description type
     */
    private String fsnSctId;

    /**
     * SctId for synonym description type
     */
    private String synonymSctId;

    /**
     * The active concept
     */
    private I_GetConceptData activeConcept;

    /**
     * UUID identifier scheme concept
     */
    private String uuidIdentifierSchemeSctId;

    /**
     * SctId for the some concept under modifier.
     */
    private String modifierSomeSctId;

    /**
     * Term Factory.
     */
    protected I_TermFactory tf = LocalVersionedTerminology.get();

    /**
     * Cache of namespaces for UUID strings.
     */
    private Map<String, NAMESPACE> namespaceCache = new HashMap<String, NAMESPACE>();

    /**
     * Parameter to allow translation of more than one identifier file
     *
     * @parameter
     */
    private IdentifierFile[] additionalIdentifierFiles;

    /**
     * Convert ace file to rf2.
     *
     * @see org.apache.maven.plugin.Mojo#execute()
     */
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            logger.info("Processing ace files.");
            exportDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            rf2DateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

            uuidToSctIdConcept.setupImpl();
            uuidToSctIdDescription.setupImpl();
            uuidToSctIdRelationship.setupImpl();

            primationSctId = uuidToSctIdConcept.transform(ArchitectonicAuxiliary.Concept.PRIMITIVE_DEFINITION.getUids()
                .iterator()
                .next()
                .toString(), NAMESPACE.SNOMED_META_DATA, PROJECT.SNOMED_CT);

            fullyDefinedSctId = uuidToSctIdConcept.transform(
                ArchitectonicAuxiliary.Concept.DEFINED_DEFINITION.getUids().iterator().next().toString(),
                NAMESPACE.SNOMED_META_DATA, PROJECT.SNOMED_CT);

            fsnSctId = uuidToSctIdConcept.transform(
                ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids().iterator().next().toString(),
                NAMESPACE.SNOMED_META_DATA, PROJECT.SNOMED_CT);

            synonymSctId = uuidToSctIdConcept.transform(
                ArchitectonicAuxiliary.Concept.SYNONYM_DESCRIPTION_TYPE.getUids().iterator().next().toString(),
                NAMESPACE.SNOMED_META_DATA, PROJECT.SNOMED_CT);

            activeConcept = tf.getConcept(ArchitectonicAuxiliary.Concept.ACTIVE.localize().getUids().iterator().next());
            uuidIdentifierSchemeSctId = uuidToSctIdConcept.transform(
                ConceptConstants.UNIVERSALLY_UNIQUE_IDENTIFIER.getUuids()[0].toString(), NAMESPACE.SNOMED_META_DATA, PROJECT.SNOMED_CT);
            modifierSomeSctId = uuidToSctIdConcept.transform(ConceptConstants.MODIFIER_SOME.getUuids()[0].toString(),
                NAMESPACE.SNOMED_META_DATA, PROJECT.SNOMED_CT);
        } catch (IOException e) {
            logger.severe("ERROR: error accessing build and/or source directories " + e.getMessage());
            throw new MojoExecutionException(e.getMessage(), e);
        } catch (Exception e) {
            logger.severe("ERROR: creating sct Ids " + e.getMessage());
            throw new MojoExecutionException(e.getMessage(), e);
        }

        convertIdFile(new IdentifierFile(idAceFile, identifierRf2File, Boolean.parseBoolean(hasHeader)));
        if (additionalIdentifierFiles != null) {
            for (IdentifierFile idFile : additionalIdentifierFiles) {
                convertIdFile(idFile);
            }
        }
        convertConceptFile();
        convertDescriptionFile();
        convertRelationshipFile();

        try {
            uuidToSctIdConcept.cleanup(null);
        } catch (Exception e) {
            logger.severe("ERROR: Cannot write out mapped ids");
            throw new MojoExecutionException(e.getMessage(), e);
        }

        logger.info("Processed ace files.");
    }

    /**
     * THIS IS A HACK REMOVE.
     *
     * Get the namespace for the I_Path.
     *
     * @param forPath I_path
     * @return NAMESPACE
     */
    private NAMESPACE getNamespace(I_Path forPath) {
        NAMESPACE namespace = NAMESPACE.NEHTA;

        // try{
        // namespace = NAMESPACE.fromString(forPath.getNamespace());
        // } catch (Exception e) {
        if (forPath.toString().equals("SNOMED Core")) {
            namespace = NAMESPACE.SNOMED_META_DATA;
            // }
        }

        return namespace;
    }

    private PROJECT getProject(I_Path forPath) {
        PROJECT project = PROJECT.AU;

        // try{
        // namespace = NAMESPACE.fromString(forPath.getNamespace());
        // } catch (Exception e) {
        if (forPath.toString().equals("SNOMED Core")) {
            project = PROJECT.SNOMED_CT;
            // }
        }

        return project;
    }

    private NAMESPACE getNamespace(String forPath) throws TerminologyException, IOException {
        NAMESPACE namespace;

        if (namespaceCache.containsKey(forPath)) {
            namespace = namespaceCache.get(forPath);
        } else {
            namespace = getNamespace(tf.getPath(UUID.fromString(forPath)));
            namespaceCache.put(forPath, namespace);
        }

        return namespace;
    }

    /**
     * Convert the ace id file to rf2 format.
     *
     * Invalid rows will be skipped and logged.
     *
     * @throws MojoExecutionException on file open and write errors.
     */
    private void convertIdFile(IdentifierFile idFile) throws MojoExecutionException {
        int lineCount = 0;

        AceIdentifierReader aceIdentifierReader;
        Rf2IdentifierWriter rf2IdentifierWriter;
        Iterator<AceIdentifierRow> identifierIterator;

        AceIdentifierRow aceIdentifierRow;
        Rf2IdentifierRow rf2IdentifierRow;
        NAMESPACE namespace;
        PROJECT project;

        aceIdentifierReader = new AceIdentifierReader(new File(idFile.getInputFileName()));
        aceIdentifierReader.setHasHeader(idFile.isHeaderLine());
        identifierIterator = aceIdentifierReader.iterator();

        try {
            File file = new File(idFile.getOutputFileName());
            file.getParentFile().mkdirs();
            rf2IdentifierWriter = new Rf2IdentifierWriter(file);
        } catch (IOException e) {
            logger.severe("ERROR: cannot open rf2 identifier file for writting.");
            throw new MojoExecutionException(e.getMessage(), e);
        }

        rf2IdentifierRow = new Rf2IdentifierRow();

        do {
            try {
                lineCount++;

                aceIdentifierRow = identifierIterator.next();

                namespace = getNamespace(aceIdentifierRow.getPathUuid());
                project = getProject(tf.getPath(UUID.fromString(aceIdentifierRow.getPathUuid())));

                rf2IdentifierRow.setIdentifierSchemeSctId(uuidIdentifierSchemeSctId);
                rf2IdentifierRow.setAlternateIdentifier(aceIdentifierRow.getPrimaryUuid());
                rf2IdentifierRow.setEffectiveTime(getRf2Time(aceIdentifierRow.getEffectiveTime()));
                rf2IdentifierRow.setActive(getRF2ActiveFlag(aceIdentifierRow.getStatusUuid()));

                rf2IdentifierRow.setModuleSctId(getSctIdWithGeneration(aceIdentifierRow.getPathUuid(), namespace, project,
                    uuidToSctIdConcept));

                rf2IdentifierRow.setReferencedComponentSctId(aceIdentifierRow.getSourceId());

                writeIdentifierRow(rf2IdentifierWriter, rf2IdentifierRow);
                if (lineCount % 10000 == 0) {
                    logger.info("Processed " + lineCount + " Ids");
                }
            } catch (TerminologyRuntimeException tre) {
                logger.severe("ERROR: cannot process line." + lineCount + " " + tre.getMessage());
                tre.printStackTrace();
            } catch (ParseException pe) {
                logger.severe("ERROR: create RF2 time from ace time." + lineCount + " " + pe.getMessage());
                pe.printStackTrace();
            } catch (Exception e) {
                logger.severe("ERROR: Transforming." + lineCount + " " + e.getMessage());
                e.printStackTrace();
            }
        } while (identifierIterator.hasNext());

        try {
            rf2IdentifierWriter.close();
        } catch (IOException e) {
            logger.severe("ERROR: cannot close rf2 identifier file.");
            throw new MojoExecutionException(e.getMessage());
        }
    }

    /**
     * Writes the identifier row to file.
     *
     * @param rf2IdentifierWriter
     * @param rf2IdentifierRow
     *
     * @throws MojoExecutionException on write errors.
     */
    private void writeIdentifierRow(Rf2IdentifierWriter rf2IdentifierWriter, Rf2IdentifierRow rf2IdentifierRow)
            throws MojoExecutionException {
        try {
            rf2IdentifierWriter.write(rf2IdentifierRow);
        } catch (IOException e) {
            logger.severe("ERROR: writting to identifier file.");
            throw new MojoExecutionException(e.getMessage());
        } catch (TerminologyException e) {
            logger.severe("ERROR: writting to identifier file.");
            throw new MojoExecutionException(e.getMessage());
        }
    }

    /**
     * Convert the ace concept file to rf2 format.
     *
     * Invalid rows will be skipped and logged.
     *
     * @throws MojoExecutionException on file open and write errors.
     */
    private void convertConceptFile() throws MojoExecutionException {
        int lineCount = 0;
        AceConceptReader aceConceptReader;
        Rf2ConceptWriter rf2ConceptWriter;
        Iterator<AceConceptRow> conceptIterator;
        NAMESPACE namespace;
        PROJECT project;

        AceConceptRow aceConceptRow;
        Rf2ConceptRow rf2ConceptRow;

        aceConceptReader = new AceConceptReader(new File(conceptAceFile));
        aceConceptReader.setHasHeader(Boolean.parseBoolean(hasHeader));
        conceptIterator = aceConceptReader.iterator();

        try {
            File file = new File(conceptRf2File);
            file.getParentFile().mkdirs();
            rf2ConceptWriter = new Rf2ConceptWriter(file);
        } catch (IOException e) {
            logger.severe("ERROR: cannot open rf2 concept file for writting.");
            throw new MojoExecutionException(e.getMessage());
        }

        rf2ConceptRow = new Rf2ConceptRow();
        do {
            try {
                lineCount++;
                aceConceptRow = conceptIterator.next();

                namespace = getNamespace(aceConceptRow.getPathUuid());
                project = getProject(tf.getPath(UUID.fromString(aceConceptRow.getPathUuid())));


                rf2ConceptRow.setConceptSctId(getSctIdWithGeneration(aceConceptRow.getConceptId(), namespace, project,
                    uuidToSctIdConcept));
                rf2ConceptRow.setEffectiveTime(getRf2Time(aceConceptRow.getEffectiveTime()));
                rf2ConceptRow.setActive(getRF2ActiveFlag(aceConceptRow.getConceptStatusId()));
                rf2ConceptRow.setModuleSctId(getSctIdWithGeneration(aceConceptRow.getPathUuid(), namespace, project,
                    uuidToSctIdConcept));
                rf2ConceptRow.setDefiniationStatusSctId(getDefinitionSctId(aceConceptRow.getIsPrimative()));

                writeConceptsRow(rf2ConceptWriter, rf2ConceptRow);
                if (lineCount % 10000 == 0) {
                    logger.info("Processed " + lineCount + " Concepts");
                }
            } catch (TerminologyRuntimeException tre) {
                logger.severe("ERROR: cannot process line." + lineCount + " " + tre.getMessage());
                tre.printStackTrace();
            } catch (Exception e) {
                logger.severe("ERROR: Transforming." + lineCount + " " + e.getMessage());
                e.printStackTrace();
            }
        } while (conceptIterator.hasNext());

        try {
            rf2ConceptWriter.close();
        } catch (IOException e) {
            logger.severe("ERROR: cannot close rf2 concept file.");
            throw new MojoExecutionException(e.getMessage());
        }
    }

    /**
     * Writes the concept row to file.
     *
     * @param rf2ConceptWriter file writer.
     * @param rf2ConceptRowList concept row to write.
     *
     * @throws MojoExecutionException on write errors.
     */
    private void writeConceptsRow(Rf2ConceptWriter rf2ConceptWriter, Rf2ConceptRow rf2ConceptRow)
            throws MojoExecutionException {
        try {
            rf2ConceptWriter.write(rf2ConceptRow);
        } catch (IOException e) {
            logger.severe("ERROR: writting to concept file.");
            throw new MojoExecutionException(e.getMessage());
        } catch (TerminologyException e) {
            logger.severe("ERROR: writting to concept file.");
            throw new MojoExecutionException(e.getMessage());
        }
    }

    /**
     * Convert the ace description file to rf2 format.
     *
     * Invalid rows will be skipped (logged).
     *
     * @throws MojoExecutionException on file open and write errors.
     */
    private void convertDescriptionFile() throws MojoExecutionException {
        int lineCount = 0;
        AceDescriptionReader aceDescriptionReader;
        Rf2DescriptionWriter rf2DescriptionWriter;
        NAMESPACE namespace;
        PROJECT project;

        aceDescriptionReader = new AceDescriptionReader(new File(descriptionAceFile));
        aceDescriptionReader.setHasHeader(Boolean.parseBoolean(hasHeader));

        try {
            File file = new File(descriptionRf2File);
            file.getParentFile().mkdirs();
            rf2DescriptionWriter = new Rf2DescriptionWriter(file);
            new File(file.getParent()).createNewFile();

        } catch (IOException e) {
            logger.finest("ERROR: cannot open rf2 description file for writting.");
            throw new MojoExecutionException(e.getMessage());
        }

        Iterator<AceDescriptionRow> descriptionIterator = aceDescriptionReader.iterator();

        AceDescriptionRow aceDescriptionRow;
        Rf2DescriptionRow rf2DescriptionRow;

        rf2DescriptionRow = new Rf2DescriptionRow();
        do {
            try {
                lineCount++;
                aceDescriptionRow = descriptionIterator.next();

                namespace = getNamespace(aceDescriptionRow.getPathUuid());
                project = getProject(tf.getPath(UUID.fromString(aceDescriptionRow.getPathUuid())));

                rf2DescriptionRow.setDescriptionSctId(getSctIdWithGeneration(aceDescriptionRow.getDescriptionId(),
                    namespace, project, uuidToSctIdDescription));
                rf2DescriptionRow.setEffectiveTime(getRf2Time(aceDescriptionRow.getEffectiveTime()));
                rf2DescriptionRow.setActive(getRF2ActiveFlag(aceDescriptionRow.getDescriptionStatusId()));
                rf2DescriptionRow.setModuleSctId(getSctIdWithGeneration(aceDescriptionRow.getPathUuid(), namespace, project,
                    uuidToSctIdConcept));
                rf2DescriptionRow.setConceptSctId(getSctIdWithGeneration(aceDescriptionRow.getConceptId(), namespace, project,
                    uuidToSctIdConcept));
                rf2DescriptionRow.setLanaguageCode(aceDescriptionRow.getLanguageCode().toLowerCase().substring(0, 2));
                rf2DescriptionRow.setTypeSctId(getDescriptionTypeSctId(aceDescriptionRow.getDescriptionTypeId()));
                rf2DescriptionRow.setTerm(aceDescriptionRow.getTerm());
                rf2DescriptionRow.setCaseSignificaceSctId(uuidToSctIdConcept.transform(
                    caseSensitivityToUuidTransform.transform(aceDescriptionRow.getCasesensitivityId()),
                    getNamespace(tf.getPath(UUID.fromString(aceDescriptionRow.getPathUuid()))), project));

                writeDescriptionRow(rf2DescriptionWriter, rf2DescriptionRow);
                if (lineCount % 10000 == 0) {
                    logger.info("Processed " + lineCount + " Descriptions");
                }
            } catch (TerminologyRuntimeException tre) {
                logger.severe("ERROR: cannot process line." + lineCount + " " + tre.getMessage());
                tre.printStackTrace();
            } catch (Exception e) {
                logger.severe("ERROR: Transforming." + lineCount + " " + e.getMessage());
                e.printStackTrace();
            }
        } while (descriptionIterator.hasNext());

        try {
            rf2DescriptionWriter.close();
        } catch (IOException e) {
            logger.finest("ERROR: cannot close rf2 description file.");
            throw new MojoExecutionException(e.getMessage());
        }
    }

    /**
     * Writes the description row to file.
     *
     * @param rf2DescriptionWriter file writer.
     * @param Rf2DescriptionRow row.
     *
     * @throws MojoExecutionException on write errors.
     */
    private void writeDescriptionRow(Rf2DescriptionWriter rf2DescriptionWriter, Rf2DescriptionRow rf2DescriptionRow)
            throws MojoExecutionException {
        try {
            rf2DescriptionWriter.write(rf2DescriptionRow);
        } catch (IOException e) {
            logger.severe("ERROR: writting to description file.");
            throw new MojoExecutionException(e.getMessage());
        } catch (TerminologyException e) {
            logger.severe("ERROR: writting to description file.");
            throw new MojoExecutionException(e.getMessage());
        }
    }

    /**
     * Convert the ace relationship file to rf2 format.
     *
     * Invalid rows will be skipped (logged).
     *
     * @throws MojoExecutionException on file open and write errors.
     */
    private void convertRelationshipFile() throws MojoExecutionException {
        int lineCount = 0;
        AceRelationshipReader aceRelationshipReader;
        Rf2RelationshipWriter rf2RelationshipWriter;
        NAMESPACE namespace;
        PROJECT project;

        aceRelationshipReader = new AceRelationshipReader(new File(relationshipAceFile));
        aceRelationshipReader.setHasHeader(Boolean.parseBoolean(hasHeader));

        try {
            File file = new File(relationshipRf2File);
            file.getParentFile().mkdirs();
            rf2RelationshipWriter = new Rf2RelationshipWriter(file);
        } catch (IOException e) {
            logger.severe("ERROR: cannot open rf2 relationship file for writting.");
            throw new MojoExecutionException(e.getMessage());
        }

        Iterator<AceRelationshipRow> relationshipIterator = aceRelationshipReader.iterator();

        AceRelationshipRow aceRelationshipRow;
        Rf2RelationshipRow rf2RelationshipRow;

        rf2RelationshipRow = new Rf2RelationshipRow();
        do {
            try {
                lineCount++;
                aceRelationshipRow = relationshipIterator.next();

                // in RF2 historical relationships are part of a reference set
                if (!HISTORICAL_CHARACTERISTIC_UUIDS.contains(UUID.fromString(aceRelationshipRow.getCharacteristicTypeId()))) {

                    namespace = getNamespace(aceRelationshipRow.getPathUuid());
                    project = getProject(tf.getPath(UUID.fromString(aceRelationshipRow.getPathUuid())));

                    rf2RelationshipRow.setRelationshipSctId(getSctIdWithGeneration(
                        aceRelationshipRow.getRelationshipId(), namespace, project, uuidToSctIdRelationship));
                    rf2RelationshipRow.setEffectiveTime(getRf2Time(aceRelationshipRow.getEffectiveTime()));
                    rf2RelationshipRow.setActive(getRF2ActiveFlag(aceRelationshipRow.getRelationshipStatusUuid()));
                    rf2RelationshipRow.setModuleSctId(getSctIdWithGeneration(aceRelationshipRow.getPathUuid(),
                        namespace, project, uuidToSctIdConcept));
                    rf2RelationshipRow.setSourceSctId(getSctIdWithGeneration(aceRelationshipRow.getConcept1Id(),
                        namespace, project, uuidToSctIdConcept));
                    rf2RelationshipRow.setDestinationSctId(getSctIdWithGeneration(aceRelationshipRow.getConcept2Id(),
                        namespace, project, uuidToSctIdConcept));
                    rf2RelationshipRow.setRelationshipGroup(aceRelationshipRow.getRelationshipGroup());
                    rf2RelationshipRow.setTypeSctId(getSctIdWithGeneration(aceRelationshipRow.getRelationshipTypeId(),
                        namespace, project, uuidToSctIdConcept));
                    rf2RelationshipRow.setCharacteristicSctId(getSctIdWithGeneration(
                        aceRelationshipRow.getCharacteristicTypeId(), namespace, project, uuidToSctIdConcept));
                    rf2RelationshipRow.setModifierSctId(modifierSomeSctId);

                    writeRelationshipRow(rf2RelationshipWriter, rf2RelationshipRow);
                }

                if (lineCount % 10000 == 0) {
                    logger.info("Processed " + lineCount + " Relationships");
                }
            } catch (TerminologyRuntimeException tre) {
                logger.severe("ERROR: cannot process line." + lineCount + " " + tre.getMessage());
                tre.printStackTrace();
            } catch (Exception e) {
                logger.severe("ERROR: Transforming." + lineCount + " " + e.getMessage());
                e.printStackTrace();
            }
        } while (relationshipIterator.hasNext());

        try {
            rf2RelationshipWriter.close();
        } catch (IOException e) {
            logger.severe("ERROR: cannot close rf2 relationship file.");
            throw new MojoExecutionException(e.getMessage());
        }
    }

    /**
     * writes the relationship row to file.
     *
     * @param rf2RelationshipWriter file writer.
     * @param Rf2RelationshipRow relationship to write to file.
     *
     * @throws MojoExecutionException on write errors.
     */
    private void writeRelationshipRow(Rf2RelationshipWriter rf2RelationshipWriter, Rf2RelationshipRow rf2RelationshipRow)
            throws MojoExecutionException {
        try {
            rf2RelationshipWriter.write(rf2RelationshipRow);
        } catch (IOException e) {
            logger.severe("ERROR: writting to relationship file.");
            throw new MojoExecutionException(e.getMessage());
        } catch (TerminologyException e) {
            logger.severe("ERROR: writting to relationship file.");
            throw new MojoExecutionException(e.getMessage());
        }
    }

    /**
     * Checks is the uuidStatus equals the Concept.ACTIVE uuid.
     * or is a child of Concept.ACTIVE
     *
     * @param uuid String
     * @return String 1 if the uuidStatus is active otherwise 0;
     * @throws TerminologyException DB error
     * @throws IOException DB error
     */
    private String getRF2ActiveFlag(final String uuidStatus) throws IOException, TerminologyException {
        String activateFlag = "0";

        if (activeConcept.isParentOf(tf.getConcept(UUID.fromString(uuidStatus)), false)) {
            activateFlag = "1";
        } else if (activeConcept.equals(tf.getConcept(UUID.fromString(uuidStatus)))) {
            activateFlag = "1";
        }

        return activateFlag;
    }

    /**
     * Gets the sct id for the definition Uuid
     *
     * @param definitionInt String 0 = well defined 1 = primative
     *
     * @return sctid String
     */
    private String getDefinitionSctId(String definitionInt) {
        String definitionSctId;

        if (definitionInt.equals("0")) {
            definitionSctId = fullyDefinedSctId;
        } else {
            definitionSctId = primationSctId;
        }

        return definitionSctId;
    }

    /**
     * Convert the description type to a valid RF2 type
     *
     * Only FSN and Synonyms are allowed in RF2. If not a FSN the synonym type
     * is returned.
     *
     * @param descriptionTypeUuid String
     * @return String SctId FSN or Synonym.
     */
    private String getDescriptionTypeSctId(String descriptionTypeUuid) throws MojoExecutionException {
        String descriptionTypeSctId;

        if (ArchitectonicAuxiliary.Concept.SYNONYM_DESCRIPTION_TYPE.getUids().iterator().next().toString().equals(
            descriptionTypeUuid)
            || ArchitectonicAuxiliary.Concept.UNSPECIFIED_DESCRIPTION_TYPE.getUids()
                .iterator()
                .next()
                .toString()
                .equals(descriptionTypeUuid)) {
            descriptionTypeSctId = synonymSctId;
        } else if (ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.getUids()
            .iterator()
            .next()
            .toString()
            .equals(descriptionTypeUuid)) {
            descriptionTypeSctId = synonymSctId;
        } else if (ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids()
            .iterator()
            .next()
            .toString()
            .equals(descriptionTypeUuid)) {
            descriptionTypeSctId = fsnSctId;
        } else {
            throw new MojoExecutionException("Description type is not valid " + descriptionTypeUuid);
        }

        return descriptionTypeSctId;
    }

    /**
     * Attempts to return a valid RF2 time stamp from the parameter.
     *
     * @param timeStamp String
     * @return RF2 time stamp.
     *
     * @throws ParseException If a RF2 time stamp cannot be created.
     */
    public String getRf2Time(final String timeStamp) throws ParseException {
        String rf2Time = new String(timeStamp);

        try {
            if (rf2Time.contains(":")) {
                rf2Time = rf2DateFormat.format(exportDateFormat.parse(rf2Time.replace("Z", "-0000")));
            } else {
                rf2Time = rf2DateFormat.format(rf2DateFormat.parse(rf2Time));
            }
        } catch (ParseException e) {
            rf2DateFormat.parse(rf2Time);
        }

        return rf2Time;
    }

    /**
     *
     * @param uuid
     * @param namespace
     * @param idWithGeneration
     * @return
     * @throws Exception
     */
    private String getSctIdWithGeneration(String uuid, NAMESPACE namespace, PROJECT project, UuidToSctIdWithGeneration idWithGeneration)
            throws Exception {
        return idWithGeneration.transform(uuid, namespace, project);
    }

    /**
     * @param descriptionAceFile the descriptionAceFile to set
     */
    protected void setDescriptionAceFile(String descriptionAceFile) {
        this.descriptionAceFile = descriptionAceFile;
    }

    /**
     * @param relationshipAceFile the relationshipAceFile to set
     */
    protected void setRelationshipAceFile(String relationshipAceFile) {
        this.relationshipAceFile = relationshipAceFile;
    }

    /**
     * @param conceptRf2File the conceptRf2File to set
     */
    protected void setConceptRf2File(String conceptRf2File) {
        this.conceptRf2File = conceptRf2File;
    }

    /**
     * @param descriptionRf2File the descriptionRf2File to set
     */
    protected void setDescriptionRf2File(String descriptionRf2File) {
        this.descriptionRf2File = descriptionRf2File;
    }

    /**
     * @param relationshipRf2File the relationshipRf2File to set
     */
    protected void setRelationshipRf2File(String relationshipRf2File) {
        this.relationshipRf2File = relationshipRf2File;
    }

    /**
     * @param idAceFile the idAceFile to set
     */
    protected final void setIdAceFile(String idAceFile) {
        this.idAceFile = idAceFile;
    }

    /**
     * @param identifierRf2File the identifierRf2File to set
     */
    protected final void setIdentifierRf2File(String identifierRf2File) {
        this.identifierRf2File = identifierRf2File;
    }

    /**
     * @param conceptAceFile the conceptAceFile to set
     */
    protected void setConceptAceFile(String conceptAceFile) {
        this.conceptAceFile = conceptAceFile;
    }

    /**
     * @param sourceDirectory the sourceDirectory to set
     */
    protected void setSourceDirectory(String sourceDirectory) {
        this.sourceDirectory = sourceDirectory;
    }

    /**
     * @param buildDirectory the buildDirectory to set
     */
    protected void setBuildDirectory(String buildDirectory) {
        this.buildDirectory = buildDirectory;
    }
}
