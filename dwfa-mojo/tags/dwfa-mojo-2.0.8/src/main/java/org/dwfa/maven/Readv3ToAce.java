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
package org.dwfa.maven;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.cement.RefsetAuxiliary.Concept;
import org.dwfa.tapi.I_ConceptualizeUniversally;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.id.Type5UuidFactory;

/**
 * Convert readv3 files to a set of <a
 * href='https://mgr.cubit.aceworkspace.net/apps/dwfa/ace-mojo/dataimport.html'>ACE
 * formatted</a> files.
 * 
 * @goal readv3-to-ace
 * @phase process-resources
 */

public class Readv3ToAce extends AbstractMojo {

	/**
	 * Location of the build directory.
	 * 
	 * @parameter expression="${project.build.directory}"
	 * @required
	 */
	private File outputDirectory;

	/**
	 * Location of the readv3 data directory to read from.
	 * 
	 * @parameter expression="${project.build.directory}/generated-resources/net/nhs/uktc/v3"
	 * @required
	 */
	private File readv3dir;

	/**
	 * Location of the ace data directory to write to.
	 * 
	 * @parameter expression="${project.build.directory}/classes/ace"
	 * @required
	 */
	private File aceDir;

	/**
	 * The effective date to associate with all changes.
	 * 
	 * @parameter
	 * @required
	 */
	private String effectiveDate;

	/**
	 * The path name to associate with all changes.
	 * 
	 * @parameter
	 * @required
	 */
	private String pathFsDesc;

	public void execute() throws MojoExecutionException, MojoFailureException {
		try {
			getLog().info("readv3 dir: " + readv3dir);
			getLog().info("ACE dir: " + aceDir);
			aceDir.mkdirs();



            Writer intRefset = new BufferedWriter(new FileWriter(new File(
                                                                          aceDir, "integer.refset")));

            Writer conceptRefset = new BufferedWriter(new FileWriter(new File(
                                                                          aceDir, "concept.refset")));

            Writer stringRefset = new BufferedWriter(new FileWriter(new File(
                                                                              aceDir, "string.refset")));

			UUID pathUUID = Type5UuidFactory.get(
					Type5UuidFactory.PATH_ID_FROM_FS_DESC, pathFsDesc);

			Map<String, String> termidReadCodeMap = new HashMap<String, String>();

			Map<String, String> termidDescTypeMap = new HashMap<String, String>();
			
			Map<String, List<UUID>> termIdDescIdMap = new HashMap<String, List<UUID>>();

			processDescriptions(conceptRefset, pathUUID, termidReadCodeMap, termidDescTypeMap, termIdDescIdMap);

			processRelationships(intRefset, conceptRefset, pathUUID);
            
			/* keys create */
            BufferedReader keyFileReader = new BufferedReader(
                                                              new FileReader(new File(readv3dir, "Keys.v3")));
			while (keyFileReader.ready()) {
			    writeKeyData(keyFileReader, stringRefset, pathUUID, termIdDescIdMap);
			}
            keyFileReader.close();

			intRefset.close();
			conceptRefset.close();
			stringRefset.close();

		} catch (FileNotFoundException e) {
			throw new MojoExecutionException(e.getMessage(), e);
		} catch (IOException e) {
			throw new MojoExecutionException(e.getMessage(), e);
		} catch (NoSuchAlgorithmException e) {
			throw new MojoExecutionException(e.getMessage(), e);
		} catch (Exception e) {
			throw new MojoExecutionException(e.getMessage(), e);
		}

	}

    private void processRelationships(Writer intRefset, Writer conceptRefset, UUID pathUUID)
            throws FileNotFoundException, IOException, NoSuchAlgorithmException, UnsupportedEncodingException {
        /* Relationship Create */
        BufferedReader relationshipFileReader = new BufferedReader(
                                                                   new FileReader(new File(readv3dir, "V3hier.v3")));
        Writer relationships = new BufferedWriter(new FileWriter(new File(
                                                                          aceDir, "relationships.txt")));
        while (relationshipFileReader.ready()) {
        	writeRelationshipData(relationshipFileReader, relationships, intRefset, pathUUID);
        }
        relationshipFileReader.close();
        /* template create */
        BufferedReader templateFileReader = new BufferedReader(
                                                               new FileReader(new File(readv3dir, "Template.v3")));
        
        while (templateFileReader.ready()) {
            writeTemplateData(templateFileReader, relationships, intRefset, conceptRefset, pathUUID);
        }
        templateFileReader.close();

        
        relationships.close();
    }

    private void processDescriptions(Writer conceptRefset, UUID pathUUID, Map<String, String> termidReadCodeMap,
        Map<String, String> termidDescTypeMap, Map<String, List<UUID>> termIdDescIdMap) throws FileNotFoundException,
            IOException, Exception, NoSuchAlgorithmException, UnsupportedEncodingException {
        // Description file
        BufferedReader descriptionFileReader = new BufferedReader(
                                                                  new FileReader(new File(readv3dir, "Descrip.v3")));
        setupDescriptionIndex(descriptionFileReader, termidReadCodeMap,
        		termidDescTypeMap);
         descriptionFileReader.close();


        /* Description create */
        BufferedReader termsFileReader = new BufferedReader(new FileReader(
                                                                           new File(readv3dir, "Terms.v3")));
        BufferedReader conceptFileReader = new BufferedReader(
                                                              new FileReader(new File(readv3dir, "Concept.v3")));


        Writer concepts = new BufferedWriter(new FileWriter(new File(
                                                                     aceDir, "concepts.txt")));
        Writer descriptions = new BufferedWriter(new FileWriter(new File(
                                                                         aceDir, "descriptions.txt")));

        while (termsFileReader.ready()) {
        	writeDescriptionData(termsFileReader, conceptFileReader, conceptRefset, concepts, descriptions,
                                               pathUUID, termidReadCodeMap, termidDescTypeMap, termIdDescIdMap);
        }
        termsFileReader.close();
        conceptFileReader.close();
        concepts.close();
        descriptions.close();
    }

