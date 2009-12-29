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
package org.dwfa.mojo.epicexport;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IdPart;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.mojo.ConceptDescriptor;
import org.dwfa.mojo.PositionDescriptor;
import org.dwfa.mojo.epicexport.kp.RegionalHibernationBuilder;
import org.dwfa.tapi.TerminologyException;

/**
 * * ImportEpicSynchFile <br/>
 * <p>
 * The <code>ImportEpicSynchFileMojo</code> imports Epic synch files used to
 * apply Epic generated id values (dot1 and CID) to concepts previously exported
 * and loaded into Epic.
 * </p>
 * <p>
 * </p>
 * 
 * 
 * 
 * @see <code>org.apache.maven.plugin.AbstractMojo</code>
 * @author Frank Novak
 * @goal import-epic-synch-file
 */
public class ImportEpicSynchFileMojo extends AbstractMojo {
    /**
     * Location of the KP data directory to read from.
     * 
     * @parameter expression="src/main/synch"
     * @required
     */
    private File kpDir;
	/**
	 * Positions to export data.
	 *
	 * @parameter
	 * @required
	 */
	private PositionDescriptor[] positionsForExport;

	/**
	 * Status values to include in export
	 *
	 * @parameter
	 * @required
	 */
	private ConceptDescriptor[] statusValuesForExport;
	/**
	 * Location of the directory to output regional hibernation files to.
	 *
	 * @parameter expression="${project.build.directory}"
	 * @required
	 */
	private String outputDirectory;
	
    
    private int synchRecordsProcessed = 0;
    private int conceptsMatched = 0;
    private int cidValuesMatched = 0;
    private int dot1ValuesMatched = 0;
    private int cidValuesMisMatched = 0;
    private int dot1ValuesMisMatched = 0;
    private int cidValuesSynched = 0;
    private int dot1ValuesSynched = 0;
	private HashSet<I_Position> positions;

	private I_IntSet statusValues;
	private I_TermFactory termFactory;
	private RegionalHibernationBuilder hibernationBuilder;

    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
        	termFactory = LocalVersionedTerminology.get();
        	/**
        	 * Set the status values and positions for export.  This is used for building the regional
        	 * hibernation.
        	 */
			positions = new HashSet<I_Position>(
					positionsForExport.length);
			for (PositionDescriptor pd : positionsForExport) {
				positions.add(pd.getPosition());
			}
			
			statusValues = termFactory.newIntSet();
			List<I_GetConceptData> statusValueList = new ArrayList<I_GetConceptData>();
			for (ConceptDescriptor status : statusValuesForExport) {
				I_GetConceptData statusConcept = status.getVerifiedConcept();
				statusValues.add(statusConcept.getConceptId());
				statusValueList.add(statusConcept);
			}
			hibernationBuilder = 
				new RegionalHibernationBuilder(outputDirectory, positions, statusValues);

            getLog().info("KP dir: " + kpDir);

            BufferedReader r = new BufferedReader(new FileReader(new File(kpDir, "epic_synch.txt")));

            I_TermFactory termFactory = LocalVersionedTerminology.get();

            while (r.ready()) {
                String line = r.readLine();
                if (line.length() > 0) {
                    processRowConceptUUID(termFactory, line);
                    synchRecordsProcessed++;
                }
            }

            r.close();
            hibernationBuilder.close();

            getLog().info("synchRecordsProcessed: " + synchRecordsProcessed);
            getLog().info("conceptsMatched:       " + conceptsMatched);
            getLog().info("cidValuesMatched:      " + cidValuesMatched);
            getLog().info("dot1ValuesMatched:     " + dot1ValuesMatched);
            getLog().info("cidValuesMisMatched:   " + cidValuesMisMatched);
            getLog().info("dot1ValuesMisMatched:  " + dot1ValuesMisMatched);
            getLog().info("cidValuesSynched:      " + cidValuesSynched);
            getLog().info("dot1ValuesSynched:     " + dot1ValuesSynched);

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

