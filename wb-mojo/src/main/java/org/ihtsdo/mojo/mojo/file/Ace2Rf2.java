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
package org.ihtsdo.mojo.mojo.file;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.logging.Logger;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.ihtsdo.mojo.maven.I_ReadAndTransform;
import org.ihtsdo.mojo.maven.transform.CaseSensitivityToUuidTransform;
import org.ihtsdo.mojo.maven.transform.UuidToSctConIdWithGeneration;
import org.ihtsdo.mojo.maven.transform.UuidToSctDescIdWithGeneration;
import org.ihtsdo.mojo.maven.transform.UuidToSctIdWithGeneration;
import org.ihtsdo.mojo.maven.transform.UuidToSctRelIdWithGeneration;
import org.ihtsdo.mojo.mojo.file.AceConceptReader.AceConceptRow;
import org.ihtsdo.mojo.mojo.file.AceDescriptionReader.AceDescriptionRow;
import org.ihtsdo.mojo.mojo.file.AceIdentifierReader.AceIdentifierRow;
import org.ihtsdo.mojo.mojo.file.AceRelationshipReader.AceRelationshipRow;
import org.ihtsdo.mojo.mojo.file.Rf2ConceptWriter.Rf2ConceptRow;
import org.ihtsdo.mojo.mojo.file.Rf2DescriptionWriter.Rf2DescriptionRow;
import org.ihtsdo.mojo.mojo.file.Rf2IdentifierWriter.Rf2IdentifierRow;
import org.ihtsdo.mojo.mojo.file.Rf2RelationshipWriter.Rf2RelationshipRow;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.tapi.TerminologyRuntimeException;

/**
 * @goal transformAce2Rf2
 */
public class Ace2Rf2 extends AbstractMojo {
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
    private I_ReadAndTransform uuidToSctIdConcept = new UuidToSctConIdWithGeneration();

    /**
     * For converting uuids to sctid for descriptions.
     */
    private I_ReadAndTransform uuidToSctIdDescription = new UuidToSctDescIdWithGeneration();

    /**
     * For converting uuids to sctid for relationship.
     */
    private I_ReadAndTransform uuidToSctIdRelationship = new UuidToSctRelIdWithGeneration();

    /**
     * Converts case sensitivity to a uuid.
     */
    private I_ReadAndTransform caseSensitivityToUuidTransform = new CaseSensitivityToUuidTransform();

    /**
     * RF2 date format.
     */
    private SimpleDateFormat rf2DateFormat = new SimpleDateFormat("yyyyMMdd'T'hhmmss'Z'");