    private void writeKeyData(BufferedReader keyFileReader, Writer stringRefset, UUID pathUUID, Map<String, List<UUID>> termIdDescIdMap) throws IOException, Exception, NoSuchAlgorithmException,
    UnsupportedEncodingException {
        int keyIndex = 0;
        int termidIndex = 1;
        int keyTypeIndex = 2;
        String line = keyFileReader.readLine();
        if (line != null && line.length() > 5) {
            String[] termparts = line.split("\\|");

            // refset UUID
            if (termparts[keyTypeIndex].equals("W")) {
                stringRefset.append(RefsetAuxiliary.Concept.CTV3_WORD_KEYS.getUids().iterator().next().toString());
            } else if (termparts[keyTypeIndex].equals("P")) {
                stringRefset.append(RefsetAuxiliary.Concept.CTV3_PARTIAL_WORD_KEYS.getUids().iterator().next().toString());
            } else if (termparts[keyTypeIndex].equals("A")) {
                stringRefset.append(RefsetAuxiliary.Concept.CTV3_ACRONYM_KEYS.getUids().iterator().next().toString());
            } else {
                throw new Exception("Can't handle key type: " + termparts[keyTypeIndex]);
            }
            stringRefset.append("\t");
            // member UUID
            stringRefset.append(Type5UuidFactory.get(Type5UuidFactory.READV3_KEY_MEMBER_ID,
                                                     termparts[keyIndex]+termparts[termidIndex]).toString());
            stringRefset.append("\t");
            // status UUID
            stringRefset.append(ArchitectonicAuxiliary.Concept.CURRENT.getUids().iterator().next().toString());
            stringRefset.append("\t");
            
            // component UUID
            if (termIdDescIdMap.get(termparts[termidIndex]) != null) {
               // getLog().info("termId: " + termparts[termidIndex]);
               // getLog().info("termIdDescIdMap size: " + termIdDescIdMap.size());
               // getLog().info("termIdDescIdMap.get(termparts[termidIndex]): " + termIdDescIdMap.get(termparts[termidIndex]));
                stringRefset.append(termIdDescIdMap.get(termparts[termidIndex]).get(0).toString());
            } else {
                throw new Exception("Description UUID is null for " + termparts[termidIndex] + " on line: " + line);
            }
            
            stringRefset.append("\t");
            
            // effective date
            stringRefset.append(effectiveDate);
            stringRefset.append("\t");
            
            // path UUID
            stringRefset.append(pathUUID.toString());
            stringRefset.append("\t");
            
            // String extension
            stringRefset.append(termparts[keyIndex]);
            stringRefset.append("\n");
        }
        
    }

    private String writeDescriptionData(BufferedReader termsFileReader, BufferedReader conceptFileReader, Writer conceptRefset,
        Writer concepts, Writer descriptions, UUID pathUUID, Map<String, String> termidReadCodeMap,
        Map<String, String> termidDescTypeMap, Map<String, List<UUID>> termIdDescIdMap) throws IOException, Exception, NoSuchAlgorithmException,
            UnsupportedEncodingException {
        int termidIndex = 0;
        int termStatusIndex = 1;
        int term30Index = 2;
        int term60Index = 3;
        int term198Index = 4;

        String line = termsFileReader.readLine();
        if (line != null && line.length() > 5) {

        	String[] termparts = line.split("\\|");

        	String descriptionType = termidDescTypeMap
        			.get(termparts[termidIndex]);

        	ArchitectonicAuxiliary.Concept descriptionTypeConcept = null;

        	if (descriptionType != null) {

        		if (descriptionType.equals("P")) {
        			descriptionTypeConcept = ArchitectonicAuxiliary.Concept.READ_30_DESC;
        		} else if (descriptionType.equals("S")) {
        			descriptionTypeConcept = ArchitectonicAuxiliary.Concept.READ_SYN_30_DESC;
        		} else {
                    throw new Exception("Can't handle description type of : " + descriptionType);
                }

        		UUID descUuid = writeDescription(descriptions, pathUUID,
        				termidReadCodeMap, termidIndex,
        				termStatusIndex, termparts[term30Index],
        				termparts,
        				Type5UuidFactory.READV3_TERM30_DESC_ID,
        				descriptionTypeConcept);
        		ArrayList<UUID> idList = new ArrayList<UUID>();
        		idList.add(descUuid);
        		termIdDescIdMap.put(termparts[termidIndex], idList);
        		
        		
        		if (termparts.length > term60Index) {
                    if (descriptionType.equals("P")) {
                        descriptionTypeConcept = ArchitectonicAuxiliary.Concept.READ_60_DESC;
                    } else if (descriptionType.equals("S")) {
                        descriptionTypeConcept = ArchitectonicAuxiliary.Concept.READ_SYN_60_DESC;
                    } else {
                        throw new Exception("Can't handle description type of : " + descriptionType);
                    }
                    descUuid =writeDescription(descriptions, pathUUID,
        					termidReadCodeMap, termidIndex,
        					termStatusIndex, termparts[term60Index],
        					termparts,
        					Type5UuidFactory.READV3_TERM60_DESC_ID,
        					descriptionTypeConcept);
                    idList.add(descUuid);
        		}
        		if (termparts.length > term198Index) {
                    if (descriptionType.equals("P")) {
                        descriptionTypeConcept = ArchitectonicAuxiliary.Concept.READ_198_DESC;
                    } else if (descriptionType.equals("S")) {
                        descriptionTypeConcept = ArchitectonicAuxiliary.Concept.READ_SYN_198_DESC;
                    } else {
                        throw new Exception("Can't handle description type of : " + descriptionType);
                    }
                    descUuid = writeDescription(descriptions, pathUUID,
        					termidReadCodeMap, termidIndex,
        					termStatusIndex, termparts[term198Index],
        					termparts,
        					Type5UuidFactory.READV3_TERM198_DESC_ID,
        					descriptionTypeConcept);
                    idList.add(descUuid);
        		}
        	}
        }

        /* Concept Create */

        while (conceptFileReader.ready()) {
        	writeConceptData(conceptFileReader, conceptRefset, concepts, pathUUID);
        }
        return line;
    }