    private void processRowConceptUUID(I_TermFactory tf, String line) throws IOException, NoSuchAlgorithmException,
            UnsupportedEncodingException {

        String[] parts = line.split("\t"); // split the tab delimited line

        // concept uuid
        String concept_id = parts[0].trim();

        // dot1 value
        String dot1 = parts[1].trim();

        // cid value
        String cid = parts[2].trim();

        try {
            I_GetConceptData concept = tf.getConcept(new UUID[] { UUID.fromString(concept_id) });
            conceptsMatched++;
            hibernationBuilder.buildAnyHibernationForConcept(concept, cid);

            /*
             * I_GetConceptData cidSourceConcept = tf.getConcept(new UUID[] {
             * UUID .fromString("bf3e7556-38cb-5395-970d-f11851c9f41e") }); //
             * EDG // Billing // Item // 11 I_GetConceptData dot1SourceConcept =
             * tf .getConcept(new UUID[] { UUID
             * .fromString("af8be384-dc60-5b56-9ad8-bc1e4b5dfbae") }); // EDG //
             * Billing // Dot1 I_GetConceptData kpPath = tf.getConcept(new
             * UUID[] { UUID .fromString("2bfc4102-f630-5fbe-96b8-625f2a6b3d5a")
             * }); // KP // Extension // Path
             */

            I_GetConceptData cidSourceConcept = tf.getConcept(new UUID[] { UUID.fromString("e3dadc2a-196d-5525-879a-3037af99607d") }); // EDG
            // Clinical
            // Item
            // 11
            // -
            // e3dadc2a-196d-5525-879a-3037af99607d

            I_GetConceptData dot1SourceConcept = tf.getConcept(new UUID[] { UUID.fromString("e49a55a7-319d-5744-b8a9-9b7cc86fd1c6") }); // EDG
            // Clinical
            // Dot1
            // -
            // e49a55a7-319d-5744-b8a9-9b7cc86fd1c6

            I_GetConceptData kpPath = tf.getConcept(new UUID[] { UUID.fromString("2bfc4102-f630-5fbe-96b8-625f2a6b3d5a") }); // KP
            // Extension
            // Path

            int cidSourceNid = cidSourceConcept.getConceptId();
            int dot1SourceNid = dot1SourceConcept.getConceptId();
            boolean foundCidSource = false;
            boolean foundDot1Source = false;

            for (I_IdPart part : concept.getIdentifier().getMutableParts()) {
                if (part.getAuthorityNid() == cidSourceNid) {
                    foundCidSource = true;
                    if (part.getDenotation().equals(cid)) {
                        cidValuesMatched++;
                    } else {
                        getLog().warn(
                            "CID value in synch file [" + cid + "] does not match database record [" + concept_id + "]");
                        cidValuesMisMatched++;
                    }
                    if (foundCidSource && foundDot1Source) {
                        break;
                    }
                }

                if (part.getAuthorityNid() == dot1SourceNid) {
                    foundDot1Source = true;
                    if (part.getDenotation().equals(dot1)) {
                        dot1ValuesMatched++;
                    } else {
                        getLog().warn(
                            "Dot1 value in synch file [" + dot1 + "] does not match database record [" + concept_id
                                + "]");
                        dot1ValuesMisMatched++;
                    }
                    if (foundCidSource && foundDot1Source) {
                        break;
                    }
                }
            }

            if (foundDot1Source == false) {
                I_IdPart aPart = concept.getIdentifier().getMutableParts().get(0).duplicateIdPart();
                aPart.setAuthorityNid(dot1SourceNid);
                aPart.setDenotation(dot1);
                aPart.setPathId(kpPath.getNid());
                aPart.setVersion(Integer.MAX_VALUE);
                concept.getIdentifier().addMutableIdPart(aPart);
                concept.getUncommittedIdVersioned().add(concept.getIdentifier());
                tf.addUncommitted(concept);
                dot1ValuesSynched++;
            }

            if (foundCidSource == false) {
                I_IdPart aPart = concept.getIdentifier().getMutableParts().get(0).duplicateIdPart();
                aPart.setAuthorityNid(cidSourceNid);
                aPart.setDenotation(cid);
                aPart.setPathId(kpPath.getNid());
                aPart.setVersion(Integer.MAX_VALUE);
                concept.getIdentifier().addMutableIdPart(aPart);
                concept.getUncommittedIdVersioned().add(concept.getIdentifier());
                tf.addUncommitted(concept);
                cidValuesSynched++;
            }
            
            

        } catch (TerminologyException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
