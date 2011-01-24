package org.ihtsdo.translation.ui.config;

public class ConfigTreeInfo {
	private String name;
	private String item;

	public ConfigTreeInfo(String name, String item) {
		super();
		this.name = name;
		this.item = item;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getItem() {
		return item;
	}

	public void setItem(String item) {
		this.item = item;
	}
	
	public String toString(){
		return this.name;
	}

}
