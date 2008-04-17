package org.dwfa.ace.api;


public interface I_ImagePart extends I_AmTypedPart {

	public void setPathId(int pathId);

	public void setStatusId(int status);

	public void setVersion(int version);

	public String getTextDescription();

	public void setTextDescription(String name);

	public void setTypeId(int type);

	public boolean hasNewData(I_ImagePart another);

	public void convertIds(I_MapNativeToNative jarToDbNativeMap);
	
	public I_ImagePart duplicate();

}