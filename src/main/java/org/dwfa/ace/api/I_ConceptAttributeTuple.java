package org.dwfa.ace.api;


public interface I_ConceptAttributeTuple {

	public abstract int getConId();

	public abstract int getConceptStatus();

	public abstract int getPathId();

	public abstract int getVersion();

	public abstract boolean hasNewData(I_ConceptAttributePart another);

	public abstract boolean isDefined();

	public abstract void setStatusId(Integer statusId);

	public abstract void setDefined(boolean defined);

	public abstract I_ConceptAttributeVersioned getConVersioned();

	public abstract I_ConceptAttributePart duplicatePart();

}