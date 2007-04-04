package org.dwfa.ace.api;

public interface I_ConceptAttributePart {

	public abstract int getPathId();

	public abstract void setPathId(int pathId);

	public abstract int getConceptStatus();

	public abstract void setConceptStatus(int conceptStatus);

	public abstract boolean isDefined();

	public abstract void setDefined(boolean defined);

	public abstract int getVersion();

	public abstract void setVersion(int version);

	public abstract I_ConceptAttributePart duplicate();

	public abstract boolean hasNewData(I_ConceptAttributePart another);

}