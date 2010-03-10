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

package org.ihtsdo.mojo.mojo.epicexport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.log.AceLog;

/**
 * A load file builder is a class that is used to determines which items will
 * appear in an export, and how they will appear, and in the case multiple export files
 * are needed for a single master file, which export file will be written to.
 * 
 * @author Steven Neiner
 * @param exportFactory - The factory to produce new writers
 * @param exportManager - The export manager
 */
public abstract class AbstractEpicExportBuilder {
	I_ExportFactory exportFactory;
	EpicExportManager exportManager;
	protected String masterfile;
	I_GetConceptData parentConcept;
	List<EpicItem> epicItems = new ArrayList<EpicItem>();
	private I_EpicExportRecordWriter writer;
	private String[] exportIfTheseItemsChanged;
	private boolean hasErrors = false;
	List<String> errors = new ArrayList<String>();
	private String[] mandatoryItems;
	private String[] alwaysWriteTheseItemsForNewRecord;
	private String[] alwaysWriteTheseItemsForExistingRecord;
	private String[] itemsToWriteIfChanged;

	
	public AbstractEpicExportBuilder(I_ExportFactory exportFactory, EpicExportManager em) {
		this.exportFactory = exportFactory;
		this.exportManager = em;
	}
	
	public I_ExportFactory getExportFactory() {
		return exportFactory;
	}

	public void setExportFactory(I_ExportFactory ef) {
		this.exportFactory = ef;
	}

	public I_EpicExportRecordWriter getWriter() {
		return writer;
	}

	public void setWriter(I_EpicExportRecordWriter writer) {
		this.writer = writer;
	}

	public I_GetConceptData getParentConcept() {
		return parentConcept;
	}

	public void setIdConcept(I_GetConceptData parentConcept) {
		this.parentConcept = parentConcept;
	}
	
	public String getParentUUID() throws IOException {
		String ret = null;
		for (UUID u: this.parentConcept.getUids()) {
			ret = u.toString();
		}
		return ret;
	}

	/**
	 * Populates an Epic item number value pair for export into loadfiles
	 * 
	 * @param refsetId - The field number from the refest which identifies what the
	 * element is
	 * @param value - The Object that contains the value
	 * @param previousValue - The object that contains the previous value
	 */
	public void addItemForExport(String item, Object value, Object previousValue) {
		epicItems.add(new EpicItem(item, value, previousValue));
	}
	
	/** Prepares the writer for receiving values for a record; initializes the
	 * buffer of values.
	 */
	public void clearRecordContents() {
		epicItems = new ArrayList<EpicItem>();
		errors = new ArrayList<String>();
		this.hasErrors = false;
	}
		
	/** 
	 * Abstract method to determine which items export and how.
	 * 
	 * @param version - Passed to determine which load file to write to
	 * @throws Exception
	 */
	public abstract void writeRecord(String version, List<String> regions) throws Exception;
	
	/**
	 * Writes an item to the export target
	 * 
	 * @param @param epicItemNumber - The item number of the value to write to the load file
	 * @param aliasItemNumber - The item number to use in the load file
	 */
	public void writeItem(String epicItemNumber, String aliasItemNumber) {
		for (Iterator<EpicItem> i = epicItems.iterator(); i.hasNext();) {
			EpicItem ei = (EpicItem) i.next();
			if (ei.itemNumber.equalsIgnoreCase(epicItemNumber))
				writer.addItemValue(aliasItemNumber, ei.value);
		}
	}
	
	/** 
	 * Writes all items to the export
	 */
	public void writeAll() {
		for (Iterator<EpicItem> i = epicItems.iterator(); i.hasNext();) {
			EpicItem ei = (EpicItem) i.next();
			writer.addItemValue(ei.itemNumber, ei.value);
		}
	}

