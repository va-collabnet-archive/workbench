package org.dwfa.ace.api;

public interface I_IdPart {

	public abstract int getPathId();

	public abstract void setPathId(int pathId);

	public abstract int getIdStatus();

	public abstract void setIdStatus(int idStatus);

	public abstract int getSource();

	public abstract void setSource(int source);

	public abstract Object getSourceId();

	public abstract void setSourceId(Object sourceId);

	public abstract int getVersion();

	public abstract void setVersion(int version);

	public abstract boolean hasNewData(I_IdPart another);

}