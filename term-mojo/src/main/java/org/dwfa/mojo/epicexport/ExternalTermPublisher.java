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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefTuple;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.ace.log.AceLog;
import org.dwfa.tapi.TerminologyException;

/**
 * Class to mine a concept and its descriptions for extensions that are used to indicate the concept is
 * used for terms that are term records in an external system.  In this class's construct method, pass a factory
 * that is used to get classes for interpreting the refset names and values.  This way multiple entities
 * can share this class.
 * 
 * @author Steven Neiner
 *
 */
public class ExternalTermPublisher {
	private I_ExportFactory exportFactory;
	private EpicExportManager exportManager;
	private int startingVersion;
	private List<String> masterFilesImpacted;
	private I_ThinExtByRefTuple idTuple;
	private List<DisplayName> displayNames;
	private I_RefsetUsageInterpreter interpreter;
	private List<ValuePair> wildcardItems;
	private HashSet<I_Position> positions;
	private I_IntSet statusValues;
	private List<ExternalTermRecord> recordQueue;
	private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd'T'hhmmss'Z'");
	private I_GetConceptData rootConcept;
	private I_GetConceptData descConcept;
	private I_ExportValueConverter converter;

	
	public ExternalTermPublisher(I_ExportFactory exportFactory) throws Exception
	{
		this.exportFactory = exportFactory;
		this.interpreter = exportFactory.getInterpreter();
		this.converter = this.exportFactory.getValueConverter(Integer.MAX_VALUE);
	}

	public void close() throws Exception {
		this.exportManager.close();
	}
	
	/**
	 * Only concepts with these positions will be returned
	 * 
	 * @param positions
	 */
	public void setPositions(HashSet<I_Position> positions) {
		this.positions = positions;
	}

	/**
	 * Only concepts with these statuses will be returned
	 * 
	 * @param statusValues
	 */
	public void setStatusValues(I_IntSet statusValues) {
		this.statusValues = statusValues;
	}
	
	/**
	 * Set the starting date, in the format yyyymmssThhmmssZ+8.  This will cause the previous values to
	 * be populated with values as they were on this date.
	 * 
	 * @param date - The date string  in the format yyyymmssThhmmssZ+8
	 */
	public void setStartingDate(String date) {
		Date parsedDate = new Date();
		try {
			parsedDate = dateFormat.parse(date);
		} catch (java.text.ParseException e) {
			AceLog.getAppLog().warning("Invalid date: " + date + " - use format yyyymmssThhmmssZ+8");
			e.printStackTrace();
		}
    	
    	this.startingVersion = Terms.get().convertToThinVersion(parsedDate.getTime());
    	this.converter = this.exportFactory.getValueConverter(startingVersion);
	}

	/**
	 * Given a concept, looks for any extensions in that concept, or its descriptions, that indicate the 
	 * concept has terms that are used as term master file records in an external system, such as Epic.
	 * 
	 * @param concept - The concept to look through for extensions attached to it and its descriptions
	 * @return List of ExternalTermRecords that are associated with the concept
	 * @throws Exception
	 */
	public List<ExternalTermRecord> getExternalTermRecordsForConcept(I_GetConceptData concept) throws Exception {
		masterFilesImpacted = new ArrayList<String>();
		displayNames = new ArrayList<DisplayName>();
		wildcardItems = new ArrayList<ValuePair>();
		List<ExternalTermRecord> externalRecords = new ArrayList<ExternalTermRecord>();
		this.recordQueue = new ArrayList<ExternalTermRecord>();
		List<ExternalTermRecord> standAloneRecords = new ArrayList<ExternalTermRecord>();
		List<ExternalTermRecord> commonItemRecords = new ArrayList<ExternalTermRecord>();
		
		this.idTuple = null;
		List<? extends I_DescriptionVersioned> descs = concept.getDescriptions();

		this.rootConcept = concept;
		int extensionsProcessed = 0;
		for (I_DescriptionVersioned desc : descs) {
			I_GetConceptData descriptionConcept = Terms.get().getConcept(desc.getConceptId());
			extensionsProcessed += processDescriptionConcept(descriptionConcept, desc);
			writeWildcardValues();
			saveAndCloseRecordQueue(concept, standAloneRecords, commonItemRecords);
		}
		// Finally, process this concept directly if it is a description concept, or process the root concept
		// for extensions attached to the root concept
		processDescriptionConcept(concept, null);
        writeWildcardValues();
        saveAndCloseRecordQueue(concept, standAloneRecords, commonItemRecords);
        //Add all of the common items to each stand-alone record
        for (ExternalTermRecord r : standAloneRecords) {
        	for (ExternalTermRecord c : commonItemRecords) {
        		if (c.getMasterFileName().equals(r.getMasterFileName())) {
        			for (ExternalTermRecord.Item i : c.getAllItems())
        				r.addItem(i);
        		}
        	}
        	externalRecords.add(r);
        }
        return externalRecords;
	}
	
		
	private void saveAndCloseRecordQueue(I_GetConceptData concept, List<ExternalTermRecord> standAloneRecords, 
			List<ExternalTermRecord> commonItemRecords) throws Exception {
		for (ExternalTermRecord r: this.recordQueue) {
			//If the description is a stand-alone term, usually meaning the term has a display name
			if (this.converter.recordIsStandAloneTerm(r)) {
				if (this.idTuple != null)
					this.converter.addRecordIds(this.idTuple, concept, r);
				standAloneRecords.add(r);

			}
			else
				commonItemRecords.add(r);
		}
		this.recordQueue.clear();

	}

