package org.dwfa.ace.api;


public interface I_RelPart {

	public abstract boolean hasNewData(I_RelPart another);

	public abstract int getPathId();

	public abstract void setPathId(int pathId);

	public abstract int getCharacteristicId();

	public abstract void setCharacteristicId(int characteristicId);

	public abstract int getGroup();

	public abstract void setGroup(int group);

	public abstract int getRefinabilityId();

	public abstract void setRefinabilityId(int refinabilityId);

	public abstract int getRelTypeId();

	public abstract void setRelTypeId(int relTypeId);

	public abstract int getVersion();

	public abstract void setVersion(int version);

	public abstract int getStatusId();

	public abstract void setStatusId(int statusId);

	public abstract void convertIds(I_MapNativeToNative jarToDbNativeMap);

	public abstract I_RelPart duplicate();

}