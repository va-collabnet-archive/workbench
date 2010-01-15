package org.dwfa.mojo.epicexport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartBoolean;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartInteger;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartString;
import org.dwfa.ace.api.ebr.I_ThinExtByRefTuple;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;

/**
 * Class used to describe a record of an external system that data is exported to.
 * 
 * @author Steven Neiner
 *
 */
public class ExternalTermRecord {
	private static int nidVersion = Integer.MAX_VALUE;
	private static int nidRetired = Integer.MIN_VALUE;
	private static int nidCurrent = Integer.MIN_VALUE;
	private static int nidEditPath = Integer.MIN_VALUE; 
	private String masterFileName;
	private String version;
	private List<String> regions;
	private List<Item> items;
	private I_GetConceptData owningConcept;
	private I_GetConceptData rootConcept;
	private I_ExportFactory creatingFactory;
	
	public ExternalTermRecord(String name) {
		this.setMasterFileName(name);
	}
	
	/** 
	 * Returns the name of the master file for this record.
	 * 
	 * @return String - the name of the master file
	 */
	public String getMasterFileName() {
		return masterFileName;
	}

	/**
	 * Sets the name of the master file
	 * 
	 * @param masterFileName
	 */
	public void setMasterFileName(String masterFileName) {
		this.masterFileName = masterFileName;
	}
	
	/**
	 * Adds a region or locality that uses this record.
	 * 
	 * @param region
	 */
	public void addRegion(String region) {
		if (this.regions == null)
			regions = new ArrayList<String>();
		regions.add(region);
	}
	
	/**
	 * Returns a list of regions or localities that uses the record.
	 * 
	 * @return String list of regions
	 */
	public List<String> getRegions() {
		return regions;
	}

	/**
	 * Get the version of the record.
	 * 
	 * @return String version name
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * Sets the version or the record.  Version is an arbitrary name, external to IHTSDO Workbench,
	 * used to identify the period when a change was applied.
	 * 
	 * @param version
	 */
	public void setVersion(String version) {
		this.version = version;
	}

	public I_GetConceptData getOwningConcept() {
		return owningConcept;
	}

	public void setOwningConcept(I_GetConceptData owningConcept) {
		this.owningConcept = owningConcept;
	}

	public I_GetConceptData getRootConcept() {
		return rootConcept;
	}

	public void setRootConcept(I_GetConceptData rootConcept) {
		this.rootConcept = rootConcept;
	}

	public I_ExportFactory getCreatingFactory() {
		return creatingFactory;
	}

	public void setCreatingFactory(I_ExportFactory creatingFactory) {
		this.creatingFactory = creatingFactory;
	}

	/**
	 * Gets the first occurrence of an item with the supplied name
	 * 
	 * @param name String
	 * @return Item
	 */
	public Item getFirstItem(String name) {
		Item ret = null;
		for (Item i: this.items) {
			if (i.getName().equalsIgnoreCase(name))
				return i;
		}	
		return ret;
	}
	
	/**
	 * Returns of list of Items for the supplied name.
	 * 
	 * @param name The name of items to return
	 * @return List of Items
	 */
	public List<Item> getItems(String name) {
		List<Item> ret = new ArrayList<Item>();
		for (Item i: this.items) {
			if (i.getName().equalsIgnoreCase(name))
				ret.add(i);
		}	
		return ret;
	}
	
	/**
	 * Gets all items for this record
	 * 
	 * @return List<Item> of all items
	 */
	public List<ExternalTermRecord.Item> getAllItems() {
		return this.items;
	}
	
	/**
	 * Returns true if an item exists
	 * @param name
	 * @return
	 */
	public boolean hasItem(String name) {
		boolean ret = false;
		for (Item i: this.items) {
			if (i.getName().equalsIgnoreCase(name))
				return true;
		}	
		return ret;
	}

	/**
	 * Adds a value item to a record
	 * 
	 * @param name The name of the item
	 * @param value The value of the item
	 */
	public void addItem(String name, Object value) {
		if (items == null)
			items = new ArrayList<Item>();
		items.add(new Item(name, value));
	}
	
