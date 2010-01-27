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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_ProcessConcepts;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.maven.MojoUtil;
import org.dwfa.mojo.ConceptDescriptor;
import org.dwfa.mojo.PositionDescriptor;
import org.dwfa.mojo.epicexport.kp.EpicLoadFileFactory;
import org.dwfa.mojo.epicexport.kp.EpicTermWarehouseFactory;


/**
 * The <code>ExportToEpicLoadFilesMojo</code> class generates Epic load files
 * used to populate and update the Epic terminology master files.
 * </p>
 * <p>
 * </p>
 *
 *
 *
 * @see <code>org.apache.maven.plugin.AbstractMojo</code>
 * @author Steven Neiner
 * @goal generate-epic-loadfiles
 */
public class ExportToEpicLoadFilesMojo extends AbstractMojo {
	public final static String DESCRIPTION_PREFERED_TERM = "prefered term";
	public final static String DESCRIPTION_SYNONYM = "synonym";
	public static final String EPIC_MASTERFILE_NAME_WILDCARD = "*";
	/**
	 * Location of the directory to output data files to.
	 *
	 * @parameter expression="${project.build.directory}"
	 * @required
	 */
	private String outputDirectory;
	
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
     * @parameter default-value="${project.build.directory}"
     * @required
     * @readonly
     */
     @SuppressWarnings("unused")
	private File buildDirectory;

     /**
      * The date to start looking for changes
      * 
      * @parameter
      * @required
      */
     private String deltaStartDate;
     
     /**
      * The name of the drop
      * 
      * @parameter
      * @required
      */
     private String dropName;
     
     private File targetDirectory;

	private HashSet<I_Position> positions;

	private I_IntSet statusValues;
	
	private I_TermFactory termFactory;
	
 	/**
 	 * The factory used to get objects for the export
 	 *
 	 * @parameter
 	 * @required
 	 */
	private I_ExportFactory exportFactory;
	
	private EpicExportManager exportManager;

     public void execute() throws MojoExecutionException, MojoFailureException {
		try {

			if (MojoUtil.alreadyRun(getLog(), outputDirectory,
					this.getClass(), targetDirectory)) {
				return;
			}

			termFactory = Terms.get();

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
			
			this.exportManager = exportFactory.getExportManager();
			ExternalTermPublisher publisher = new ExternalTermPublisher(this.exportFactory);
			publisher.setStartingDate(deltaStartDate);
			publisher.setPositions(positions);
			publisher.setStatusValues(statusValues);
			/* //TEST CODE: dbdd4eb3-1457-34d5-a248-bdeb1b86bd3f
			I_GetConceptData concept = termFactory.getConcept(UUID.fromString("beb91a4a-1aba-38c0-8513-43fe1d0b8eec"));

			List<ExternalTermRecord> er = publisher.getExternalTermRecordsForConcept(concept);
			for (ExternalTermRecord record: er) {
				System.out.println(record.toString());
				 // getLog().info("Adding item 100 ");
				 // record.addMember("100", "Swine Flu");
				 ExternalTermRecord.Item item = record.getFirstItem("7010");
				 if (item != null) {
					 getLog().info("Setting item 7010");
					 item.memberUpdate("Hx of traumatic vertebral FX TEST");
				 }

			}
			// END TEST CODE */
			
			getLog().info(
					" processing concepts for positions: " + positions
							+ " with status: " + statusValueList);

			if (outputDirectory.endsWith("/") == false) {
				outputDirectory = outputDirectory + "/";
			}
			
			ExportIterator expItr = new ExportIterator(publisher, this.exportManager, this.dropName);
			
			
			/*
			 * Single process debugging sample EDG Clinical = 3073adf3-0c10-3cbb-975f-7bfc0c9cbd17 
			 * EDG Clinical soft delete = 6b9d08a1-d645-3d85-84aa-85c90e48c53d
			 * EDG Clinical description concept 528a6294-a8be-5443-ac3d-e87195f88191
			 * EDG Billing = a7130b8c-e6c1-57d8-986a-0d88552c12e4
			 * 
			*/
			// Iterate through all concepts
			Terms.get().iterateConcepts(expItr);
			expItr.close();

		} catch (Exception e) {
			getLog().error(e);
			throw new MojoExecutionException("Unable to export database due to exception", e);
		}

	}// End method execute

     
     private class ExportIterator implements I_ProcessConcepts {
    	 private ExternalTermPublisher publisher;
    	 private EpicExportManager exportManager;
    	 private String version;
    	 private int counter = 0;
    	 
    	 public ExportIterator(ExternalTermPublisher publisher, EpicExportManager exportManager, String version) {
    		 this.publisher = publisher;
    		 this.exportManager = exportManager;
    		 this.version = version;
    	 }
    	 
    	 public void processConcept(I_GetConceptData concept) throws Exception {
 			if (++counter % 10000 == 0)
				getLog().info("Iterated " + counter + " concepts");

    		 List<ExternalTermRecord> terms = publisher.getExternalTermRecordsForConcept(concept);
    		 for (ExternalTermRecord term: terms) {
    			 term.setVersion(version);
    		     exportManager.exportExternalTermRecord(term);
    		 }
    	 }
    	 
 		public void close() throws Exception {
 			File f = new File(this.exportManager.getBaseDir() + "_export-results.txt");
 			BufferedWriter bw = new BufferedWriter(new FileWriter(f));
			this.exportManager.close(bw);
			bw.close();
		}
    	 
     }
}