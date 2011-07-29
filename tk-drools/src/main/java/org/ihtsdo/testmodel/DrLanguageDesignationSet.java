package org.ihtsdo.testmodel;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;

public class DrLanguageDesignationSet {

	Set<DrDescription> descriptions;
	String languageRefsetUuid;

	// Inferred properties
	int size = 0;
	int preferredTermOccurrence = 0;
	int preferredFsnOccurrence = 0;
	boolean hasDuplicates = false;

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer("");
		try {
			try {
				ConceptChronicleBI languageRefset = Ts.get().getConcept(UUID.fromString(languageRefsetUuid));
				sb.append("Language Refset: " + languageRefset + " (" + languageRefsetUuid + "),");
			} catch (IllegalArgumentException ex) {
			}

			sb.append(" Size: " + size + ",");
			sb.append(" PreferredTermOccurrence: " + preferredTermOccurrence + ",");
			sb.append(" PreferredFsnOccurrence: " + preferredFsnOccurrence + ",");
			sb.append(" HasDuplicates: " + hasDuplicates);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return sb.toString();
	}

	public DrLanguageDesignationSet() {
		descriptions = new HashSet<DrDescription>();
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