	/**
	 * Adds a value item to a record
	 * 
	 * @param name The name of the item
	 * @param value The value of the item
	 * @param previousValue The previous value of an item
	 */
	public void addItem(String name, Object value, Object previousValue) {
		if (items == null)
			items = new ArrayList<Item>();
		items.add(new Item(name, value, previousValue));
	}

	public void addItem(String name, Object value, Object previousValue, I_ThinExtByRefTuple extensionTuple_) {
		if (items == null)
			items = new ArrayList<Item>();
		items.add(new Item(name, value, previousValue, extensionTuple_));
	}
	/**
	 * Adds a value item to a record
	 * 
	 * @param i The Item object containing the name/value pair
	 */
	public void addItem(Item i) {
		if (items == null)
			items = new ArrayList<Item>();
		items.add(i);
	}

	/**
	 * Returns the value Object for an item, null if the item does not exist.  (Be careful with NPE's, test
	 * for existence!).
	 * 
	 * @param name The name of the item
	 * @return The Value object
	 */
	public Object getItemValue(String name) {
		Item i = getFirstItem(name);
		Object ret = null;
		if (ret != null)
			ret = i.getValue();
		return ret;
	}

	/**
	 * Returns the previous value Object for an item, null if the item does not exist.  
	 * (Be careful with NPE's, test for existence!).
	 * 
	 * @param name The name of the item
	 * @return The Value object
	 */
	public Object getPreviousItemValue(String name) {
		Item i = getFirstItem(name);
		Object ret = null;
		if (ret != null)
			ret = i.getPreviousValue();
		return ret;
	}
	
	public String toString() {
		StringBuffer ret = new StringBuffer("external record: masterfile=");
		ret.append(this.masterFileName);
		for (Item i: this.items) {
			ret.append(' ');
			ret.append(i.toString());
		}
		return ret.toString();
	}

	@SuppressWarnings("deprecation")
	public void addMember(String item, String val) throws TerminologyException {
		try {
			I_TermFactory tf = LocalVersionedTerminology.get();
	        int nidUnspecifiedUuid = ArchitectonicAuxiliary.Concept.UNSPECIFIED_UUID.localize().getNid();
	        I_Path editIPath;
	        I_ConfigAceFrame config = tf.getActiveAceFrameConfig();
	        //TODO: Getting NPE
	        editIPath = tf.getPath(config.getClassifierInputPath().getUids());
	        int memberId = tf.uuidToNativeWithGeneration(UUID.randomUUID(), 
	        		nidUnspecifiedUuid, editIPath, nidVersion);
	        I_GetConceptData refset = this.creatingFactory.getInterpreter().
	        	getRefsetForItem(this.getMasterFileName(), item);
	        if (refset == null) {
	        	throw new TerminologyException("Cannot locate refset for item " + item);
	        }
	        else {
	        	// Add the core
	        	UUID uuidTypeString = UUID.fromString("4a5d2768-e2ae-3bc1-be2d-8d733cd4abdb");
	        	int nidTypeString = tf.getConcept(uuidTypeString).getConceptId();
	        	I_ThinExtByRefVersioned newExt = tf.newExtension(refset.getNid(), memberId, 
	        			this.getOwningConcept().getNid(),
	                nidTypeString);
	            tf.addUncommitted(newExt);
	            
	            // Add the part
	            I_ThinExtByRefPartString newExtPart = tf.newExtensionPart(I_ThinExtByRefPartString.class);

	            newExtPart.setPathId(nidEditPath);
	            newExtPart.setStatusId(nidCurrent);
	            newExtPart.setVersion(nidVersion);

	            newExtPart.setStringValue(val);
	            newExt.addVersion(newExtPart);

	            tf.addUncommitted(newExt);

	        }
	        
		}
		catch (IOException e) {
			throw new TerminologyException(e);
		}

	}
	/**
	 * Name/value pair object to describe data elements for an external system, such as Epic.
	 * 
	 * @author Steven Neiner
	 *
	 */
	public class Item {
		private String name;
		private Object value;
		private Object previousValue;
		private I_ThinExtByRefTuple sourceExtensionTuple;
		
		
		public Item(String name, Object value) {
			this.setName(name);
			this.setValue(value);
		}
		
