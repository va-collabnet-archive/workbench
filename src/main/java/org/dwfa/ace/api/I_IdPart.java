package org.dwfa.ace.api;

public interface I_IdPart {

	public int getPathId();

	public void setPathId(int pathId);

	public int getIdStatus();

	public void setIdStatus(int idStatus);

	public int getSource();

	public void setSource(int source);

	public Object getSourceId();

	public void setSourceId(Object sourceId);

	public int getVersion();

	public void setVersion(int version);

	public boolean hasNewData(I_IdPart another);
	
	public I_IdPart duplicate();

}