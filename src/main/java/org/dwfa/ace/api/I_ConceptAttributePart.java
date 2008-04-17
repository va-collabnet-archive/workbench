package org.dwfa.ace.api;

public interface I_ConceptAttributePart extends I_AmPart {

	public void setPathId(int pathId);

	public int getConceptStatus();

	public void setConceptStatus(int conceptStatus);

	public boolean isDefined();

	public void setDefined(boolean defined);

	public void setVersion(int version);

	public I_ConceptAttributePart duplicate();

	public boolean hasNewData(I_ConceptAttributePart another);

}