		public Item(String name, Object value, Object previousValue) {
			this.setName(name);
			this.setValue(value);
			this.setPreviousValue(previousValue);
		}

		public Item(String name, Object value, Object previousValue, I_ThinExtByRefTuple extensionTuple) {
			this.setName(name);
			this.setValue(value);
			this.setPreviousValue(previousValue);
			this.sourceExtensionTuple = extensionTuple;
		}
		/**
		 * Returns the name of the data item.
		 * 
		 * @return String name
		 */
		public String getName() {
			return name;
		}
		
		/**
		 * Sets the name of the item
		 * 
		 * @param name
		 */
		public void setName(String name) {
			this.name = name;
		}
		
		/**
		 * Gets the value of an item
		 * 
		 * @return
		 */
		public Object getValue() {
			return value;
		}
		
		/**
		 * Sets the value of an item.
		 * 
		 * @param value
		 */
		public void setValue(Object value) {
			this.value = value;
		}

		/**
		 * Gets the previous value of an item
		 * 
		 * @return
		 */
		public Object getPreviousValue() {
			return previousValue;
		}

		/**
		 * Sets the previous value of an item.
		 * 
		 * @param value
		 */
		public void setPreviousValue(Object previousValue) {
			this.previousValue = previousValue;
		}
		
		
		public I_ThinExtByRefTuple getExtensionTuple() {
			return sourceExtensionTuple;
		}

		public void setExtensionTuple(I_ThinExtByRefTuple extensionTuple) {
			this.sourceExtensionTuple = extensionTuple;
		}

		public String toString() {
			return this.name.concat("=").concat(value.toString());
		}
		
		/**
		 * Will update the corresponding refset member with the supplied value.
		 * 
		 * @param val - The value to update to
		 * @throws Exception
		 */
		public void memberUpdate(String val) throws TerminologyException, Exception {
			I_ThinExtByRefPart part = sourceExtensionTuple.getMutablePart();
			if (I_ThinExtByRefPartString.class.isAssignableFrom(part.getClass()))
				memberUpdateString(val);
			else
				throw new TerminologyException("Data type mismatch: Item " + this.getName());
		}
		
		/**
		 * Will update the corresponding refset member with the supplied value.
		 * 
		 * @param val - The value to update to
		 * @throws Exception
		 */
		public void memberUpdate(int val) throws TerminologyException {
			I_ThinExtByRefPart part = sourceExtensionTuple.getMutablePart();
			if (I_ThinExtByRefPartInteger.class.isAssignableFrom(part.getClass()))
				memberUpdateInt(val);
			else
				throw new TerminologyException("Data type mismatch: Item " + this.getName());
		}

		/**
		 * Will update the corresponding refset member with the supplied value.
		 * 
		 * @param val - The value to update to
		 * @throws Exception
		 */
		public void memberUpdate(boolean val) throws TerminologyException {
			I_ThinExtByRefPart part = sourceExtensionTuple.getMutablePart();
			if (I_ThinExtByRefPartBoolean.class.isAssignableFrom(part.getClass()))
				memberUpdateBoolean(val);
			else
				throw new TerminologyException("Data type mismatch: Item " + this.getName());
		}