    private String writeConceptData(BufferedReader conceptFileReader, Writer conceptRefset, Writer concepts, UUID pathUUID)
            throws IOException, NoSuchAlgorithmException, UnsupportedEncodingException, Exception {
        int readcodeIndex = 0;
        int conceptStatusIndex = 1;
        int linguisticIndex = 2;
        int subjectTypeIndex = 3;

        String line2 = conceptFileReader.readLine();
        if (line2 != null && line2.length() > 5) {

        	String[] conceptparts = line2.split("\\|");

        	concepts.append(Type5UuidFactory.get(
        			Type5UuidFactory.READV3_CONCEPT_ID,
        			conceptparts[readcodeIndex])
        			.toString());
        	concepts.append("\t");

        	if (conceptparts[conceptStatusIndex].equals("O")) {

        		concepts
        				.append(ArchitectonicAuxiliary.Concept.OPTIONAL
        						.getUids().iterator().next()
        						.toString());

        	} else if (conceptparts[conceptStatusIndex].equals("C")) {

        		concepts
        				.append(ArchitectonicAuxiliary.Concept.CURRENT
        						.getUids().iterator().next()
        						.toString());

        	} else if (conceptparts[conceptStatusIndex].equals("R")) {

        		concepts
        				.append(ArchitectonicAuxiliary.Concept.RETIRED
        						.getUids().iterator().next()
        						.toString());

        	} else if (conceptparts[conceptStatusIndex].equals("E")) {

        		concepts
        				.append(ArchitectonicAuxiliary.Concept.EXTINCT
        						.getUids().iterator().next()
        						.toString());
        	} else {

        		throw new Exception("Cant handle :"
        				+ conceptparts[conceptStatusIndex]);

        	}

        	concepts.append("\t");

        	concepts.append("1");
        	concepts.append("\t");

        	concepts.append(effectiveDate);
        	concepts.append("\t");

        	concepts.append(pathUUID.toString());
        	concepts.append("\n");
        	
        	if  (conceptparts[linguisticIndex].equals("A")) {
                appendConceptRefset(conceptRefset, RefsetAuxiliary.Concept.CTV3_LINGUISTIC_ROLE, pathUUID, readcodeIndex, linguisticIndex, 
                                    Type5UuidFactory.READV3_LINGUISTIC_ROLE_MEMBER_ID, conceptparts, 
                                    RefsetAuxiliary.Concept.ATTRIBUTE_LINGUISTIC_ROLE.getUids().iterator().next());
        	} else if (conceptparts[linguisticIndex].equals("N")) {
                appendConceptRefset(conceptRefset, RefsetAuxiliary.Concept.CTV3_LINGUISTIC_ROLE, pathUUID, readcodeIndex, linguisticIndex, 
                                    Type5UuidFactory.READV3_LINGUISTIC_ROLE_MEMBER_ID, conceptparts, 
                                    RefsetAuxiliary.Concept.NON_ATTRIBUTE_LINGUISTIC_ROLE.getUids().iterator().next());
        	}  else {
        	    throw new IOException("Can't handle linguistic role: " + conceptparts[linguisticIndex]);
        	}
            appendConceptRefset(conceptRefset, RefsetAuxiliary.Concept.CTV3_SUBJECT_TYPE, pathUUID, readcodeIndex, subjectTypeIndex, 
                                Type5UuidFactory.READV3_SUBJECT_TYPE_ID, conceptparts,
                                Type5UuidFactory.get(Type5UuidFactory.READV3_CONCEPT_ID,
                                                     conceptparts[subjectTypeIndex]));

            
        	// subject type refset
        	
        	

        }
        return line2;
    }