    /**
     * Convert ace file to rf2.
     * 
     * @see org.apache.maven.plugin.Mojo#execute()
     */
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            ((UuidToSctIdWithGeneration) uuidToSctIdConcept).setupImpl(new File(buildDirectory), new File(
                sourceDirectory));
            ((UuidToSctIdWithGeneration) uuidToSctIdDescription).setupImpl(new File(buildDirectory), new File(
                sourceDirectory));
            ((UuidToSctIdWithGeneration) uuidToSctIdRelationship).setupImpl(new File(buildDirectory), new File(
                sourceDirectory));
        } catch (IOException e) {
            logger.severe("ERROR: error accessing build and/or source directories " + e.getMessage());
            throw new MojoExecutionException(e.getMessage());
        }

        convertIdFile();
        convertConceptFile();
        convertDescriptionFile();
        convertRelationshipFile();
    }

    /**
     * Convert the ace id file to rf2 format.
     * 
     * Invalid rows will be skipped and logged.
     * 
     * @throws MojoExecutionException on file open and write errors.
     */
    private void convertIdFile() throws MojoExecutionException {
        AceIdentifierReader aceIdentifierReader;
        Rf2IdentifierWriter rf2IdentifierWriter;
        Iterator<AceIdentifierRow> identifierIterator;

        AceIdentifierRow aceIdentifierRow;
        Rf2IdentifierRow rf2IdentifierRow;

        aceIdentifierReader = new AceIdentifierReader(new File(idAceFile));
        aceIdentifierReader.setHasHeader(Boolean.parseBoolean(hasHeader));
        identifierIterator = aceIdentifierReader.iterator();

        try {
            rf2IdentifierWriter = new Rf2IdentifierWriter(new File(identifierRf2File));
        } catch (IOException e) {
            logger.severe("ERROR: cannot open rf2 identifier file for writting.");
            throw new MojoExecutionException(e.getMessage());
        }

        do {
            try {
                rf2IdentifierRow = rf2IdentifierWriter.new Rf2IdentifierRow();
                aceIdentifierRow = identifierIterator.next();

                // TODO RF2 meta-data
                rf2IdentifierRow.setIdentifierSchemeSctId("TODO RF2 Meta data");
                rf2IdentifierRow.setAlternateIdentifier(aceIdentifierRow.getPrimaryUuid());
                rf2IdentifierRow.setEffectiveTime(getRf2Time(aceIdentifierRow.getEffectiveTime()));
                rf2IdentifierRow.setActive(getRF2ActiveFlag(aceIdentifierRow.getStatusUuid()));
                rf2IdentifierRow.setModuleSctId(uuidToSctIdConcept.transform(aceIdentifierRow.getPathUuid()));
                rf2IdentifierRow.setReferencedComponentSctId(uuidToSctIdConcept.transform(aceIdentifierRow.getPrimaryUuid()));

                writeIdentifierRow(rf2IdentifierWriter, rf2IdentifierRow);
            } catch (TerminologyRuntimeException tre) {
                logger.severe("ERROR: cannot process line." + tre.getMessage());
            } catch (ParseException pe) {
                logger.severe("ERROR: create RF2 time from ace time." + pe.getMessage());
            } catch (Exception e) {
                logger.severe("ERROR: Transforming." + e.getMessage());
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
        AceConceptReader aceConceptReader;
        Rf2ConceptWriter rf2ConceptWriter;
        Iterator<AceConceptRow> conceptIterator;

        AceConceptRow aceConceptRow;
        Rf2ConceptRow rf2ConceptRow;

        aceConceptReader = new AceConceptReader(new File(conceptAceFile));
        aceConceptReader.setHasHeader(Boolean.parseBoolean(hasHeader));
        conceptIterator = aceConceptReader.iterator();

        try {
            rf2ConceptWriter = new Rf2ConceptWriter(new File(conceptRf2File));
        } catch (IOException e) {
            logger.severe("ERROR: cannot open rf2 concept file for writting.");
            throw new MojoExecutionException(e.getMessage());
        }

        do {
            try {
                rf2ConceptRow = rf2ConceptWriter.new Rf2ConceptRow();
                aceConceptRow = conceptIterator.next();

                rf2ConceptRow.setConceptSctId(uuidToSctIdConcept.transform(aceConceptRow.getConceptId()));
                rf2ConceptRow.setEffectiveTime(aceConceptRow.getEffectiveTime());
                rf2ConceptRow.setActive(aceConceptRow.getConceptStatus());
                rf2ConceptRow.setModuleSctId(uuidToSctIdConcept.transform(aceConceptRow.getPathUuid()));
                rf2ConceptRow.setDefiniationStatusSctId(uuidToSctIdConcept.transform(aceConceptRow.getStatusUuid()));

                writeConceptsRow(rf2ConceptWriter, rf2ConceptRow);
            } catch (TerminologyRuntimeException tre) {
                logger.severe("ERROR: cannot process line." + tre.getMessage());
            } catch (Exception e) {
                logger.severe("ERROR: Transforming." + e.getMessage());
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
        AceDescriptionReader aceDescriptionReader;
        Rf2DescriptionWriter rf2DescriptionWriter;

        aceDescriptionReader = new AceDescriptionReader(new File(descriptionAceFile));
        aceDescriptionReader.setHasHeader(Boolean.parseBoolean(hasHeader));

        try {
            rf2DescriptionWriter = new Rf2DescriptionWriter(new File(descriptionRf2File));
        } catch (IOException e) {
            logger.finest("ERROR: cannot open rf2 description file for writting.");
            throw new MojoExecutionException(e.getMessage());
        }

        Iterator<AceDescriptionRow> descriptionIterator = aceDescriptionReader.iterator();

        AceDescriptionRow aceDescriptionRow;
        Rf2DescriptionRow rf2DescriptionRow;

        do {
            try {
                rf2DescriptionRow = rf2DescriptionWriter.new Rf2DescriptionRow();
                aceDescriptionRow = descriptionIterator.next();

                rf2DescriptionRow.setDescriptionSctId(uuidToSctIdDescription.transform(aceDescriptionRow.getDescriptionId()));
                rf2DescriptionRow.setEffectiveTime(aceDescriptionRow.getEffectiveTime());
                rf2DescriptionRow.setActive(aceDescriptionRow.getDescriptionStatus());
                rf2DescriptionRow.setModuleSctId(uuidToSctIdConcept.transform(aceDescriptionRow.getPathUuid()));
                rf2DescriptionRow.setConceptSctId(uuidToSctIdConcept.transform(aceDescriptionRow.getConceptId()));
                rf2DescriptionRow.setLanaguageCode(aceDescriptionRow.getLanguageCode());
                rf2DescriptionRow.setTypeSctId(uuidToSctIdConcept.transform(aceDescriptionRow.getDescriptionTypeId()));// covert
                // from
                // 3
                // to
                // 2,
                // prefered
                // term
                // is
                // now
                // synonym
                rf2DescriptionRow.setTerm(aceDescriptionRow.getTerm());
                rf2DescriptionRow.setCaseSignificaceSctId(uuidToSctIdConcept.transform(caseSensitivityToUuidTransform.transform(aceDescriptionRow.getCasesensitivityId())));

                writeDescriptionRow(rf2DescriptionWriter, rf2DescriptionRow);
            } catch (TerminologyRuntimeException tre) {
                logger.severe("ERROR: cannot process line." + tre.getMessage());
            } catch (Exception e) {
                logger.severe("ERROR: Transforming." + e.getMessage());
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
        AceRelationshipReader aceRelationshipReader;
        Rf2RelationshipWriter rf2RelationshipWriter;

        aceRelationshipReader = new AceRelationshipReader(new File(relationshipAceFile));
        aceRelationshipReader.setHasHeader(Boolean.parseBoolean(hasHeader));

        try {
            rf2RelationshipWriter = new Rf2RelationshipWriter(new File(relationshipRf2File));
        } catch (IOException e) {
            logger.severe("ERROR: cannot open rf2 relationship file for writting.");
            throw new MojoExecutionException(e.getMessage());
        }

        Iterator<AceRelationshipRow> relationshipIterator = aceRelationshipReader.iterator();

        AceRelationshipRow aceRelationshipRow;
        Rf2RelationshipRow rf2RelationshipRow;

        do {
            try {
                rf2RelationshipRow = rf2RelationshipWriter.new Rf2RelationshipRow();
                aceRelationshipRow = relationshipIterator.next();

                rf2RelationshipRow.setRelationshipSctId(uuidToSctIdRelationship.transform(aceRelationshipRow.getRelationshipId()));
                rf2RelationshipRow.setEffectiveTime(aceRelationshipRow.getEffectiveTime());
                rf2RelationshipRow.setActive(aceRelationshipRow.getRelationshipStatus());
                rf2RelationshipRow.setModuleSctId(uuidToSctIdConcept.transform(aceRelationshipRow.getPathUuid()));
                rf2RelationshipRow.setSourceSctId(uuidToSctIdConcept.transform(aceRelationshipRow.getConcept1Id()));
                rf2RelationshipRow.setDestinationSctId(uuidToSctIdConcept.transform(aceRelationshipRow.getConcept1Id()));
                rf2RelationshipRow.setRelationshipGroup(aceRelationshipRow.getRelationshipGroup());
                rf2RelationshipRow.setTypeSctId(uuidToSctIdConcept.transform(aceRelationshipRow.getRelationshipTypeId()));
                rf2RelationshipRow.setCharacteristicSctId(uuidToSctIdConcept.transform(aceRelationshipRow.getCharacteristicTypeId()));
                // TODO need RF2 meta data.
                rf2RelationshipRow.setModifierSctId("TODO RF2 metadata");

                writeRelationshipRow(rf2RelationshipWriter, rf2RelationshipRow);
            } catch (TerminologyRuntimeException tre) {
                logger.severe("ERROR: cannot process line." + tre.getMessage());
            } catch (Exception e) {
                logger.severe("ERROR: Transforming." + e.getMessage());
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
     * 
     * @param uuid String
     * @return String 1 if the uuidStatus is active otherwise 0;
     */
    private String getRF2ActiveFlag(final String uuidStatus) {
        String activateFlag = "0";

        if (ArchitectonicAuxiliary.Concept.ACTIVE.getUids().iterator().next().toString().equals(uuidStatus)) {
            activateFlag = "1";
        }

        return activateFlag;
    }

    /**
     * Attempts to return a valid RF2 time stamp from the parameter.
     * 
     * @param timeStamp String
     * @return RF2 time stamp.
     * 
     * @throws ParseException If a RF2 time stamp cannot be created.
     */
    private String getRf2Time(final String timeStamp) throws ParseException {
        String rf2Time = new String(timeStamp);

        try {
            rf2DateFormat.parse(rf2Time);
        } catch (ParseException e) {
            logger.info("Attemting to create an RF2 date from " + timeStamp);
            rf2Time += "T000000Z";
            rf2DateFormat.parse(rf2Time);
        }

        return rf2Time;
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
