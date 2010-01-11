package org.dwfa.mojo.epicexport;

import java.util.ArrayList;
import java.util.List;

/**
 * Class used to describe a record of an external system that data is exported to.
 * 
 * @author Steven Neiner
 *
 */
public class ExternalTermRecord {
	private String masterFileName;
	private String version;
	private List<String> regions;
	private List<Item> items;
	
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
	 * Returns the previopus value Object for an item, null if the item does not exist.  
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
		
		public Item(String name, Object value) {
			this.setName(name);
			this.setValue(value);
		}
		
		public Item(String name, Object value, Object previousValue) {
			this.setName(name);
			this.setValue(value);
			this.setPreviousValue(previousValue);
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
		
		public String toString() {
			return this.name.concat("=").concat(value.toString());
		}
		
	}
}
