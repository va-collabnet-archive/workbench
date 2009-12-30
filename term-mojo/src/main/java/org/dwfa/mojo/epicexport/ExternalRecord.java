package org.dwfa.mojo.epicexport;

import java.util.ArrayList;
import java.util.List;

public class ExternalRecord {
	private String masterFileName;
	List<Item> items;
	
	public ExternalRecord(String name) {
		this.setMasterFileName(name);
	}
	
	public String getMasterFileName() {
		return masterFileName;
	}

	public void setMasterFileName(String masterFileName) {
		this.masterFileName = masterFileName;
	}
	
	public Item getFirstItem(String name) {
		Item ret = null;
		for (Item i: this.items) {
			if (i.getName().equalsIgnoreCase(name))
				return i;
		}	
		return ret;
	}
	
	public void addItem(String name, Object value) {
		if (items == null)
			items = new ArrayList<Item>();
		items.add(new Item(name, value));
	}
	
	public void addItem(String name, Object value, Object previousValue) {
		if (items == null)
			items = new ArrayList<Item>();
		items.add(new Item(name, value, previousValue));
	}

	public Object getItemValue(String name) {
		Item i = getFirstItem(name);
		Object ret = null;
		if (ret != null)
			ret = i.getValue();
		return ret;
	}

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

		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public Object getValue() {
			return value;
		}
		public void setValue(Object value) {
			this.value = value;
		}
		public Object getPreviousValue() {
			return previousValue;
		}
		public void setPreviousValue(Object previousValue) {
			this.previousValue = previousValue;
		}
		
		public String toString() {
			return this.name.concat("=").concat(value.toString());
		}
		
	}
}