		/**
		 * Will update the corresponding String refset member with the supplied value.
		 * 
		 * @param val - The value to update to
		 * @throws Exception
		 */
		public void memberUpdateString(String val) throws TerminologyException, Exception {
			if (this.sourceExtensionTuple == null)
				throw new TerminologyException("Cannot update value; source extension tuple is null: Item " + this.getName());
	    	I_ThinExtByRefPart mutablePart = sourceExtensionTuple.getMutablePart();
	    	I_ThinExtByRefVersioned core = sourceExtensionTuple.getCore();
	    	I_ThinExtByRefPartString extPartStr = (I_ThinExtByRefPartString) mutablePart;
	    	if (!extPartStr.getStringValue().equalsIgnoreCase(val)) {
System.out.println("Updating item to: " + val); 		
		    	I_ThinExtByRefPart dupl = (I_ThinExtByRefPart) mutablePart.makeAnalog(mutablePart.getStatusId(), mutablePart.getPathId(), nidVersion);
	            I_ThinExtByRefPartString duplStr = (I_ThinExtByRefPartString) dupl;
	            duplStr.setStringValue(val);
	            core.addVersion(dupl);
	            LocalVersionedTerminology.get().addUncommitted(core);
	    	}
	    	// I_TermFactory tf = LocalVersionedTerminology.get();
	    	//tf.commitTransaction();
	    	// tf.commit();
		}


		/**
		 * Will update the corresponding integer refset member with the supplied value.
		 * 
		 * @param val - The value to update to
		 * @throws Exception
		 */
		public void memberUpdateInt(int val) throws TerminologyException {
			if (this.sourceExtensionTuple == null)
				throw new TerminologyException("Cannot update value - source extension tuple is null");
	    	I_ThinExtByRefPart mutablePart = sourceExtensionTuple.getMutablePart();
	    	I_ThinExtByRefVersioned core = sourceExtensionTuple.getCore();
	    	I_ThinExtByRefPartInteger extPartInt = (I_ThinExtByRefPartInteger) mutablePart;
	    	if (extPartInt.getIntValue() != val) {
		    	I_ThinExtByRefPart dupl = (I_ThinExtByRefPart) mutablePart.makeAnalog(mutablePart.getStatusId(), mutablePart.getPathId(), nidVersion);
		    	I_ThinExtByRefPartInteger duplInt = (I_ThinExtByRefPartInteger) dupl;
		    	duplInt.setIntValue(val);
	            core.addVersion(dupl);
	            LocalVersionedTerminology.get().addUncommitted(core);
	    	}
		}

		/**
		 * Will update the corresponding boolean refset member with the supplied value.
		 * 
		 * @param val - The value to update to
		 * @throws Exception
		 */
		public void memberUpdateBoolean(boolean val) throws TerminologyException {
			if (this.sourceExtensionTuple == null)
				throw new TerminologyException("Cannot update value - source extension tuple is null");
	    	I_ThinExtByRefPart mutablePart = sourceExtensionTuple.getMutablePart();
	    	I_ThinExtByRefVersioned core = sourceExtensionTuple.getCore();
	    	I_ThinExtByRefPartBoolean extPartBool = (I_ThinExtByRefPartBoolean) mutablePart;
	    	if (extPartBool.getBooleanValue() != val) {
		    	I_ThinExtByRefPart dupl = (I_ThinExtByRefPart) mutablePart.makeAnalog(mutablePart.getStatusId(), mutablePart.getPathId(), nidVersion);
		    	I_ThinExtByRefPartBoolean duplBool = (I_ThinExtByRefPartBoolean) dupl;
		    	duplBool.setBooleanValue(val);
	            core.addVersion(dupl);
	            LocalVersionedTerminology.get().addUncommitted(core);
	    	}
		}
		
		public void retireMember() throws TerminologyException {
			if (this.sourceExtensionTuple == null)
				throw new TerminologyException("Cannot retire member - source extension tuple is null");
			I_ThinExtByRefPart extPart = sourceExtensionTuple.getMutablePart();
			I_ThinExtByRefVersioned core = sourceExtensionTuple.getCore();
	        I_ThinExtByRefPart dupl = (I_ThinExtByRefPart) extPart.makeAnalog(nidRetired, extPart.getPathId(), 
	        		LocalVersionedTerminology.get().convertToThickVersion(nidVersion));
	        core.addVersion(dupl);
	        LocalVersionedTerminology.get().addUncommitted(core);

		}

	}
}