	/** 
	 * Plural of writeItem, writes a list of items to the export target
	 * 
	 * @param items
	 */
	public void writeItems(String... items) {
		for (String i: items)
			writeItem(i, i);
	}
	/**
	 * Writes an item to the export target
	 * 
	 * @param epicItemNumber - The item number to write to the load file
	 */
	public void writeItem(String epicItemNumber) {
		writeItem(epicItemNumber, epicItemNumber);
	}
	
	/**
	 * Returns the  master file that this builder is for
	 * 
	 * @return
	 */
	public String getMasterfile() {
		return masterfile;
	}

	public void setMasterfile(String masterfile) {
		this.masterfile = masterfile;
	}

	public EpicExportManager getExportManager() {
		return exportManager;
	}

	/**
	 * If an item has no value set, will populate the the file with the value of another
	 * item.  If the value of epicItemNumber is set, will populate with that item.
	 * 
	 * @param epicItemNumber - The item number to check for if populated. I
	 * @param defaultItem - If not populated, will use this item number to populate.
	 */
	public void writeItemWithDefault(String epicItemNumber, String defaultItem) {
		if (getFirstItem(epicItemNumber) == null) {
			EpicItem d = getFirstItem(defaultItem);
			if (d != null)
				writer.addItemValue(epicItemNumber, d.value.toString());
		}
		else {
			writeItem(epicItemNumber);
		}
	}

	/**
	 * If an item has changed value since the last version, will populate the load file,
	 * otherwise will leave it out of the load file.
	 * 
	 * @param epicItemNumber - The item number of the value to populate
	 * @param aliasItemNumber - The item number that will appear in the load file.
	 */
	public void writeItemIfChanged(String epicItemNumber, String aliasItemNumber) {
		if (itemHasChanges(epicItemNumber)) {
			writeItem(epicItemNumber, aliasItemNumber);
		}
	}
	
	/**
	 * Plural of writeItemIfChanged, If an item has changed value since the last version, 
	 * will populate the load file, otherwise will leave it out of the load file.
	 * 
	 * @param items List of items to write
	 */
	public void writeItemsIfChanged(String... items) {
		for (String i: items)
			writeItemIfChanged(i);
	}
	
	/**
	 * If an item has changed value since the last version, will populate the load file,
	 * otherwise will leave it out of the load file.
	 * 
	 * @param epicItemNumber - The item number of the value to populate
	 */
	public void writeItemIfChanged(String epicItemNumber) {
		writeItemIfChanged(epicItemNumber, epicItemNumber);
	}
	
	/**
	 * Write an item number / literal value combination to the load file
	 * 
	 * @param epicItemNumber
	 * @param literal
	 */
	public void writeLiteralItem(String epicItemNumber, String literal) {
		writer.addItemValue(epicItemNumber, literal);
	}
	
	/**
	 * Writes a literal line of text to the output
	 * 
	 * @param text
	 * @throws IOException
	 */
	public void writeLine(String text) throws IOException {
		writer.writeLine(text);
	}
	
	/**
	 * Determines whether an item has any changes between the current value
	 * and previous values
	 * 
	 * @param epicItemNumber - The item number to check for changes
	 * @return - true if an item has any changes
	 */
	public boolean itemHasChanges(String epicItemNumber) {
		boolean ret = false;
		for (Iterator<EpicItem> i = epicItems.iterator(); i.hasNext();) {
			EpicItem ei = (EpicItem) i.next();
			if (ei.itemNumber.equalsIgnoreCase(epicItemNumber) ) {
				ret = ret || ei.hasChanges();
				if (ret)
					break;
			}
		}
		return ret;
	}
	

