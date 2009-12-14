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
import java.util.Set;
import java.util.UUID;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionPart;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IdPart;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_ProcessConcepts;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LineageHelper;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartBoolean;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConcept;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConceptString;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartInteger;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartString;
import org.dwfa.ace.api.ebr.I_ThinExtByRefTuple;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.ace.task.profile.NewDefaultProfile;
import org.dwfa.maven.MojoUtil;
import org.dwfa.mojo.ConceptDescriptor;
import org.dwfa.mojo.PositionDescriptor;
import org.dwfa.mojo.epicexport.kp.EpicLoadFileFactory;
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
	/**
	 * Location of the directory to output data files to.
	 *
	 * @parameter expression="${project.build.directory}"
	 * @required
	 */
	private String outputDirectory;
	
	/**
	 * If true, do not create thesaurus entries for preferred terms with no synonyms.
	 *
	 * 
	 * 
	 */
	// private ExportSpecification[] specs;

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
			getLog().info(
					" processing concepts for positions: " + positions
							+ " with status: " + statusValueList);

			if (outputDirectory.endsWith("/") == false) {
				outputDirectory = outputDirectory + "/";
			}
			
			ExportIterator expItr = new ExportIterator(this);
			
			
			/*
			 * Single process debuging sample EDG Clinical = 3073adf3-0c10-3cbb-975f-7bfc0c9cbd17 
			 * EDG Clinical soft delete = 6b9d08a1-d645-3d85-84aa-85c90e48c53d
			 * EDG Clinical description concept 528a6294-a8be-5443-ac3d-e87195f88191
			 * EDG Billing = a7130b8c-e6c1-57d8-986a-0d88552c12e4
			 * 
			*/
			
			// I_GetConceptData concept = termFactory.getConcept(UUID.fromString("e9bba573-b160-4fcc-b4a1-7ec9ab019046"));
			// expItr.processConcept(concept);
			
			
			// Iterate through all concepts
            LocalVersionedTerminology.get().iterateConcepts(expItr);
			expItr.close();

		} catch (Exception e) {
			getLog().error(e);
			throw new MojoExecutionException("Unable to export database due to exception", e);
		}

	}// End method execute
     
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
		
		public ExportIterator(ExportToEpicLoadFilesMojo parent) throws Exception
		{
			Date parsedDate = new Date();
			
			if (parent.writerName.equals("kp loadfile export")) {
				exportFactory = new EpicLoadFileFactory();
				exportManager = exportFactory.getExportManager(parent.outputDirectory);
			}
			else if (parent.writerName.equals("kp term warehouse build")){
				
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
		}

		public void close() throws Exception {
			this.exportManager.close();
		}
		
		public void processConcept(I_GetConceptData concept) throws Exception {
			masterFilesImpacted = new ArrayList<String>();
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
			// getLog().info("Processing root concept");
			// if (extensionsProcessed == 0)
			processDescriptionConcept(concept, null);
	        for (String masterfile: masterFilesImpacted) {
		        I_EpicLoadFileBuilder exportWriter = exportManager.getLoadFileBuilder(masterfile);
		        if (exportWriter != null) { 
			        exportWriter.writeRecord(this.dropName);
			        exportWriter.clearRecordContents();
		        }
		        this.currentMasterFile = null;
	        }
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
        	if (lastTuple != null)
        		this.writeRecordIds(lastTuple);
	        
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
	    	String stringValue = null;
	    	String previousStringValue = null;
	    	List<EpicItemIdentifier> refsetAppliesTo = new ArrayList<EpicItemIdentifier>();
	    	
	    	I_ThinExtByRefPart extensionTuplePart = extensionTuple.getPart();
	    	// getLog().info("Processing refset: " + refsetName);	    	
	    	//TODO: Re-factor into separate class, allow pattern matching, store and read from pom.xml
	    	if(refsetName.equals("EDG Billing Item 207")) {
	    		refsetAppliesTo.add(new EpicItemIdentifier(
	    				EpicExportManager.EPIC_MASTERFILE_NAME_EDG_BILLING, "207"));
	    		refsetAppliesTo.add(new EpicItemIdentifier(
	    				EpicExportManager.EPIC_MASTERFILE_NAME_EDG_CLINICAL, "207"));
	    	}
	    	else if(refsetName.equals("EDG Billing Item 91")) {
	    		refsetAppliesTo.add(new EpicItemIdentifier(
	    				EpicExportManager.EPIC_MASTERFILE_NAME_EDG_BILLING, "91"));
	    		refsetAppliesTo.add(new EpicItemIdentifier(
	    				EpicExportManager.EPIC_MASTERFILE_NAME_EDG_CLINICAL, "91"));
// getLog().info("Found billing 91");
	    	}
	    	else if(refsetName.equals("EDG Billing Item 2")) {
	    		refsetAppliesTo.add(new EpicItemIdentifier(
	    				EpicExportManager.EPIC_MASTERFILE_NAME_EDG_BILLING, "2"));
	    		stringValue = getDisplayName(conceptForDescription); 
	    		previousStringValue = getPreviousDisplayName(conceptForDescription); 
	    	}
	    	else if(refsetName.startsWith("EDG Billing Item ")) {
	    		String item = refsetName.substring(17);
	    		refsetAppliesTo.add(new EpicItemIdentifier(
	    				EpicExportManager.EPIC_MASTERFILE_NAME_EDG_BILLING, item));
	    	}
	    	else if(refsetName.equals("EDG Billing Contact Date")) {
	    		this.setCurrentItem(EpicExportManager.EPIC_MASTERFILE_NAME_EDG_BILLING, "20");
	    	}

	    	
	    	/**
	    	 *  EDG Clinical refsets
	    	 */
	    	else if(refsetName.equals("EDG Clinical Item 2 National")) {
	    		refsetAppliesTo.add(new EpicItemIdentifier(
	    				EpicExportManager.EPIC_MASTERFILE_NAME_EDG_CLINICAL, "2"));

	    		if (description != null) {
	    			stringValue = description.getLastTuple().getPart().getText();
	    			previousStringValue = getPreviousDisplayName(description);
	    		}
	    		// getLog().info("Found display name: " + stringValue + " / " + previousStringValue);	
	    	}
	    	else if (refsetName.startsWith("EDG Clinical Item 2 "))
	    	{
	    		// String region = refsetName.substring(20);
	    		refsetAppliesTo.add(new EpicItemIdentifier(
	    				EpicExportManager.EPIC_MASTERFILE_NAME_EDG_CLINICAL, "2"));
	    		if (description != null) {
	    			stringValue = description.getLastTuple().getPart().getText();
	    			previousStringValue = stringValue;
	    		}
	    		
	    	}
	    	else if(refsetName.equals("EDG Clinical Item 2")) {
	    		refsetAppliesTo.add(new EpicItemIdentifier(
	    				EpicExportManager.EPIC_MASTERFILE_NAME_EDG_CLINICAL, "2"));
	    		stringValue = getDisplayName(conceptForDescription); 
	    		previousStringValue = getPreviousDisplayName(conceptForDescription); 
	    	}
	    	else if(refsetName.equals("EDG Clinical Item 50")) {
	    		
	    		//refsetAppliesTo.add(new EpicItemIdentifier(
	    		//		EpicExportManager.EPIC_MASTERFILE_NAME_EDG_CLINICAL, "50"));
	    		// stringValue = getDisplayName(conceptForDescription);
	    		//previousStringValue = getPreviousDisplayName(conceptForDescription); 
	    		I_ThinExtByRefPartBoolean doAdd = (I_ThinExtByRefPartBoolean) extensionTuplePart;
	    		if (doAdd.getValue()) {
		    		List<I_DescriptionVersioned> descs = conceptForDescription.getDescriptions();
		    		for(I_DescriptionVersioned d: descs) {
		    			int type = d.getFirstTuple().getTypeId();
		    			if (type == -2147078660 || type == -2143913499) {
		    				String syn = d.getFirstTuple().getPart().getText();
	// getLog().info("Synonym: " + syn + " type=" + type); 
		    				if (!getDisplayName(conceptForDescription).equals(syn))
		    					exportManager.getLoadFileBuilder(EpicExportManager.EPIC_MASTERFILE_NAME_EDG_CLINICAL)
									.addItemForExport("50", syn, null);
		    			}
		    		}
	    		}
	    	}
	    	else if(refsetName.startsWith("EDG Clinical Item ")) {
	    		String item = refsetName.substring(18);
	    		refsetAppliesTo.add(new EpicItemIdentifier(
	    				EpicExportManager.EPIC_MASTERFILE_NAME_EDG_CLINICAL, item));
	    	}

	    	else if(refsetName.equals("Reason for Soft Delete")) {
	    		this.currentItem = "5";
	    		stringValue = "*SD";
	    		previousStringValue = "*SD";
	    	}
	    	else if (refsetName.equals("ICD10-CM Code Mapping Status") ||
	    			refsetName.equals("Path reference set") || 
	    			refsetName.equals("Path origin reference set")) {
	    		// Ignore
	    	}
	    		
	    	else 
	    		getLog().warn("Unhandled refset name: " + refsetName);
	    	
	    	for (EpicItemIdentifier e: refsetAppliesTo) {
	    		if (e.getItemNumber().equals("2"))
	    			masterFilesImpacted.add(e.getMasterfile());
	    		if (stringValue == null)
	    			stringValue = getValueAsString(extensionTuplePart);
	    		I_EpicLoadFileBuilder exportWriter = exportManager.getLoadFileBuilder(e.getMasterfile());
	    		if (previousStringValue == null)
	    			previousStringValue = getValueAsString(previousPart);
	    		//getLog().info("Exporting item " + this.currentItem + " with a value of " + stringValue +
	    		//		" and a previous value of " + previousStringValue);
	    		
	    		exportWriter.addItemForExport(e.getItemNumber(), stringValue, previousStringValue);
	    	}
	    	
	    }
	    
	    public void writeRecordIds(I_ThinExtByRefTuple extensionTuple) throws Exception {
    		/* Special post handling, such writing id when we encounter a display name */
	    	String dot11 = null;
	    	String dot1 = null;
	    	String uuid = null;
	    	
	    	I_GetConceptData idConcept = termFactory.getConcept(extensionTuple.getComponentId());
	    	
	    	for (String masterfile: masterFilesImpacted) {
		    	I_EpicLoadFileBuilder exportWriter = exportManager.getLoadFileBuilder(masterfile);
	    		
	    		exportWriter.setIdConcept(idConcept);
	    		uuid = getIdForConcept(idConcept, "2faa9262-8fb2-11db-b606-0800200c9a66");
	    		
	    		if(masterfile.equals(EpicExportManager.EPIC_MASTERFILE_NAME_EDG_CLINICAL)) {
					dot11 = getIdForConcept(idConcept, "e3dadc2a-196d-5525-879a-3037af99607d");
					dot1 = getIdForConcept(idConcept, "e49a55a7-319d-5744-b8a9-9b7cc86fd1c6");
	    		}
	    		else if(masterfile.equals(EpicExportManager.EPIC_MASTERFILE_NAME_EDG_BILLING)) {
					dot11 = getIdForConcept(idConcept, "bf3e7556-38cb-5395-970d-f11851c9f41e");
					dot1 = getIdForConcept(idConcept, "af8be384-dc60-5b56-9ad8-bc1e4b5dfbae");
	    		}
	    		if (dot11 != null)
	    			exportWriter.addItemForExport("11", dot11, dot11);
	    		if (dot1 != null)
	   				exportWriter.addItemForExport("1", dot1, dot1);
	   			exportWriter.addItemForExport("35", uuid, uuid);
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
	    
	    public String getValueAsString(I_ThinExtByRefPart thinExtByRefPart) {
	    	String value = null;
	    	if (thinExtByRefPart != null) {
		    	if (I_ThinExtByRefPartString.class.isAssignableFrom(thinExtByRefPart.getClass())) {
		    		I_ThinExtByRefPartString str = (I_ThinExtByRefPartString) thinExtByRefPart;
		    		value = str.getStringValue();
		    	}
		    	if (I_ThinExtByRefPartInteger.class.isAssignableFrom(thinExtByRefPart.getClass())) {
		    		I_ThinExtByRefPartInteger str = (I_ThinExtByRefPartInteger) thinExtByRefPart;
		    		value = new Integer(str.getIntValue()).toString();
		    	}
		    	if (I_ThinExtByRefPartBoolean.class.isAssignableFrom(thinExtByRefPart.getClass())) {
		    		I_ThinExtByRefPartBoolean str = (I_ThinExtByRefPartBoolean) thinExtByRefPart;
		    		value = (str.getValue()) ? "1" : "0";
		    	}
	    	}
	    	return value;

	    }
	    public void setCurrentItem(String masterFile, String item) {
	    	this.currentMasterFile = masterFile;
	    	this.currentItem = item;
	    }
	    
	    public String getDisplayName(I_GetConceptData conceptData) throws Exception {
	    	String ret = null;
	
	    	List<? extends I_DescriptionVersioned> descs = conceptData.getDescriptions();
	    	for (Iterator<? extends I_DescriptionVersioned> i = descs.iterator(); i.hasNext();) {
	    		I_DescriptionVersioned d = i.next();
	    		I_DescriptionTuple dt = d.getLastTuple();
	    		I_DescriptionPart part = dt.getPart();
	    		ret = part.getText();
	    	}
	    	
	    	return ret;
	    }

	    public String getPreviousDisplayName(I_GetConceptData conceptData) throws Exception {
	    	String ret = null;
	
	    	List<? extends I_DescriptionVersioned> descs = conceptData.getDescriptions();
	    	I_DescriptionTuple newestOldTuple = null;
	    	for (Iterator<? extends I_DescriptionVersioned> i = descs.iterator(); i.hasNext();) {
	    		I_DescriptionVersioned d = i.next();
	    		for (I_DescriptionTuple dt : d.getTuples()) {
	    			if (dt.getVersion() < this.startingVersion)
	    				if (newestOldTuple == null) {
	    					newestOldTuple = dt;
	    				}
	    				else if (dt.getVersion() > newestOldTuple.getVersion()) {
	    					newestOldTuple = dt;
	    				}
	    		}
	    	}
	    	if (newestOldTuple != null)
	    		ret = newestOldTuple.getPart().getText();
	    	return ret;
	    }
	    

	    public String getPreviousDisplayName(I_DescriptionVersioned d) throws Exception {
	    	String ret = null;
	

	    	I_DescriptionTuple newestOldTuple = null;
    		for (I_DescriptionTuple dt : d.getTuples()) {
    			if (dt.getVersion() < this.startingVersion)
    				if (newestOldTuple == null) {
    					newestOldTuple = dt;
    				}
    				else if (dt.getVersion() > newestOldTuple.getVersion()) {
    					newestOldTuple = dt;
    				}
    		}
	    	if (newestOldTuple != null)
	    		ret = newestOldTuple.getPart().getText();
	    	return ret;
	    }
	    
	    public String getIdForConcept(I_GetConceptData concept, String idTypeUUID) throws Exception {
	    	String ret = null;
	    	//getLog().info("Looking for id in: " + concept);
			I_GetConceptData idSourceConcept = termFactory.getConcept(new UUID[] { UUID
					.fromString(idTypeUUID) }); 
			int idSourceNid = idSourceConcept.getConceptId();
			for (I_IdPart part : concept.getId().getVersions()) {
				if (part.getSource() == idSourceNid) {
					ret = part.getSourceId().toString();
					break;
				}
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
    }


}