    private void appendConceptRefset(Writer conceptRefset, I_ConceptualizeUniversally refsetType, UUID pathUUID, int readcodeIndex, int extensionIndex,
        UUID memberTypeFivePrefix, String[] conceptparts, UUID extensionConceptUUID) throws IOException, NoSuchAlgorithmException, UnsupportedEncodingException, TerminologyException {
        // linguistic role refset
        // refset UUID
        conceptRefset.append(refsetType.getUids().iterator().next().toString());
        conceptRefset.append("\t");
        // member UUID
        conceptRefset.append(Type5UuidFactory.get(memberTypeFivePrefix,
                                                  conceptparts[readcodeIndex]).toString());
        conceptRefset.append("\t");
        // status UUID
        conceptRefset.append(ArchitectonicAuxiliary.Concept.CURRENT.getUids().iterator().next().toString());
        conceptRefset.append("\t");
        // component UUID
        conceptRefset.append(Type5UuidFactory.get(Type5UuidFactory.READV3_CONCEPT_ID,
                                                  conceptparts[readcodeIndex]).toString());
        conceptRefset.append("\t");
        
        // effective date
        conceptRefset.append(effectiveDate);
        conceptRefset.append("\t");
        
        // path UUID
        conceptRefset.append(pathUUID.toString());
        conceptRefset.append("\t");
        
        // concept extension
        conceptRefset.append(extensionConceptUUID.toString());
        conceptRefset.append("\n");
    }
    
