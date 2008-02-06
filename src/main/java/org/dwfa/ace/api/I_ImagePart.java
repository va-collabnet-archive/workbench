package org.dwfa.ace.api;


public interface I_ImagePart {

	public int getPathId();

	public int getStatusId();

	public int getVersion();

	public void setPathId(int pathId);

	public void setStatusId(int status);

	public void setVersion(int version);

	public String getTextDescription();

	public void setTextDescription(String name);

	public int getTypeId();

	public void setTypeId(int type);

	public boolean hasNewData(I_ImagePart another);

	public void convertIds(I_MapNativeToNative jarToDbNativeMap);
	

}