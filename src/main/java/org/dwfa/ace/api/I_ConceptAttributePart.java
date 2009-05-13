package org.dwfa.ace.api;

public interface I_ConceptAttributePart extends I_AmPart {

	/**
	 * @deprecated Use {@link #getStatusId}
	 */
	@Deprecated
	public int getConceptStatus();

	/**
	 * @deprecated Use {@link #setStatusId(int)}
	 */ 
	@Deprecated
	public void setConceptStatus(int conceptStatus);

	public boolean isDefined();

	public void setDefined(boolean defined);

	public I_ConceptAttributePart duplicate();

	public boolean hasNewData(I_ConceptAttributePart another);

}