    private void writeTemplateData(BufferedReader templateFileReader, Writer relationships, Writer intRefset,
        Writer conceptRefset, UUID pathUUID) throws IOException, NoSuchAlgorithmException, UnsupportedEncodingException {
        int objectReadCodeIndex = 0;
        int attributeReadCodeIndex = 1;
        int valueReadCodeIndex = 2;
        int valueTypeIndex = 3;
        int cardinalityIndex = 4;
        int semanticStatusIndex = 5;
        int browseAttributeOrderIndex = 6;
        int browseValueOrderIndex = 7;
        int notesScreenOrderIndex = 8;
        int attributeDisplayStatusIndex = 9;
        int characteristicStatusIndex = 10;

        
        String templateLine = templateFileReader.readLine();
        if (templateLine != null && templateLine.length() > 5) {

            String[] templatePart = templateLine.split("\\|");

            relationships.append(Type5UuidFactory.get(Type5UuidFactory.READV3_REL_ID,
                    templatePart[objectReadCodeIndex]+
                    templatePart[attributeReadCodeIndex]+
                    templatePart[valueReadCodeIndex]).toString());
            relationships.append("\t");

            relationships.append(ArchitectonicAuxiliary.Concept.CURRENT.getUids().iterator().next().toString());
            relationships.append("\t");

            relationships.append(Type5UuidFactory.get(Type5UuidFactory.READV3_CONCEPT_ID,
                    templatePart[objectReadCodeIndex]).toString());
            relationships.append("\t");

            relationships
                    .append(Type5UuidFactory.get(Type5UuidFactory.READV3_CONCEPT_ID,
                                                 templatePart[attributeReadCodeIndex]).toString());
            relationships.append("\t");

            relationships
                    .append(Type5UuidFactory.get(Type5UuidFactory.READV3_CONCEPT_ID,
                            templatePart[valueReadCodeIndex]).toString());
            relationships.append("\t");

            relationships
                    .append(ArchitectonicAuxiliary.Concept.ADDITIONAL_CHARACTERISTIC
                            .getUids().iterator().next().toString());
            relationships.append("\t");
            // refine
            if ( templatePart[semanticStatusIndex].equals("F")) {
                relationships
                .append(ArchitectonicAuxiliary.Concept.NOT_REFINABLE
                        .getUids().iterator().next().toString());
            } else {
                relationships
                .append(ArchitectonicAuxiliary.Concept.OPTIONAL_REFINABILITY
                        .getUids().iterator().next().toString());
            }
            relationships.append("\t");
            // group
            relationships.append("0");
            relationships.append("\t");

            relationships.append(effectiveDate);
            relationships.append("\t");

            relationships.append(pathUUID.toString());
            relationships.append("\n");
            
            
            writeIntRefsetEntryAndGenerateId(intRefset, pathUUID, objectReadCodeIndex, attributeReadCodeIndex,
                                             valueReadCodeIndex, templatePart, 
                                             cardinalityIndex, 
                                             Type5UuidFactory.READV3_TEMPL_CARDINALITY_MEMBER_ID, 
                                             RefsetAuxiliary.Concept.CTV3_TEMPLATE_CARDINALITY);

            writeIntRefsetEntryAndGenerateId(intRefset, pathUUID, objectReadCodeIndex, attributeReadCodeIndex,
                                             valueReadCodeIndex, templatePart, 
                                             browseAttributeOrderIndex, 
                                             Type5UuidFactory.READV3_TEMPL_BROWSE_ATTRIBUTE_ORDER_MEMBER_ID, 
                                             RefsetAuxiliary.Concept.CTV3_TEMPLATE_BROWSE_ATTRIBUTE);
            writeIntRefsetEntryAndGenerateId(intRefset, pathUUID, objectReadCodeIndex, attributeReadCodeIndex,
                                             valueReadCodeIndex, templatePart, 
                                             browseValueOrderIndex, 
                                             Type5UuidFactory.READV3_TEMPL_BROWSE_VALUE_ORDER_MEMBER_ID, 
                                             RefsetAuxiliary.Concept.CTV3_TEMPLATE_BROWSE_VALUE);
            writeIntRefsetEntryAndGenerateId(intRefset, pathUUID, objectReadCodeIndex, attributeReadCodeIndex,
                                             valueReadCodeIndex, templatePart, 
                                             notesScreenOrderIndex, 
                                             Type5UuidFactory.READV3_TEMPL_NOTES_SCREEN_ORDER_MEMBER_ID, 
                                             RefsetAuxiliary.Concept.CTV3_TEMPLATE_NOTES_SCREEN);
            
            
            Concept extensionConcept = null;
            int index = valueTypeIndex;
            if (templatePart[index].equals("C")) {
                extensionConcept = Concept.TEMPLATE_CODE_VALUE_TYPE;
            } else if (templatePart[index].equals("N")) {
                extensionConcept = Concept.TEMPLATE_NUMBER_VALUE_TYPE;
            } else if (templatePart[index].equals("D")) {
                extensionConcept = Concept.TEMPLATE_DATE_VALUE_TYPE;
            } else {
                throw new IOException("a. Can't handle " +
                                    templatePart[index] + " for index " + index);
            }
            writeConRefsetEntryAndGenerateId(conceptRefset, pathUUID, objectReadCodeIndex, attributeReadCodeIndex,
                                             valueReadCodeIndex, templatePart, 
                                             extensionConcept, 
                                             Type5UuidFactory.READV3_TEMPL_VALUE_TYPE_MEMBER_ID, 
                                             RefsetAuxiliary.Concept.CTV3_TEMPLATE_VALUE_TYPE);
            
            
            extensionConcept = null;
            index = semanticStatusIndex;
            if (templatePart[index].equals("F")) {
                extensionConcept = Concept.TEMPLATE_FINAL_SEMANTIC_STATUS;
            } else if (templatePart[index].equals("R")) {
                extensionConcept = Concept.TEMPLATE_REFINABLE_SEMANTIC_STATUS;
            } else if (templatePart[index].equals("N")) {
                extensionConcept = Concept.TEMPLATE_NUMERIC_QUALIFIER_REFINE_SEMANTIC_STATUS;
            } else if (templatePart[index].equals("M")) {
                extensionConcept = Concept.TEMPLATE_MANDATORY_TO_REFINE_SEMANTIC_STATUS;
            } else if (templatePart[index].equals("C")) {
                extensionConcept = Concept.TEMPLATE_CHILD_REFINE_SEMANTIC_STATUS;
            } else if (templatePart[index].equals("Q")) {
                extensionConcept = Concept.TEMPLATE_QUALIFIER_REFINE_SEMANTIC_STATUS;
            } else if (templatePart[index].equals("U")) {
                extensionConcept = Concept.TEMPLATE_UNSPECIFIED_SEMANTIC_STATUS;
            } else {
                throw new IOException("b. Can't handle " +
                                    templatePart[index] + " for index " + index);
            }
            writeConRefsetEntryAndGenerateId(conceptRefset, pathUUID, objectReadCodeIndex, attributeReadCodeIndex,
                                             valueReadCodeIndex, templatePart, 
                                             extensionConcept, 
                                             Type5UuidFactory.READV3_TEMPL_SEMANTIC_STATUS_MEMBER_ID, 
                                             RefsetAuxiliary.Concept.CTV3_TEMPLATE_SEMANTIC_STATUS);
            
            
            extensionConcept = null;
            index = attributeDisplayStatusIndex;
            if (templatePart[index].equals("D")) {
                extensionConcept = Concept.TEMPLATE_ATTRIBUTE_DISPLAYED;
            } else if (templatePart[index].equals("H")) {
                extensionConcept = Concept.TEMPLATE_ATTRIBUTE_HIDDEN;
            } else if (templatePart[index].equals("U")) {
                extensionConcept = Concept.TEMPLATE_ATTRIBUTE_UNSPECIFIED;
            } else {
                throw new IOException("c. Can't handle " +
                                    templatePart[index] + " for index " + index);
            }
            writeConRefsetEntryAndGenerateId(conceptRefset, pathUUID, objectReadCodeIndex, attributeReadCodeIndex,
                                             valueReadCodeIndex, templatePart, 
                                             extensionConcept, 
                                             Type5UuidFactory.READV3_TEMPL_ATTRIBUTE_DISPLAYSTATUS_MEMBER_ID, 
                                             RefsetAuxiliary.Concept.CTV3_TEMPLATE_DISPLAY_STATUS);
            
            
            extensionConcept = null;
            index = characteristicStatusIndex;
            if (templatePart[index].equals("Q")) {
                extensionConcept = Concept.TEMPLATE_CHARACTERSITIC_QUALIFIER;
            } else if (templatePart[index].equals("A")) {
                extensionConcept = Concept.TEMPLATE_CHARACTERSITIC_ATOM;
            } else if (templatePart[index].equals("F")) {
                extensionConcept = Concept.TEMPLATE_CHARACTERSITIC_FACT;
            } else {
                throw new IOException("d. Can't handle " +
                                    templatePart[index] + " for index " + index);
            }
            writeConRefsetEntryAndGenerateId(conceptRefset, pathUUID, objectReadCodeIndex, attributeReadCodeIndex,
                                             valueReadCodeIndex, templatePart, 
                                             extensionConcept, 
                                             Type5UuidFactory.READV3_TEMPL_CHARACTERISTIC_STATUS_MEMBER_ID, 
                                             RefsetAuxiliary.Concept.CTV3_TEMPLATE_CHARACTERISTIC_STATUS);
            
            
            

        }
    }

