package org.dwfa.mojo.epicexport;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefTuple;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.ace.log.AceLog;
import org.dwfa.tapi.TerminologyException;

public class ExternalTermPublisher {
	private I_ExportFactory exportFactory;
	private EpicExportManager exportManager;
	private int startingVersion;
	private List<String> masterFilesImpacted;
	private I_ThinExtByRefTuple idTuple;
	private List<DisplayName> displayNames;
	private I_RefsetUsageInterpreter interpreter;
	private List<ValuePair> wildcardItems;
	private I_TermFactory termFactory;
	private HashSet<I_Position> positions;
	private I_IntSet statusValues;
	private List<ExternalTermRecord> externalRecords;
	private List<ExternalTermRecord> recordQueue;
	private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd'T'hhmmss'Z'");
	private I_GetConceptData rootConcept;
	private String currentItem;
	private String currentMasterFile;

	
	public ExternalTermPublisher(I_ExportFactory exportFactory) throws Exception
	{
		this.exportFactory = exportFactory;
		this.interpreter = exportFactory.getInterpreter();
		this.termFactory = LocalVersionedTerminology.get();
	}

	public void close() throws Exception {
		this.exportManager.close();
	}
	
	public void setPositions(HashSet<I_Position> positions) {
		this.positions = positions;
	}

	public void setStatusValues(I_IntSet statusValues) {
		this.statusValues = statusValues;
	}
	
	public void setStartingDate(String d) {
		Date parsedDate = new Date();
		try {
			parsedDate = dateFormat.parse(d);
		} catch (java.text.ParseException e) {
			AceLog.getAppLog().warning("Invalid date: " + d + " - use format yyyymmssThhmmssZ+8");
			e.printStackTrace();
		}
    	
    	this.startingVersion = termFactory.convertToThinVersion(parsedDate.getTime());

	}

	public List<ExternalTermRecord> getExternalTermRecordsForConcept(I_GetConceptData concept) throws Exception {
		masterFilesImpacted = new ArrayList<String>();
		displayNames = new ArrayList<DisplayName>();
		wildcardItems = new ArrayList<ValuePair>();
		this.externalRecords = new ArrayList<ExternalTermRecord>();
		this.recordQueue = new ArrayList<ExternalTermRecord>();
		
		this.idTuple = null;
		List<? extends I_DescriptionVersioned> descs = concept.getDescriptions();

		this.setCurrentItem(null, null);
		this.rootConcept = concept;
		int extensionsProcessed = 0;
		int recsAdded = 0;
		for (I_DescriptionVersioned desc : descs) {
			I_GetConceptData descriptionConcept = termFactory.getConcept(desc.getConceptId());
			extensionsProcessed += processDescriptionConcept(descriptionConcept, desc);

			/* processDescriptionConcept(concept, null);
			if (this.idTuple != null)
				this.exportFactory.getValueConverter(startingVersion).
					addRecordIds(this.idTuple, this.recordQueue, concept);
			writeWildcardValues();
			recsAdded += addQueuedRecords();
			*/
		}
		// if (recsAdded == 0) {
			// Finally, process this concept directly if it is a description concept, or process the root concept
			// for extensions attached to the root concept
			processDescriptionConcept(concept, null);
			if (this.idTuple != null)
				this.exportFactory.getValueConverter(startingVersion).
					addRecordIds(this.idTuple, this.recordQueue, concept);
	        writeWildcardValues();
	        addQueuedRecords();
		// }
        return this.externalRecords;
	}
	
	public int addQueuedRecords() {
		int count = 0;
		ExternalTermRecord d = null;
		for (ExternalTermRecord r: this.recordQueue) {
			if (r.getFirstItem("2") != null) {
				// System.out.println("Adding record " + r.toString());			
				this.externalRecords.add(r);
				d = r;
				count++;
			}
		}
		if (d != null)
			this.recordQueue.remove(d);
		// this.recordQueue = new ArrayList<ExternalTermRecord>();
		return count;
	}

    private int processDescriptionConcept(I_GetConceptData concept, I_DescriptionVersioned description) throws TerminologyException, Exception {
    	List<I_ThinExtByRefVersioned> extensions;
    	if (description != null)
    		extensions = termFactory.getAllExtensionsForComponent(description.getDescId());
    	else
    		extensions = termFactory.getAllExtensionsForComponent(concept.getConceptId());
    	for (I_ThinExtByRefVersioned thinExtByRefVersioned : extensions) {
        	if (termFactory.hasConcept(thinExtByRefVersioned.getRefsetId())) {
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
    	I_GetConceptData refsetConcept = termFactory.getConcept(refsetId);
    	mineRefsetsForItems(refsetConcept, extensionTuple, extendedConcept, description, 
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

    
    private void mineRefsetsForItems(I_GetConceptData refsetConcept, 
    		I_ThinExtByRefTuple extensionTuple, I_GetConceptData conceptForDescription,
    		I_DescriptionVersioned description, I_ThinExtByRefPart previousPart) throws Exception {
    	this.currentItem = null;
    	String refsetName = refsetConcept.getInitialText();
    	
    	// I_ThinExtByRefPart extensionTuplePart = extensionTuple.getMutablePart();

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
	    		if (app.getItemNumber().equals("2")) {
	    			if (isNewDisplayNameApplication(app.getMasterfile(), populater.getRegion())) {
	    				this.idTuple = extensionTuple;
	    				addItem(app.getMasterfile(), app.getItemNumber(), 
	    						populater.getItemValue(), populater.getPreviousItemValue());
	    				addRegion(app.getMasterfile(), app.getRegion());
	    			}
	    		}
	    		else {
    				addItem(app.getMasterfile(), app.getItemNumber(), 
    						populater.getItemValue(), populater.getPreviousItemValue());
	    		}
    		}
    	}
    }
    
    private void addRegion(String masterfile, String region) {
    	ExternalTermRecord record = getExternalRecordForMasterfile(masterfile);
    	record.addRegion(region);

    }
    
    private void addItem(String masterFile, String item, Object value, Object previousValue) {
    	ExternalTermRecord record = getExternalRecordForMasterfile(masterFile);
    	record.addItem(item, value, previousValue);
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
    		this.recordQueue.add(ret);
    	}
    	return ret;
    }
    
    private String convertToIntString(String str) {
    	String ret = null;
    	try {
    		ret = new Integer(str).toString();
    	}
    	catch (NumberFormatException e) {
    		ret = null; // Ignore
    	}
    	return ret;
    }
    
    private void writeWildcardValues() {
    	for (ValuePair p: this.wildcardItems) {
    		for (String m: this.masterFilesImpacted) {
    			addItem(m, p.getItemNumber(), 
    					p.getValue(), p.getPreviousValue());

    		}
    	}
    }
    private void setCurrentItem(String masterFile, String item) {
    	this.currentMasterFile = masterFile;
    	this.currentItem = item;
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
    
    public List<String> getRegions(String masterFile) {
    	ArrayList<String> ret = new ArrayList<String>();
    	for (DisplayName d:  this.displayNames) {
    		if (d.getMasterFile().equals(masterFile))
    			ret.add(d.getRegion());
    	}
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
