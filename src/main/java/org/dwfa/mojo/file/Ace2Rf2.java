/**
 * Mojo to convert Ace file to rf2 file.
 */
package org.dwfa.mojo.file;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.maven.I_ReadAndTransform;
import org.dwfa.maven.transform.CaseSensitivityToUuidTransform;
import org.dwfa.maven.transform.IdentityTransform;
import org.dwfa.maven.transform.UuidToSctConIdWithGeneration;
import org.dwfa.maven.transform.UuidToSctDescIdWithGeneration;
import org.dwfa.maven.transform.UuidToSctIdWithGeneration;
import org.dwfa.maven.transform.UuidToSctRelIdWithGeneration;
import org.dwfa.mojo.file.AceConceptReader.AceConceptRow;
import org.dwfa.mojo.file.AceDescriptionReader.AceDescriptionRow;
import org.dwfa.mojo.file.AceRelationshipReader.AceRelationshipRow;
import org.dwfa.mojo.file.Rf2ConceptWriter.Rf2ConceptRow;
import org.dwfa.mojo.file.Rf2DescriptionWriter.Rf2DescriptionRow;
import org.dwfa.mojo.file.Rf2RelationshipWriter.Rf2RelationshipRow;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.tapi.TerminologyRuntimeException;

/**
 * @goal transformAce2Rf2
 */
public class Ace2Rf2 extends AbstractMojo {
    /**
     * Number of row to cache before writing to file.
     */
    private static final int BATCH_SIZE = 1000;

    private Logger logger = Logger.getLogger(Ace2Rf2.class.getName());

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
     * For converting uuids to sctid for status.
     */
    private I_ReadAndTransform statusIdentity = new IdentityTransform();