    private void writeConRefsetEntryAndGenerateId(Writer conceptRefset, UUID pathUUID, int objectReadCodeIndex,
        int attributeReadCodeIndex, int valueReadCodeIndex, String[] templatePart, Concept conceptExtension,
        UUID type5prefix, Concept refsetIdentity) throws NoSuchAlgorithmException, UnsupportedEncodingException, IOException {
        UUID memberId = Type5UuidFactory.get(type5prefix,
                                             templatePart[objectReadCodeIndex]+
                                             templatePart[attributeReadCodeIndex]+
                                             templatePart[valueReadCodeIndex]);
        writeConRefsetEntry(conceptRefset, pathUUID, objectReadCodeIndex, attributeReadCodeIndex, valueReadCodeIndex, templatePart,
                            refsetIdentity, 
                            memberId, 
                            conceptExtension);
        
    }

    private void writeConRefsetEntry(Writer intRefset, UUID pathUUID, int objectReadCodeIndex, int attributeReadCodeIndex,
        int valueReadCodeIndex,
        String[] relationshipParts, Concept refset, UUID memberId, Concept conceptExtension) throws IOException,
            NoSuchAlgorithmException, UnsupportedEncodingException {
        // refset UUID
        intRefset.append(refset.getUids().iterator().next().toString());
        intRefset.append("\t");
        // member UUID
        intRefset.append(memberId.toString());
        intRefset.append("\t");
        // status UUID
        intRefset.append(ArchitectonicAuxiliary.Concept.CURRENT.getUids().iterator().next().toString());
        intRefset.append("\t");
        // component UUID
         
        intRefset.append(Type5UuidFactory.get(Type5UuidFactory.READV3_REL_ID,
                                                  relationshipParts[objectReadCodeIndex]+
                                                  relationshipParts[attributeReadCodeIndex]+
                                                  relationshipParts[valueReadCodeIndex]).toString());
        intRefset.append("\t");
        
        // effective date
        intRefset.append(effectiveDate);
        intRefset.append("\t");
        
        // path UUID
        intRefset.append(pathUUID.toString());
        intRefset.append("\t");
        
        // Concept extension
        intRefset.append(conceptExtension.getUids().iterator().next().toString());
        intRefset.append("\n");
    }

    private void writeIntRefsetEntryAndGenerateId(Writer intRefset, UUID pathUUID, int objectReadCodeIndex,
        int attributeReadCodeIndex, int valueReadCodeIndex, String[] templatePart, int index, UUID type5prefix,
        Concept refsetIdentity) throws NoSuchAlgorithmException, UnsupportedEncodingException, IOException {
        UUID memberId = Type5UuidFactory.get(type5prefix,
                                             templatePart[objectReadCodeIndex]+
                                             templatePart[attributeReadCodeIndex]+
                                             templatePart[valueReadCodeIndex]);
        writeIntRefsetEntry(intRefset, pathUUID, objectReadCodeIndex, attributeReadCodeIndex, valueReadCodeIndex, templatePart,
                            refsetIdentity, 
                            memberId, 
                            templatePart[index]);
    }

    private void writeIntRefsetEntry(Writer intRefset, UUID pathUUID, int objectReadCodeIndex, int attributeReadCodeIndex,
        int valueReadCodeIndex,
        String[] relationshipParts, Concept refset, UUID memberId, String extIntValue) throws IOException,
            NoSuchAlgorithmException, UnsupportedEncodingException {
        // refset UUID
        intRefset.append(refset.getUids().iterator().next().toString());
        intRefset.append("\t");
        // member UUID
        intRefset.append(memberId.toString());
        intRefset.append("\t");
        // status UUID
        intRefset.append(ArchitectonicAuxiliary.Concept.CURRENT.getUids().iterator().next().toString());
        intRefset.append("\t");
        // component UUID
        intRefset.append(Type5UuidFactory.get(Type5UuidFactory.READV3_REL_ID,
                                                  relationshipParts[objectReadCodeIndex]+
                                                  relationshipParts[attributeReadCodeIndex]+
                                                  relationshipParts[valueReadCodeIndex]).toString());
        intRefset.append("\t");
        
        // effective date
        intRefset.append(effectiveDate);
        intRefset.append("\t");
        
        // path UUID
        intRefset.append(pathUUID.toString());
        intRefset.append("\t");
        
        // integer extension
        intRefset.append(extIntValue);
        intRefset.append("\n");
    }


