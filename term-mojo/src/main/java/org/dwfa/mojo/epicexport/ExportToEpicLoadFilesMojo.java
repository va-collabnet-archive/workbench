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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IdPart;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_ProcessConcepts;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefTuple;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.maven.MojoUtil;
import org.dwfa.mojo.ConceptDescriptor;
import org.dwfa.mojo.PositionDescriptor;
import org.dwfa.mojo.epicexport.kp.EpicLoadFileFactory;
import org.dwfa.mojo.epicexport.kp.EpicTermWarehouseFactory;
import org.dwfa.tapi.TerminologyException;


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
      * Location of the build directory.
      *
      * @parameter expression="${project.build.directory}"
      * @required
      */
     
 	/**
 	 * The name of the writer, used to determine the output
 	 *
 	 * @parameter
 	 * @required
 	 */
 	private String writerName;
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
     
     
     /**
      * The URL of the database
      * 
      * @parameter
      * @optional
      */
     private String database;
     /**
      * The username of the database
      * 
      * @parameter
      * @optional
      */
     private String username;
     /**
      * The password of the database
      * 
      * @parameter
      * @optional
      */
     private String password;
     
     private File targetDirectory;

	private HashSet<I_Position> positions;

	private I_IntSet statusValues;
	
	private I_TermFactory termFactory;
	private I_ExportFactory exportFactory;
	private EpicExportManager exportManager;

     public void execute() throws MojoExecutionException, MojoFailureException {
		try {

			if (MojoUtil.alreadyRun(getLog(), outputDirectory,
					this.getClass(), targetDirectory)) {
				return;
			}

			termFactory = LocalVersionedTerminology.get();

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
			
			this.setFactories();
			ExternalTermPublisher mapper = new ExternalTermPublisher(this.exportFactory);
			mapper.setStartingDate(deltaStartDate);
			mapper.setPositions(positions);
			mapper.setStatusValues(statusValues);
			/* //TEST CODE:
			I_GetConceptData concept = termFactory.getConcept(UUID.fromString("1ca9b835-cbf6-40e8-82b3-acbf0eb30293"));

			List<ExternalTermRecord> er = mapper.getExternalTermRecordsForConcept(concept);
			for (ExternalTermRecord record: er)
				System.out.println(record.toString());
			// END TEST CODE */
			
			getLog().info(
					" processing concepts for positions: " + positions
							+ " with status: " + statusValueList);

			if (outputDirectory.endsWith("/") == false) {
				outputDirectory = outputDirectory + "/";
			}
			
			ExportIterator expItr = new ExportIterator(mapper, this.exportManager, this.dropName);
			
			
			/*
			 * Single process debugging sample EDG Clinical = 3073adf3-0c10-3cbb-975f-7bfc0c9cbd17 
			 * EDG Clinical soft delete = 6b9d08a1-d645-3d85-84aa-85c90e48c53d
			 * EDG Clinical description concept 528a6294-a8be-5443-ac3d-e87195f88191
			 * EDG Billing = a7130b8c-e6c1-57d8-986a-0d88552c12e4
			 * 
			*/
			// Iterate through all concepts
			LocalVersionedTerminology.get().iterateConcepts(expItr);
			expItr.close();

		} catch (Exception e) {
			getLog().error(e);
			throw new MojoExecutionException("Unable to export database due to exception", e);
		}

	}// End method execute

     private void setFactories() throws Exception {
		if (this.writerName.equals("kp loadfile export")) {
			exportFactory = new EpicLoadFileFactory();
			exportManager = exportFactory.getExportManager(this.outputDirectory);
		}
		else if (this.writerName.equals("kp term warehouse build")){
			exportFactory = new EpicTermWarehouseFactory();
			exportManager = exportFactory.getExportManager(this.database, this.username, this.password);
		}
		else
			throw new Exception("Unknown writer name: " + this.writerName);
     }
     
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