package org.dwfa.ace.api;


public interface I_RelPart extends I_AmTypedPart {

	public boolean hasNewData(I_RelPart another);

	public void setPathId(int pathId);

	public int getCharacteristicId();

	public void setCharacteristicId(int characteristicId);

	public int getGroup();

	public void setGroup(int group);

	public int getRefinabilityId();

	public void setRefinabilityId(int refinabilityId);

	public int getRelTypeId();

	public void setRelTypeId(int relTypeId);

	public void setVersion(int version);

	public void setStatusId(int statusId);

	public void convertIds(I_MapNativeToNative jarToDbNativeMap);

	public I_RelPart duplicate();

}