    private int processDescriptionConcept(I_GetConceptData concept, I_DescriptionVersioned description) throws TerminologyException, Exception {
    	List<? extends I_ThinExtByRefVersioned> extensions;
    	this.descConcept = concept;
    	if (description != null)
    		extensions = Terms.get().getAllExtensionsForComponent(description.getDescId());
    	else
    		extensions = Terms.get().getAllExtensionsForComponent(concept.getConceptId());
    	for (I_ThinExtByRefVersioned thinExtByRefVersioned : extensions) {
        	if (Terms.get().hasConcept(thinExtByRefVersioned.getRefsetId())) {
                for (I_ThinExtByRefTuple thinExtByRefTuple : thinExtByRefVersioned.getTuples(statusValues,
                    positions, false, false)) {
                	processExtension(thinExtByRefTuple, concept, description);
                }
        	}
        	else {
        		throw new Exception("No concept for ID " + thinExtByRefVersioned.getRefsetId());
        	}
        }
        return extensions.size();
    }

    private void processExtension(I_ThinExtByRefTuple extensionTuple, I_GetConceptData extendedConcept, 
    		I_DescriptionVersioned description) throws Exception {
    	
    	int refsetId = extensionTuple.getRefsetId();
    	I_GetConceptData refsetConcept = Terms.get().getConcept(refsetId);
    	mineRefsetsForItems(refsetConcept, extensionTuple, extendedConcept, description, 
    			getPreviousVersion(extensionTuple));
    }
    
    private I_ThinExtByRefPart getPreviousVersion(I_ThinExtByRefTuple thinExtByRefTuple) throws Exception {
    	    	
    	return ExternalTermPublisher.getPreviousVersionOfExtension(thinExtByRefTuple, startingVersion);
    }

