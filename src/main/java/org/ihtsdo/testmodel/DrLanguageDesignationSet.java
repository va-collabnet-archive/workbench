package org.ihtsdo.testmodel;

import java.util.HashSet;
import java.util.Set;

public class DrLanguageDesignationSet {
	
	Set<DrDescription> descriptions;
	int size;
	int preferredTermOccurrence;
	int preferredFsnOccurrence;
	boolean hasDuplicates; 
	String languageRefsetUuid;
	
	public DrLanguageDesignationSet(){
		descriptions=new HashSet<DrDescription>();
	}

	public Set<DrDescription> getDescriptions() {
		return descriptions;
	}

	public void setDescriptions(Set<DrDescription> descriptions) {
		this.descriptions = descriptions;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public int getPreferredTermOccurrence() {
		return preferredTermOccurrence;
	}

	public void setPreferredTermOccurrence(int preferredTermOccurrence) {
		this.preferredTermOccurrence = preferredTermOccurrence;
	}

	public int getPreferredFsnOccurrence() {
		return preferredFsnOccurrence;
	}

	public void setPreferredFsnOccurrence(int preferredFsnOccurrence) {
		this.preferredFsnOccurrence = preferredFsnOccurrence;
	}

	public boolean isHasDuplicates() {
		return hasDuplicates;
	}

	public void setHasDuplicates(boolean hasDuplicates) {
		this.hasDuplicates = hasDuplicates;
	}

	public String getLanguageRefsetUuid() {
		return languageRefsetUuid;
	}

	public void setLanguageRefsetUuid(String languageRefsetUuid) {
		this.languageRefsetUuid = languageRefsetUuid;
	}
}
