package org.dwfa.ace.api;


public interface I_RelPart {

	public boolean hasNewData(I_RelPart another);

	public int getPathId();

	public void setPathId(int pathId);

	public int getCharacteristicId();

	public void setCharacteristicId(int characteristicId);

	public int getGroup();

	public void setGroup(int group);

	public int getRefinabilityId();

	public void setRefinabilityId(int refinabilityId);

	public int getRelTypeId();

	public void setRelTypeId(int relTypeId);

	public int getVersion();

	public void setVersion(int version);

	public int getStatusId();

	public void setStatusId(int statusId);

	public void convertIds(I_MapNativeToNative jarToDbNativeMap);

	public I_RelPart duplicate();

}