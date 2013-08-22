package org.ihtsdo.workunit.sif;

import java.util.UUID;

public class SifIdentifier {
	
	private UUID identifierSchemeId;
	private String identifier;

	public SifIdentifier() {
		// TODO Auto-generated constructor stub
	}

	public SifIdentifier(UUID identifierSchemeId, String identifier) {
		super();
		this.identifierSchemeId = identifierSchemeId;
		this.identifier = identifier;
	}

	/**
	 * @return the identifierSchemeId
	 */
	public UUID getIdentifierSchemeId() {
		return identifierSchemeId;
	}

	/**
	 * @param identifierSchemeId the identifierSchemeId to set
	 */
	public void setIdentifierSchemeId(UUID identifierSchemeId) {
		this.identifierSchemeId = identifierSchemeId;
	}

	/**
	 * @return the identifier
	 */
	public String getIdentifier() {
		return identifier;
	}

	/**
	 * @param identifier the identifier to set
	 */
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((identifier == null) ? 0 : identifier.hashCode());
		result = prime
				* result
				+ ((identifierSchemeId == null) ? 0 : identifierSchemeId
						.hashCode());
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
		SifIdentifier other = (SifIdentifier) obj;
		if (identifier == null) {
			if (other.identifier != null)
				return false;
		} else if (!identifier.equals(other.identifier))
			return false;
		if (identifierSchemeId == null) {
			if (other.identifierSchemeId != null)
				return false;
		} else if (!identifierSchemeId.equals(other.identifierSchemeId))
			return false;
		return true;
	}

}