	/**
	 * Looks for the first occurrence of the item in the exported items cache.
	 * 
	 * @param epicItemNumber
	 * @return the first item found
	 */
	public EpicItem getFirstItem(String epicItemNumber) {
		EpicItem ret = null;
		
		for (Iterator<EpicItem> i = epicItems.iterator(); i.hasNext();) {
			EpicItem ei = (EpicItem) i.next();
			if (ei.itemNumber.equalsIgnoreCase(epicItemNumber) ) {
				ret = ei;
				break;
			}
		}
		return ret;
	}
	/**
	 * Looks for the first occurrence of the item in the exported items cache, and returns true if found.
	 * 
	 * @param epicItemNumber
	 * @return true if found
	 */
	public boolean hasItem(String epicItemNumber) {
		boolean ret = false;
		
		for (EpicItem i: epicItems) {
			if (i.itemNumber.equalsIgnoreCase(epicItemNumber) ) {
				ret = true;
				break;
			}
		}
		return ret;
	}

	/**
	 * exportIfTheseItemsChanged is a String list of items that if any of them have changes, will
	 * indicate the record has changes. See isChangedRecord().
	 * 
	 * @param exportIfTheseItemsChanged
	 */
	public void setExportIfTheseItemsChanged(String... exportIfTheseItemsChanged) {
		this.exportIfTheseItemsChanged = exportIfTheseItemsChanged;
	}

	
	public String[] getExportIfTheseItemsChanged() {
		return exportIfTheseItemsChanged;
	}

	/**
	 * Returns true if there are any items with supplied name
	 * 
	 * @param epicItemNumber
	 * @return
	 */
	public boolean itemIsPopulated(String epicItemNumber) {
		return !(getFirstItem(epicItemNumber) == null);
	}
	
	/**
	 * Returns true if all items in the list have values.  If any item in the list is not present,
	 * will add an error.  Used for validation.  Will also add an error for missing items.
	 * 
	 * @param epicItemNumber
	 * @return boolean true if all populated
	 */	
	public boolean allItemsArePopulated(String[] itemList) {
		boolean ret = true;
		for (String i: itemList) {
			boolean found = this.itemIsPopulated(i);
			ret = ret && found;
			if (!found)
				this.addError("Missing item " + i);
		}
		return ret;
	}

	/**
	 * Returns true if any item in the list has a change, meaning a current value differing from the previous value
	 * 
	 * @param itemList - List of item names to look for changes
	 * @return boolean - true if there are any changes
	 */
	public boolean anyItemsHaveChanges(String[] itemList) {
		boolean ret = false;
		for (String i: itemList) {
			ret = ret || this.itemHasChanges(i);
			if (ret)
				break;
		}
		return ret;
	}
	
	/**
	 * Returns true if any item has changes, meaning a current value differing from the previous value
	 * 
	 * @return boolean - true if there are any changes
	 */
	public boolean anyItemsHaveChanges() {
		boolean ret = false;
		for (EpicItem e: this.epicItems) {
			ret = ret || e.hasChanges();
		}
		return ret;
	}
	
	/**
	 * Returns true if any item in the list exportIfTheseItemsChanged 
	 * has changes, meaning a current value differing from the previous value
	 * 
	 * @return boolean - true if there are any changes
	 */
	public boolean isChangedRecord() {
		return this.anyItemsHaveChanges(this.exportIfTheseItemsChanged);
	}
	
	/**
	 * Adds an error message to this builder for later retrieval.
	 * 
	 * @param message
	 */
	public void addError(String message) {
		this.hasErrors = true;
		this.errors.add(message);
		AceLog.getAppLog().warning(message + ": " + toString());
	}
	
	/**
	 * Will add an error if <i>condition</i> is true.
	 * 
	 * @param condition - boolean condition
	 * @param message - the error message to add if ture
	 */
	public void addErrorIfTrue(boolean condition, String message) {
		if (condition)
			addError(message);
	}
	
	/**
	 * Gets all errors for this builder
	 * @return List<String> of error messages
	 */
	public List<String> getErrors() {
		return this.errors;
	}
	
	/**
	 * Writes any errors to this builders writer.
	 *  
	 * @throws IOException
	 */
	public void writeAnyErrors() throws IOException {
		for (String e: this.errors) {
			this.writer.writeLine("ERROR: " + e);
		}
	}
	
	/**
	 * Returns true if there were any error messages added in addError.
	 * @return true if there are errors
	 */
	public boolean hasErrors() {
		return this.hasErrors;
	}

