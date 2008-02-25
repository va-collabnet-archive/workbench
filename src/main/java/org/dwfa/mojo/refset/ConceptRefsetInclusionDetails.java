package org.dwfa.mojo.refset;

public class ConceptRefsetInclusionDetails {

	private int conceptId;
	private int inclusionTypeId;
	private int inclusionReasonId;
	public int getConceptId() {
		return conceptId;
	}
	public void setConceptId(int conceptId) {
		this.conceptId = conceptId;
	}
	public int getInclusionTypeId() {
		return inclusionTypeId;
	}
	public void setInclusionTypeId(int inclusionTypeId) {
		this.inclusionTypeId = inclusionTypeId;
	}
	public int getInclusionReasonId() {
		return inclusionReasonId;
	}
	public void setInclusionReasonId(int inclusionReasonId) {
		this.inclusionReasonId = inclusionReasonId;
	}
	public ConceptRefsetInclusionDetails(int conceptId, int inclusionTypeId,
			int inclusionReasonId) {
		super();
		this.conceptId = conceptId;
		this.inclusionTypeId = inclusionTypeId;
		this.inclusionReasonId = inclusionReasonId;
		
	}
	
	@Override
	public String toString() {
		return ""+conceptId;
	}
	
	@Override
	public int hashCode() {
		return conceptId;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj==null) {
			return false;
		}
		if (!(obj instanceof ConceptRefsetInclusionDetails)) {
			return false;
		} else {
			ConceptRefsetInclusionDetails c = (ConceptRefsetInclusionDetails) obj;
			if (c.getConceptId()==conceptId) {
				return true;
			} else {
				return false;
			}
		}
	}
		
}
