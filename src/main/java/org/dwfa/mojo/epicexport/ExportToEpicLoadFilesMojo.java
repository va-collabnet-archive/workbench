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

import java.io.File;
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
	
	//private HashMap<Integer, RefsetType> refsetTypeMap = new HashMap<Integer, RefsetType>();

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
			
			// BEGIN TEST CODE
			ExternalRecordMapper mapper = new ExternalRecordMapper(new EpicLoadFileFactory());
			mapper.setStartingDate(deltaStartDate);
			mapper.setPositions(positions);
			mapper.setStatusValues(statusValues);
			I_GetConceptData concept = termFactory.getConcept(UUID.fromString("3073adf3-0c10-3cbb-975f-7bfc0c9cbd17"));

			List<ExternalRecord> er = mapper.getExternalRecordsForConcept(concept);
			for (ExternalRecord record: er)
				System.out.println(record.toString());
			// END TEST CODE
			
			getLog().info(
					" processing concepts for positions: " + positions
							+ " with status: " + statusValueList);

			if (outputDirectory.endsWith("/") == false) {
				outputDirectory = outputDirectory + "/";
			}
			
			ExportIterator expItr = new ExportIterator(this);
			
			
			/*
			 * Single process debugging sample EDG Clinical = 3073adf3-0c10-3cbb-975f-7bfc0c9cbd17 
			 * EDG Clinical soft delete = 6b9d08a1-d645-3d85-84aa-85c90e48c53d
			 * EDG Clinical description concept 528a6294-a8be-5443-ac3d-e87195f88191
			 * EDG Billing = a7130b8c-e6c1-57d8-986a-0d88552c12e4
			 * 
			*/
			
			//I_GetConceptData concept = termFactory.getConcept(UUID.fromString("3073adf3-0c10-3cbb-975f-7bfc0c9cbd17"));
			// expItr.processConcept(concept);
			
			
			// Iterate through all concepts
			LocalVersionedTerminology.get().iterateConcepts(expItr);
			expItr.close();

		} catch (Exception e) {
			getLog().error(e);
			throw new MojoExecutionException("Unable to export database due to exception", e);
		}

	}// End method execute
     
    @SuppressWarnings("unused")
	private class ExportIterator implements I_ProcessConcepts {

		private String currentItem;
		
		private String currentMasterFile;
		private I_ExportFactory exportFactory;
		private EpicExportManager exportManager;
		private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd'T'hhmmss'Z'");
		private int startingVersion;
		private String dropName;
		private int counter = 0;
		private Date startingDate;
		private List<String> masterFilesImpacted;
		private I_ThinExtByRefTuple idTuple;
		private List<DisplayName> displayNames;
		private I_RefsetUsageInterpreter interpreter;
		private List<ValuePair> wildcardItems;
		
		public ExportIterator(ExportToEpicLoadFilesMojo parent) throws Exception
		{
			Date parsedDate = new Date();
			
			if (parent.writerName.equals("kp loadfile export")) {
				exportFactory = new EpicLoadFileFactory();
				exportManager = exportFactory.getExportManager(parent.outputDirectory);
			}
			else if (parent.writerName.equals("kp term warehouse build")){
				exportFactory = new EpicTermWarehouseFactory();
				exportManager = exportFactory.getExportManager(parent.database, parent.username, parent.password);
			}
			else
				throw new Exception("Unknown writer name: " + parent.writerName);
			this.dropName = parent.dropName;
			try {
				parsedDate = dateFormat.parse(parent.deltaStartDate);
			} catch (java.text.ParseException e) {
				getLog().error("Invalid date: " + parent.deltaStartDate + " - use format yyyymmssThhmmssZ+8");
				e.printStackTrace();
			}
	    	
	    	this.startingVersion = termFactory.convertToThinVersion(parsedDate.getTime());
	    	this.startingDate = new Date(parsedDate.getTime());
	    	this.interpreter = exportFactory.getInterpreter();
		}

		public void close() throws Exception {
			this.exportManager.close();
		}
		
		public void processConcept(I_GetConceptData concept) throws Exception {
			masterFilesImpacted = new ArrayList<String>();
			displayNames = new ArrayList<DisplayName>();
			wildcardItems = new ArrayList<ValuePair>();
			
			this.idTuple = null;
			List<? extends I_DescriptionVersioned> descs = concept.getDescriptions();

			if (++counter % 10000 == 0)
				getLog().info("Iterated " + counter + " concepts");
			this.setCurrentItem(null, null);
			int extensionsProcessed = 0;
			for (I_DescriptionVersioned desc : descs) {
				// String name = desc.getLastTuple().getPart().getText();
				I_GetConceptData descriptionConcept = termFactory.getConcept(desc.getConceptId());
				extensionsProcessed += processDescriptionConcept(descriptionConcept, desc);
			}
			// Finally, process this concept directly if it is a description concept, or process the root concept
			// for extensions attached to the root concept
			processDescriptionConcept(concept, null);
			if (this.idTuple != null)
				this.exportFactory.getValueConverter(startingVersion).writeRecordIds(this.idTuple,
						masterFilesImpacted, exportManager);
			// Write all of the records
	        for (String masterfile: masterFilesImpacted) {
		        I_EpicLoadFileBuilder exportBuilder = exportManager.getLoadFileBuilder(masterfile);
		        if (exportBuilder != null) { 
		        	writeWildcardValues(exportBuilder);
			        exportBuilder.writeRecord(this.dropName, this.getRegions(masterfile));
			        exportBuilder.clearRecordContents();
		        }
		        this.currentMasterFile = null;
	        }
	        
	        // Need to clear any "orphaned" items, so they do not get put into the next concept.
	        // This may happen if a refset for a description is not associated with the master file
	        // of the other refsets.
	        exportManager.clearAllContents();
		}
		

	    private int processDescriptionConcept(I_GetConceptData concept, I_DescriptionVersioned description) throws TerminologyException, Exception {
	    	List<I_ThinExtByRefVersioned> extensions;
	    	if (description != null)
	    		extensions = termFactory.getAllExtensionsForComponent(description.getDescId());
	    	else
	    		extensions = termFactory.getAllExtensionsForComponent(concept.getConceptId());
	    	// getLog().info("Found " + extensions.size() + " extensions");
	    	// getLog().info("--------------------------------- Processing extensions: " );
	    	
	    	I_ThinExtByRefTuple lastTuple = null;
	    	for (I_ThinExtByRefVersioned thinExtByRefVersioned : extensions) {
	    		// getLog().info("Processing extension: " + thinExtByRefVersioned );
	        	if (termFactory.hasConcept(thinExtByRefVersioned.getRefsetId())) {
	                for (I_ThinExtByRefTuple thinExtByRefTuple : thinExtByRefVersioned.getTuples(statusValues,
	                    positions, false, false)) {
	                	lastTuple = thinExtByRefTuple;
	                	export(thinExtByRefTuple, concept, description);
	                }
	        	}else {
	        		throw new Exception("No concept for ID " + thinExtByRefVersioned.getRefsetId());
	        	}
	        }
        	// if (lastTuple != null)
        		
	        
	        return extensions.size();
	    }

	    void export(I_ThinExtByRefTuple extensionTuple, I_GetConceptData extendedConcept, 
	    		I_DescriptionVersioned description) throws Exception {
	    	
	    	int refsetId = extensionTuple.getRefsetId();
	    	I_GetConceptData refsetConcept = termFactory.getConcept(refsetId);
	    	exportRefset(refsetConcept, extensionTuple, extendedConcept, description, 
	    			getPreviousVersion(extensionTuple));
	    }
	    
	    private I_ThinExtByRefPart getPreviousVersion(I_ThinExtByRefTuple thinExtByRefTuple) throws Exception {
	    	List<? extends I_ThinExtByRefPart> versions = thinExtByRefTuple.getVersions();
	    	
	    	I_ThinExtByRefPart newestOldVersion  = null;
	    	for (Iterator<? extends I_ThinExtByRefPart> i = versions.iterator(); i.hasNext(); ) {
	    		I_ThinExtByRefPart v = i.next();
	    		if (v.getVersion() <= this.startingVersion) {
		    		if (newestOldVersion == null) {
		    			newestOldVersion = v;
		    		}
		    		else if (v.getVersion() > newestOldVersion.getVersion()){
		    			newestOldVersion = v;
		    		}
	    		}
	    	}
	    	return newestOldVersion;
	    }

	    
	    public void exportRefset(I_GetConceptData refsetConcept, 
	    		I_ThinExtByRefTuple extensionTuple, I_GetConceptData conceptForDescription,
	    		I_DescriptionVersioned description, I_ThinExtByRefPart previousPart) throws Exception {
	    	this.currentItem = null;
	    	String refsetName = refsetConcept.getInitialText();
	    	
	    	I_ThinExtByRefPart extensionTuplePart = extensionTuple.getMutablePart();
	    	// getLog().info("Processing refset: " + refsetName);	    	

	    	List<I_RefsetUsageInterpreter.I_RefsetApplication> applications = 
	    		this.interpreter.getApplications(refsetName);
	    	I_ExportValueConverter populater = this.exportFactory.getValueConverter(startingVersion);
	    	for (I_RefsetUsageInterpreter.I_RefsetApplication app: applications) {
	    		populater.populateValues(app, conceptForDescription, description, extensionTuple, previousPart);
	    		
	    		if (app.getMasterfile().equals(ExportToEpicLoadFilesMojo.EPIC_MASTERFILE_NAME_WILDCARD)) {
	    			this.wildcardItems.add(new ValuePair(app.getItemNumber(), 
	    					populater.getItemValue(), populater.getPreviousItemValue()));
	    		}
	    		else {
		    		I_EpicLoadFileBuilder exportWriter = exportManager.getLoadFileBuilder(app.getMasterfile());
		    		if (app.getItemNumber().equals("2")) {
		    			if (isNewDisplayNameApplication(app.getMasterfile(), populater.getRegion())) {
		    				this.idTuple = extensionTuple;
		    				exportWriter.addItemForExport(app.getItemNumber(), 
		    						populater.getItemValue(), populater.getPreviousItemValue());
		    			}
		    		}
		    		else {
			    		//getLog().info("Exporting item " + this.currentItem + " with a value of " + stringValue +
			    		//		" and a previous value of " + previousStringValue);
		    		
			    		exportWriter.addItemForExport(app.getItemNumber(), 
			    				populater.getItemValue(), populater.getPreviousItemValue());
		    		}
	    		}
	    	}
	    }
	    
	    
	    
		public String convertToIntString(String str) {
	    	String ret = null;
	    	try {
	    		ret = new Integer(str).toString();
	    	}
	    	catch (NumberFormatException e) {
	    		ret = null; // Ignore
	    	}
	    	return ret;
	    }
	    
	    public void writeWildcardValues(I_EpicLoadFileBuilder builder) {
	    	for (ValuePair p: this.wildcardItems)
	    		builder.addItemForExport(p.getItemNumber(), p.getValue(), p.getPreviousValue());
	    }
	    public void setCurrentItem(String masterFile, String item) {
	    	this.currentMasterFile = masterFile;
	    	this.currentItem = item;
	    }
	    	    
	    public String getIdForConcept(I_GetConceptData concept, String idTypeUUID) throws Exception {
	    	String ret = null;
	    	//getLog().info("Looking for id in: " + concept);
			I_GetConceptData idSourceConcept = termFactory.getConcept(new UUID[] { UUID
					.fromString(idTypeUUID) }); 
			int idSourceNid = idSourceConcept.getConceptId();
			for (I_IdPart part : concept.getIdentifier().getMutableIdParts()) {
				if (part.getAuthorityNid() == idSourceNid) {
					ret = part.getDenotation().toString();
					break;
				}
			}
			return ret;
	    }
	    
	    public boolean isNewDisplayNameApplication(String masterFile, String region) {
	    	boolean found = false;
	    	boolean ret = false;
	    	for (String m : this.masterFilesImpacted) {
	    		if ( m.equals(masterFile)) {
	    			found = true;
	    			break;
	    		}
	    	}
	    	if (!found)
	    		this.masterFilesImpacted.add(masterFile);
	    	ret = !found;
	    	found = false;
	    	for (DisplayName d : this.displayNames) {
	    		if (d.getMasterFile().equals(masterFile) && d.getRegion().equals(region)) {
	    			found = true;
	    			break;
	    		}
	    	}
	    	if (!found)
	    		this.displayNames.add(new DisplayName(masterFile, region));
	    	return ret;
	    }
	    
	    public List<String> getRegions(String masterFile) {
	    	ArrayList<String> ret = new ArrayList<String>();
	    	for (DisplayName d:  this.displayNames) {
	    		if (d.getMasterFile().equals(masterFile))
	    			ret.add(d.getRegion());
	    	}
	    	return ret;
	    }
	    
	    private class EpicItemIdentifier {
	    	String masterfile;
	    	String itemNumber;
			
	    	public EpicItemIdentifier(String masterfile, String ItemNumber) {
	    		this.setMasterfile(masterfile);
	    		this.setItemNumber(ItemNumber);
	    	}
	    	
	    	public String getMasterfile() {
				return masterfile;
			}
			public void setMasterfile(String masterfile) {
				this.masterfile = masterfile;
			}
			public String getItemNumber() {
				return itemNumber;
			}
			public void setItemNumber(String itemNumber) {
				this.itemNumber = itemNumber;
			}
	    }
	    
	    private class DisplayName {
	    	public String masterFile;
	    	public String region;
	    	
	    	public DisplayName(String masterFile, String region) {
	    		this.setMasterFile(masterFile);
	    		this.setRegion(region);
	    	}

			public String getMasterFile() {
				return masterFile;
			}

			public void setMasterFile(String masterFile) {
				this.masterFile = masterFile;
			}

			public String getRegion() {
				return region;
			}

			public void setRegion(String region) {
				this.region = region;
			}
	    }
	    
	    private class ValuePair {
	    	String itemNumber;
	    	String value;
	    	String previousValue;
	    	
	    	public ValuePair(String item, String val, String previousVal) {
	    		setItemNumber(item);
	    		setValue(val);
	    		setPreviousValue(previousVal);
	    	}
	    	
			public String getItemNumber() {
				return itemNumber;
			}
			public void setItemNumber(String itemNumber) {
				this.itemNumber = itemNumber;
			}
			public String getValue() {
				return value;
			}
			public void setValue(String value) {
				this.value = value;
			}
			public String getPreviousValue() {
				return previousValue;
			}
			public void setPreviousValue(String previousValue) {
				this.previousValue = previousValue;
			}
	    }
    }


}