    public static I_ThinExtByRefPart getPreviousVersionOfExtension(I_ThinExtByRefTuple thinExtByRefTuple,
    		int startingVersion) throws Exception {
    	List<? extends I_ThinExtByRefPart> versions = thinExtByRefTuple.getVersions();
    	
    	I_ThinExtByRefPart newestOldVersion  = null;
    	for (Iterator<? extends I_ThinExtByRefPart> i = versions.iterator(); i.hasNext(); ) {
    		I_ThinExtByRefPart v = i.next();
    		if (v.getVersion() <= startingVersion) {
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
    
    
    private void mineRefsetsForItems(I_GetConceptData refsetConcept, 
    		I_ThinExtByRefTuple extensionTuple, I_GetConceptData conceptForDescription,
    		I_DescriptionVersioned description, I_ThinExtByRefPart previousPart) throws Exception {
    	// this.currentItem = null;
    	String refsetName = refsetConcept.getInitialText();
    	// System.out.println("Processing refset " + refsetName);
    	List<I_RefsetUsageInterpreter.I_RefsetApplication> applications = 
    		this.interpreter.getApplications(refsetName);
    	
    	for (I_RefsetUsageInterpreter.I_RefsetApplication app: applications) {
    		this.converter.populateValues(app, conceptForDescription, description, extensionTuple, previousPart);
    		
    		if (app.getMasterfile().equals(ExportToEpicLoadFilesMojo.EPIC_MASTERFILE_NAME_WILDCARD)) {
    			this.wildcardItems.add(new ValuePair(app.getItemNumber(), 
    					this.converter.getItemValue(), this.converter.getPreviousItemValue(),
    					extensionTuple));
    		}
    		else {
	    		if (app.itemIsTermName()) {
	    			if (isNewDisplayNameApplication(app.getMasterfile(), this.converter.getRegion())) {
	    				this.idTuple = extensionTuple;
	    				addItem(app.getMasterfile(), app.getItemNumber(), 
	    						this.converter.getItemValue(), this.converter.getPreviousItemValue(),
	    						extensionTuple);
	    				addRegion(app.getMasterfile(), app.getRegion());
	    				saveDescriptionStatus(app.getMasterfile(), description);
	    			}
	    		}
	    		else {
    				addItem(app.getMasterfile(), app.getItemNumber(), 
    						this.converter.getItemValue(), this.converter.getPreviousItemValue(),
    						extensionTuple);
	    		}
    		}
    	}
    }
    
    private void addRegion(String masterfile, String region) {
    	ExternalTermRecord record = getExternalRecordForMasterfile(masterfile);
    	record.addRegion(region);
    }

    private void saveDescriptionStatus(String masterfile, I_DescriptionVersioned description) throws Exception {
    	ExternalTermRecord record = getExternalRecordForMasterfile(masterfile);
    	if (description != null) {
	    	I_DescriptionTuple dt = description.getLastTuple();
	    	if (dt != null) {
		    	int statusId = description.getLastTuple().getMutablePart().getStatusId();
		    	record.setTermStatus(this.converter.getInterpretedStatus(statusId));
	    	}
	    	record.setPreviousStatus(getPreviousStatus(description));
    	}
    }
    
    private ExternalTermRecord.status getPreviousStatus(I_DescriptionVersioned d) throws Exception {
    	ExternalTermRecord.status ret = null;

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
    		ret = this.converter.getInterpretedStatus(newestOldTuple.getMutablePart().getStatusId());
    	return ret;
    }

    
    private void addItem(String masterFile, String item, Object value, Object previousValue,
    		I_ThinExtByRefTuple extensionTuple) {
    	ExternalTermRecord record = getExternalRecordForMasterfile(masterFile);
    	record.addItem(item, value, previousValue, extensionTuple);
    }
    
    private ExternalTermRecord getExternalRecordForMasterfile(String masterFile) {
    	ExternalTermRecord ret = null;
    	if (this.recordQueue == null)
    		this.recordQueue = new ArrayList<ExternalTermRecord>();
    	
    	for (ExternalTermRecord er: this.recordQueue) {
    		if (er.getMasterFileName().equals(masterFile))
    			ret = er;
    	}
    	if (ret == null) {
    		ret = new ExternalTermRecord(masterFile);
    		ret.setOwningConcept(descConcept);
    		ret.setRootConcept(rootConcept);
    		ret.setCreatingFactory(this.exportFactory);
    		this.recordQueue.add(ret);
    	}
    	return ret;
    }
    
    private void writeWildcardValues() {
    	for (ValuePair p: this.wildcardItems) {
    		for (String m: this.masterFilesImpacted) {
    			addItem(m, p.getItemNumber(), 
    					p.getValue(), p.getPreviousValue(), p.getExtensionTuple());

    		}
    	}
    	this.wildcardItems.clear();
    }
    

    private boolean isNewDisplayNameApplication(String masterFile, String region) {
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
    	I_ThinExtByRefTuple extensionTuple;
    	
    	public ValuePair(String item, String val, String previousVal, I_ThinExtByRefTuple extensionTuple) {
    		setItemNumber(item);
    		setValue(val);
    		setPreviousValue(previousVal);
    		setExtensionTuple(extensionTuple);
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

		public I_ThinExtByRefTuple getExtensionTuple() {
			return extensionTuple;
		}

		public void setExtensionTuple(I_ThinExtByRefTuple extensionTuple) {
			this.extensionTuple = extensionTuple;
		}
		
    }

}
