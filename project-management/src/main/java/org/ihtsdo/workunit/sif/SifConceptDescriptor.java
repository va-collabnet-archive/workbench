package org.ihtsdo.workunit.sif;

public class SifConceptDescriptor {
	
	private SifIdentifier identifier;
	private String description;
	private String abbrev;

	public SifConceptDescriptor() {
		// TODO Auto-generated constructor stub
	}
	
	public SifConceptDescriptor(SifIdentifier identifier, String description) {
		super();
		this.identifier = identifier;
		this.description = description;
	}

	/**
	 * @return the identifier
	 */
	public SifIdentifier getIdentifier() {
		return identifier;
	}

	/**
	 * @param identifier the identifier to set
	 */
	public void setIdentifier(SifIdentifier identifier) {
		this.identifier = identifier;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the abbrev
	 */
	public String getAbbrev() {
		return abbrev;
	}

	/**
	 * @param abbrev the abbrev to set
	 */
	public void setAbbrev(String abbrev) {
		this.abbrev = abbrev;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((abbrev == null) ? 0 : abbrev.hashCode());
		result = prime * result
				+ ((description == null) ? 0 : description.hashCode());
		result = prime * result
				+ ((identifier == null) ? 0 : identifier.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SifConceptDescriptor other = (SifConceptDescriptor) obj;
		if (abbrev == null) {
			if (other.abbrev != null)
				return false;
		} else if (!abbrev.equals(other.abbrev))
			return false;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		if (identifier == null) {
			if (other.identifier != null)
				return false;
		} else if (!identifier.equals(other.identifier))
			return false;
		return true;
	}

}