	/**
	 * List of items to test for complete record 
	 * 
	 * @return
	 */
	public String[] getMandatoryItems() {
		return mandatoryItems;
	}

	public void setMandatoryItems(String... mandatoryItems) {
		this.mandatoryItems = mandatoryItems;
	}

	/**
	 * List of items to write for new record
	 * 
	 * @return
	 */
	public String[] getAlwaysWriteTheseItemsForNewRecord() {
		return alwaysWriteTheseItemsForNewRecord;
	}

	public void setAlwaysWriteTheseItemsForNewRecord(
			String... alwaysWriteTheseItemsForNewRecord) {
		this.alwaysWriteTheseItemsForNewRecord = alwaysWriteTheseItemsForNewRecord;
	}

	/**
	 * List of items to write for existing record
	 * 
	 * @return
	 */
	public String[] getAlwaysWriteTheseItemsForExistingRecord() {
		return alwaysWriteTheseItemsForExistingRecord;
	}

	public void setAlwaysWriteTheseItemsForExistingRecord(
			String... alwaysWriteTheseItemsForExistingRecord) {
		this.alwaysWriteTheseItemsForExistingRecord = alwaysWriteTheseItemsForExistingRecord;
	}

	/**
	 * List of items to write if their values have changed
	 * 
	 * @return
	 */
	public String[] getItemsToWriteIfChanged() {
		return itemsToWriteIfChanged;
	}

	public void setItemsToWriteIfChanged(String... itemsToWriteIfChanged) {
		this.itemsToWriteIfChanged = itemsToWriteIfChanged;
	}
	

	
	/**
	 * Returns true if there is at least one change in the supplied list, and no changes in items outside of the
	 * supplied list.
	 * 
	 * @param itemList
	 * @return
	 */
	public boolean onlyHasChangesIn(String[] itemList) {
		boolean ret = anyItemsHaveChanges(itemList);
		List<String> checkFor = new ArrayList<String>();
		for (String i: this.exportIfTheseItemsChanged) {
			boolean doAdd = true;
			for (String j: itemList) {
				if (j.equals(i))
					doAdd = false;
			}
			if (doAdd)
				checkFor.add(i);
		}
		ret = ret && !anyItemsHaveChanges(checkFor.toArray(new String[0]));
		return ret;
	}

	public String stringArrayToList(List <String> a) {
		return stringArrayToList(a, ",");
	}
	
	public String stringArrayToList(List <String> a, String seperator) {
    	StringBuffer ret = new StringBuffer();
    	int i = 0;
    	for (String s: a) {
    		if (++i > 1)
    			ret.append(seperator);
    		ret.append(s);
    	}
    	return ret.toString();
	}
	
	public String toString() {
		StringBuffer ret = new StringBuffer();
		EpicItem dot11 = getFirstItem("11");
		if (dot11 != null) {
			ret.append(" CID(11): ");
			ret.append(dot11.value);
		}
		EpicItem dot2 = getFirstItem("2");
		if (dot2 != null) {
			ret.append(" Display Name(2): ");
			ret.append(dot2.value);
		}
		return ret.toString();
	}

	public class EpicItem {
		String itemNumber;
		Object value;
		Object previousValue;
		
		public EpicItem(String itemNumber, Object value, Object previousValue) {
			this.itemNumber = itemNumber;
			this.value = value;
			this.previousValue = previousValue;
		}
		
		public Object getValue() {
			return value;
		}

		public Object getPreviousValue() {
			return previousValue;
		}

		/**
		 * Returns true if there is a difference between the previous value and the
		 * current value.
		 * 
		 * @return true if there is a difference
		 */
		public boolean hasChanges() {
			if (previousValue == null && value == null)
				return false;
			else if (previousValue == null && value != null)
				return true;
			else if (previousValue != null && value == null)
				return true;
			else
				return !value.equals(previousValue);
		}
		
	}
	
}
