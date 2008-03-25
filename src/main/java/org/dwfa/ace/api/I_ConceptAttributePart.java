package org.dwfa.ace.api;

public interface I_ConceptAttributePart {

	public int getPathId();

	public void setPathId(int pathId);

	public int getConceptStatus();

	public void setConceptStatus(int conceptStatus);

	public boolean isDefined();

	public void setDefined(boolean defined);

	public int getVersion();

	public void setVersion(int version);

	public I_ConceptAttributePart duplicate();

	public boolean hasNewData(I_ConceptAttributePart another);

}