    private void writeRelationshipData(BufferedReader relationshipFileReader, Writer relationships, Writer intRefset,
        UUID pathUUID) throws IOException, NoSuchAlgorithmException, UnsupportedEncodingException {
        int childReadcodeIndex = 0;
        int parentReadcodeIndex = 1;
        int childReadcodeOrderIndex = 2;

        String line3 = relationshipFileReader.readLine();
        if (line3 != null && line3.length() > 5) {

        	String[] relationshipParts = line3.split("\\|");

        	relationships.append(Type5UuidFactory.get(Type5UuidFactory.READV3_REL_ID,
        			relationshipParts[childReadcodeIndex]+relationshipParts[parentReadcodeIndex]).toString());
        	relationships.append("\t");

        	relationships.append(ArchitectonicAuxiliary.Concept.CURRENT.getUids().iterator().next().toString());
        	relationships.append("\t");

        	relationships.append(Type5UuidFactory.get(Type5UuidFactory.READV3_CONCEPT_ID,
        			relationshipParts[childReadcodeIndex]).toString());
        	relationships.append("\t");

        	relationships
        			.append(ArchitectonicAuxiliary.Concept.IS_A_REL
        					.getUids().iterator().next().toString());
        	relationships.append("\t");

        	relationships
        			.append(Type5UuidFactory.get(Type5UuidFactory.READV3_CONCEPT_ID,
        					relationshipParts[parentReadcodeIndex]).toString());
        	relationships.append("\t");

        	relationships
        			.append(ArchitectonicAuxiliary.Concept.STATED_RELATIONSHIP
        					.getUids().iterator().next().toString());
        	relationships.append("\t");
        	// refine
        	relationships
        			.append(ArchitectonicAuxiliary.Concept.NOT_REFINABLE
        					.getUids().iterator().next().toString());
        	relationships.append("\t");
        	// group
        	relationships.append("0");
        	relationships.append("\t");

        	relationships.append(effectiveDate);
        	relationships.append("\t");

        	relationships.append(pathUUID.toString());
        	relationships.append("\n");
        	
        	// integer refset for order...
        	// refset UUID
            intRefset.append(RefsetAuxiliary.Concept.CTV3_REL_ORDER.getUids().iterator().next().toString());
            intRefset.append("\t");
            // member UUID
            intRefset.append(Type5UuidFactory.get(Type5UuidFactory.READV3_REL_ORDER_REFSET_MEMBER_ID,
                                                  relationshipParts[childReadcodeIndex]+relationshipParts[parentReadcodeIndex]).toString());
            intRefset.append("\t");
            // status UUID
            intRefset.append(ArchitectonicAuxiliary.Concept.CURRENT.getUids().iterator().next().toString());
            intRefset.append("\t");
            // component UUID
            intRefset.append(Type5UuidFactory.get(Type5UuidFactory.READV3_REL_ID,
                                                      relationshipParts[childReadcodeIndex]+relationshipParts[parentReadcodeIndex]).toString());
            intRefset.append("\t");
            
            // effective date
            intRefset.append(effectiveDate);
            intRefset.append("\t");
            
            // path UUID
            intRefset.append(pathUUID.toString());
            intRefset.append("\t");
            
            // integer extension
            intRefset.append(relationshipParts[childReadcodeOrderIndex]);
            intRefset.append("\n");

            

        }
    }

	private UUID writeDescription(Writer descriptions, UUID pathUUID,
			Map<String, String> termidReadCodeMap, int termidIndex,
			int termStatusIndex, String term, String[] termparts,
			UUID genPrefix,
			ArchitectonicAuxiliary.Concept descriptionTypeConcept)
			throws Exception {

		if (term == null || term.length() < 1) {

			return null;
		}
		UUID descUuid = Type5UuidFactory.get(genPrefix,
		                                     termparts[termidIndex] + term);
		descriptions.append(descUuid.toString());
		descriptions.append("\t");

		if (termparts[termStatusIndex].equals("O")) {

			descriptions.append(ArchitectonicAuxiliary.Concept.RETIRED
					.getUids().iterator().next().toString());

		} else if (termparts[termStatusIndex].equals("C")) {

			descriptions.append(ArchitectonicAuxiliary.Concept.CURRENT
					.getUids().iterator().next().toString());

		} else {

			throw new Exception("Can't handle :" + termparts[termStatusIndex]);

		}

		descriptions.append("\t");

		descriptions.append(Type5UuidFactory.get(
				Type5UuidFactory.READV3_CONCEPT_ID,
				termidReadCodeMap.get(termparts[termidIndex])).toString());
		descriptions.append("\t");

		descriptions.append(term);
		descriptions.append("\t");

		descriptions.append("1");
		descriptions.append("\t");

		descriptions.append(descriptionTypeConcept.getUids().iterator().next()
				.toString());

		descriptions.append("\t");

		descriptions.append("en-gb");
		descriptions.append("\t");

		descriptions.append(effectiveDate);
		descriptions.append("\t");

		descriptions.append(pathUUID.toString());
		descriptions.append("\n");

		return descUuid;
	}

	private void setupDescriptionIndex(BufferedReader descriptionFileReader,
			Map<String, String> termidReadCodeMap,
			Map<String, String> termidDescTypeMap) throws IOException {

		while (descriptionFileReader.ready()) {
			int readcodeIndex = 0;
			int termidIndex = 1;
			int descTypeIndex = 2;

			String line = descriptionFileReader.readLine();
			if (line != null && line.length() > 5) {
				String[] descparts = line.split("\\|");

				termidReadCodeMap.put(descparts[termidIndex],
						descparts[readcodeIndex]);
				termidDescTypeMap.put(descparts[termidIndex],
						descparts[descTypeIndex]);

			}

		}
	}