    /**
     * Convert ace file to rf2.
     * 
     * @see org.apache.maven.plugin.Mojo#execute()
     */
    public void execute() throws MojoExecutionException, MojoFailureException {
        try{
            ((UuidToSctIdWithGeneration) uuidToSctIdConcept).setupImpl(new File(buildDirectory), new File(sourceDirectory));
            ((UuidToSctIdWithGeneration) uuidToSctIdDescription).setupImpl(new File(buildDirectory), new File(sourceDirectory));
            ((UuidToSctIdWithGeneration) uuidToSctIdRelationship).setupImpl(new File(buildDirectory), new File(sourceDirectory));
        } catch (IOException e) {
            logger.severe("ERROR: error accessing build and/or source directories " + e.getMessage());
            throw new MojoExecutionException(e.getMessage());
        }

        convertConceptFile();
        convertDescriptionFile();
        convertRelationshipFile();
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
        List<Rf2ConceptRow> rf2ConceptRowList = new ArrayList<Rf2ConceptRow>();
        AceConceptRow aceConceptRow;
        Rf2ConceptRow rf2ConceptRow;

        aceConceptReader = new AceConceptReader(new File(conceptAceFile));
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
                rf2ConceptRow.setEffectiveTime(statusIdentity.transform(aceConceptRow.getEffectiveTime()));
                rf2ConceptRow.setActive(statusIdentity.transform(aceConceptRow.getConceptStatus()));
                //TODO need RF2 meta data.
                rf2ConceptRow.setModuleSctId("TODO RF2 metadata");
                rf2ConceptRow.setDefiniationStatusSctId(uuidToSctIdConcept.transform(aceConceptRow.getStatusUuid()));

                rf2ConceptRowList.add(rf2ConceptRow);
                
                if ((rf2ConceptRowList.size() % BATCH_SIZE) == 0) {
                    writeConceptsRows(rf2ConceptWriter, rf2ConceptRowList);
                    rf2ConceptRowList.clear();
                }

            } catch (TerminologyRuntimeException tre) {
                logger.severe("ERROR: cannot process line." + tre.getMessage());
            } catch (Exception e) {
                logger.severe("ERROR: Transforming." + e.getMessage());
            }
        } while (conceptIterator.hasNext());

        writeConceptsRows(rf2ConceptWriter, rf2ConceptRowList);
    }

    /**
     * Writes the list of concepts rows to file.
     * 
     * @param rf2ConceptWriter file writer.
     * @param rf2ConceptRowList concepts rows to write.
     * 
     * @throws MojoExecutionException on write errors.
     */
    private void writeConceptsRows(Rf2ConceptWriter rf2ConceptWriter, List<Rf2ConceptRow> rf2ConceptRowList)
            throws MojoExecutionException {
        try {
            rf2ConceptWriter.write(rf2ConceptRowList);
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
        List<Rf2DescriptionRow> rf2DescriptionRowList = new ArrayList<Rf2DescriptionRow>();

        aceDescriptionReader = new AceDescriptionReader(new File(descriptionAceFile));

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
                rf2DescriptionRow.setEffectiveTime(statusIdentity.transform(aceDescriptionRow.getEffectiveTime()));
                rf2DescriptionRow.setActive(statusIdentity.transform(aceDescriptionRow.getDescriptionStatus()));
                //TODO need RF2 meta data.
                rf2DescriptionRow.setModuleSctId("TODO RF2 metadata");
                rf2DescriptionRow.setConceptSctId(uuidToSctIdConcept.transform(aceDescriptionRow.getConceptId()));
                rf2DescriptionRow.setLanaguageCode(statusIdentity.transform(aceDescriptionRow.getLanguageCode()));
                rf2DescriptionRow.setTypeSctId(uuidToSctIdConcept.transform(aceDescriptionRow.getDescriptionTypeId()));
                rf2DescriptionRow.setTypeSctId(uuidToSctIdConcept.transform(aceDescriptionRow.getDescriptionTypeId()));
                rf2DescriptionRow.setTerm(aceDescriptionRow.getTerm());
                rf2DescriptionRow.setCaseSignificaceSctId(uuidToSctIdConcept.transform(
                        caseSensitivityToUuidTransform.transform(aceDescriptionRow.getCasesensitivityId())));
                
                rf2DescriptionRowList.add(rf2DescriptionRow);

            } catch (TerminologyRuntimeException tre) {
                logger.severe("ERROR: cannot process line." + tre.getMessage());
            } catch (Exception e) {
                logger.severe("ERROR: Transforming." + e.getMessage());
            }
            
            if ((rf2DescriptionRowList.size() % BATCH_SIZE) == 0) {
                writeDescriptionRows(rf2DescriptionWriter, rf2DescriptionRowList);
                rf2DescriptionRowList.clear();
            }
        } while (descriptionIterator.hasNext());

        writeDescriptionRows(rf2DescriptionWriter, rf2DescriptionRowList);
    }

    /**
     * Writes the list for description rows to file.
     * 
     * @param rf2DescriptionWriter file writer.
     * @param rf2DescriptionRowList list of rows.
     * 
     * @throws MojoExecutionException on write errors.
     */
    private void writeDescriptionRows(Rf2DescriptionWriter rf2DescriptionWriter,
            List<Rf2DescriptionRow> rf2DescriptionRowList) throws MojoExecutionException {
        try {
            rf2DescriptionWriter.write(rf2DescriptionRowList);
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
        List<Rf2RelationshipRow> rf2RelationshipRowList = new ArrayList<Rf2RelationshipRow>();

        aceRelationshipReader = new AceRelationshipReader(new File(relationshipAceFile));

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
                rf2RelationshipRow.setEffectiveTime(statusIdentity.transform(aceRelationshipRow.getEffectiveTime()));
                rf2RelationshipRow.setActive(statusIdentity.transform(aceRelationshipRow.getRelationshipStatus()));
                //TODO need RF2 meta data.
                rf2RelationshipRow.setModuleSctId("TODO RF2 metadata");
                rf2RelationshipRow.setSourceSctId(uuidToSctIdConcept.transform(aceRelationshipRow.getConcept1Id()));
                rf2RelationshipRow.setDestinationSctId(uuidToSctIdConcept.transform(aceRelationshipRow.getConcept1Id()));
                rf2RelationshipRow.setRelationshipGroup(aceRelationshipRow.getRelationshipGroup());
                rf2RelationshipRow.setTypeSctId(uuidToSctIdConcept.transform(aceRelationshipRow.getRelationshipTypeId()));
                rf2RelationshipRow.setCharacteristicSctId(uuidToSctIdConcept.transform(aceRelationshipRow.getCharacteristicTypeId()));
                //TODO need RF2 meta data.
                rf2RelationshipRow.setModifierSctId("TODO RF2 metadata");
                
                rf2RelationshipRowList.add(rf2RelationshipRow);

                if ((rf2RelationshipRowList.size() % BATCH_SIZE) == 0) {
                    writeRelationshipRows(rf2RelationshipWriter, rf2RelationshipRowList);
                    rf2RelationshipRowList.clear();
                }
            } catch (TerminologyRuntimeException tre) {
                logger.severe("ERROR: cannot process line." + tre.getMessage());
            } catch (Exception e) {
                logger.severe("ERROR: Transforming." + e.getMessage());
            }
        } while (relationshipIterator.hasNext());

        writeRelationshipRows(rf2RelationshipWriter, rf2RelationshipRowList);
    }

    /**
     * writes the relationship rows to file.
     * 
     * @param rf2RelationshipWriter file writer.
     * @param rf2RelationshipRowList relationships to write to file.
     * 
     * @throws MojoExecutionException on write errors.
     */
    private void writeRelationshipRows(Rf2RelationshipWriter rf2RelationshipWriter,
            List<Rf2RelationshipRow> rf2RelationshipRowList) throws MojoExecutionException {
        try {
            rf2RelationshipWriter.write(rf2RelationshipRowList);
        } catch (IOException e) {
            logger.severe("ERROR: writting to relationship file.");
            throw new MojoExecutionException(e.getMessage());
        } catch (TerminologyException e) {
            logger.severe("ERROR: writting to relationship file.");
            throw new MojoExecutionException(e.getMessage());
        }
    }    
    
    /**
     * @return the descriptionAceFile
     */
    protected String getDescriptionAceFile() {
        return descriptionAceFile;
    }

    /**
     * @param descriptionAceFile the descriptionAceFile to set
     */
    protected void setDescriptionAceFile(String descriptionAceFile) {
        this.descriptionAceFile = descriptionAceFile;
    }

    /**
     * @return the relationshipAceFile
     */
    protected String getRelationshipAceFile() {
        return relationshipAceFile;
    }

    /**
     * @param relationshipAceFile the relationshipAceFile to set
     */
    protected void setRelationshipAceFile(String relationshipAceFile) {
        this.relationshipAceFile = relationshipAceFile;
    }

    /**
     * @return the conceptRf2File
     */
    protected String getConceptRf2File() {
        return conceptRf2File;
    }

    /**
     * @param conceptRf2File the conceptRf2File to set
     */
    protected void setConceptRf2File(String conceptRf2File) {
        this.conceptRf2File = conceptRf2File;
    }

    /**
     * @return the descriptionRf2File
     */
    protected String getDescriptionRf2File() {
        return descriptionRf2File;
    }

    /**
     * @param descriptionRf2File the descriptionRf2File to set
     */
    protected void setDescriptionRf2File(String descriptionRf2File) {
        this.descriptionRf2File = descriptionRf2File;
    }

    /**
     * @return the relationshipRf2File
     */
    protected String getRelationshipRf2File() {
        return relationshipRf2File;
    }

    /**
     * @param relationshipRf2File the relationshipRf2File to set
     */
    protected void setRelationshipRf2File(String relationshipRf2File) {
        this.relationshipRf2File = relationshipRf2File;
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
