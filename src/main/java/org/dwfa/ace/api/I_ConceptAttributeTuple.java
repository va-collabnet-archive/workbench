package org.dwfa.ace.api;


public interface I_ConceptAttributeTuple extends I_AmPart {

	public abstract int getConId();

	public abstract int getConceptStatus();

	public abstract boolean hasNewData(I_ConceptAttributePart another);

	public abstract boolean isDefined();

	public abstract void setDefined(boolean defined);

	public abstract I_ConceptAttributeVersioned getConVersioned();

	/**
	 * @deprecated Use {@link #duplicate()}
	 */
	@Deprecated
	public abstract I_ConceptAttributePart duplicatePart();
	
	public I_ConceptAttributePart duplicate();
	
   public abstract I_ConceptAttributePart getPart();

}