	private void processRow(Writer concepts, Writer descriptions,
			Writer relationships, Writer ids, UUID pathUUID, String line)
			throws IOException, NoSuchAlgorithmException,
			UnsupportedEncodingException {

		String id = line.substring(0, 8).trim();
		String desc = line.substring(8).trim();

		ids.append(Type5UuidFactory.get(Type5UuidFactory.OPCS_CONCEPT_ID, id)
				.toString()); // concept id
		ids.append("\t");
		ids.append(ArchitectonicAuxiliary.Concept.UNSPECIFIED_STRING.getUids()
				.iterator().next().toString()); // source
		ids.append("\t");
		ids.append(id); // source id
		ids.append("\t");
		ids.append(ArchitectonicAuxiliary.Concept.CURRENT.getUids().iterator()
				.next().toString()); // status
		ids.append("\t");
		ids.append(effectiveDate);
		ids.append("\t");
		ids.append(pathUUID.toString()); // path id
		ids.append("\n");

		concepts.append(Type5UuidFactory.get(Type5UuidFactory.OPCS_CONCEPT_ID,
				id).toString()); // concept id
		concepts.append("\t");
		concepts.append(ArchitectonicAuxiliary.Concept.CURRENT.getUids()
				.iterator().next().toString()); // status
		concepts.append("\t");
		concepts.append("1"); // primitive
		concepts.append("\t");
		concepts.append(effectiveDate);
		concepts.append("\t");
		concepts.append(pathUUID.toString()); // path id
		concepts.append("\n");

		descriptions.append(Type5UuidFactory.get(Type5UuidFactory.OPCS_DESC_ID,
				id).toString());
		descriptions.append("\t");
		descriptions.append(ArchitectonicAuxiliary.Concept.CURRENT.getUids()
				.iterator().next().toString());
		descriptions.append("\t");
		descriptions.append(Type5UuidFactory.get(
				Type5UuidFactory.OPCS_CONCEPT_ID, id).toString());
		descriptions.append("\t");
		descriptions.append(desc);
		descriptions.append("\t");
		descriptions.append("1");
		descriptions.append("\t");
		descriptions
				.append(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE
						.getUids().iterator().next().toString()); // status
		descriptions.append("\t");
		descriptions.append("en"); // primitive
		descriptions.append("\t");
		descriptions.append(effectiveDate);
		descriptions.append("\t");
		descriptions.append(pathUUID.toString()); // path id
		descriptions.append("\n");

		String parentId = "opcs";
		if (id.contains(".")) {
			parentId = id.substring(0, 3);
		}

		relationships.append(Type5UuidFactory.get(Type5UuidFactory.OPCS_REL_ID,
				id + parentId).toString());
		relationships.append("\t");
		relationships.append(ArchitectonicAuxiliary.Concept.CURRENT.getUids()
				.iterator().next().toString());
		relationships.append("\t");
		relationships.append(Type5UuidFactory.get(
				Type5UuidFactory.OPCS_CONCEPT_ID, id).toString());
		relationships.append("\t");
		relationships.append(ArchitectonicAuxiliary.Concept.IS_A_REL.getUids()
				.iterator().next().toString());
		relationships.append("\t");
		relationships.append(Type5UuidFactory.get(
				Type5UuidFactory.OPCS_CONCEPT_ID, parentId).toString());
		relationships.append("\t");
		relationships
				.append(ArchitectonicAuxiliary.Concept.STATED_RELATIONSHIP
						.getUids().iterator().next().toString());
		relationships.append("\t");
		relationships.append(ArchitectonicAuxiliary.Concept.NOT_REFINABLE
				.getUids().iterator().next().toString());
		relationships.append("\t");
		relationships.append("0");
		relationships.append("\t");
		relationships.append(effectiveDate);
		relationships.append("\t");
		relationships.append(pathUUID.toString()); // path id
		relationships.append("\n");
	}

	private void writeRoot(Writer concepts, Writer descriptions,
			Writer relationships, Writer ids, UUID pathUUID)
			throws IOException, NoSuchAlgorithmException,
			UnsupportedEncodingException {
		String id = "opcs";
		String desc = "OPCS Concept";

		ids.append(Type5UuidFactory.get(Type5UuidFactory.OPCS_CONCEPT_ID, id)
				.toString()); // concept id
		ids.append("\t");
		ids.append(ArchitectonicAuxiliary.Concept.UNSPECIFIED_STRING.getUids()
				.iterator().next().toString()); // source
		ids.append("\t");
		ids.append(id); // source id
		ids.append("\t");
		ids.append(ArchitectonicAuxiliary.Concept.CURRENT.getUids().iterator()
				.next().toString()); // status
		ids.append("\t");
		ids.append(effectiveDate);
		ids.append("\t");
		ids.append(pathUUID.toString()); // path id
		ids.append("\n");

		concepts.append(Type5UuidFactory.get(Type5UuidFactory.OPCS_CONCEPT_ID,
				id).toString()); // concept id
		concepts.append("\t");
		concepts.append(ArchitectonicAuxiliary.Concept.CURRENT.getUids()
				.iterator().next().toString()); // status
		concepts.append("\t");
		concepts.append("1"); // primitive
		concepts.append("\t");
		concepts.append(effectiveDate);
		concepts.append("\t");
		concepts.append(pathUUID.toString()); // path id
		concepts.append("\n");

		descriptions.append(Type5UuidFactory.get(Type5UuidFactory.OPCS_DESC_ID,
				id).toString());
		descriptions.append("\t");
		descriptions.append(ArchitectonicAuxiliary.Concept.CURRENT.getUids()
				.iterator().next().toString());
		descriptions.append("\t");
		descriptions.append(Type5UuidFactory.get(
				Type5UuidFactory.OPCS_CONCEPT_ID, id).toString());
		descriptions.append("\t");
		descriptions.append(desc);
		descriptions.append("\t");
		descriptions.append("1");
		descriptions.append("\t");
		descriptions
				.append(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE
						.getUids().iterator().next().toString()); // status
		descriptions.append("\t");
		descriptions.append("en"); // primitive
		descriptions.append("\t");
		descriptions.append(effectiveDate);
		descriptions.append("\t");
		descriptions.append(pathUUID.toString()); // path id
		descriptions.append("\n");

	}
}
