package org.ihtsdo.workunit.sif;


public class SifRefsetMemberSimpleType extends SifTerminologyComponent {
	
	private SifIdentifier refsetId;
	private SifIdentifier referencedComponentId;
	
	public SifRefsetMemberSimpleType() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @return the refsetId
	 */
	public SifIdentifier getRefsetId() {
		return refsetId;
	}

	/**
	 * @param refsetId the refsetId to set
	 */
	public void setRefsetId(SifIdentifier refsetId) {
		this.refsetId = refsetId;
	}

	/**
	 * @return the referencedComponentId
	 */
	public SifIdentifier getReferencedComponentId() {
		return referencedComponentId;
	}

	/**
	 * @param referencedComponentId the referencedComponentId to set
	 */
	public void setReferencedComponentId(SifIdentifier referencedComponentId) {
		this.referencedComponentId = referencedComponentId;
	}

}
