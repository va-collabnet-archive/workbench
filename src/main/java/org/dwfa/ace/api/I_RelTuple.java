package org.dwfa.ace.api;

public interface I_RelTuple {

	public abstract int getC1Id();

	public abstract int getC2Id();

	public abstract int getRelId();

	public abstract int getPathId();

	public abstract int getCharacteristicId();

	public abstract int getGroup();

	public abstract int getRefinabilityId();

	public abstract int getRelTypeId();

	public abstract int getStatusId();

	public abstract int getVersion();

	public abstract void setRelTypeId(Integer typeId);

	public abstract void setStatusId(Integer statusId);

	public abstract void setCharacteristicId(Integer characteristicId);

	public abstract void setRefinabilityId(Integer refinabilityId);

	public abstract void setGroup(Integer group);

	public abstract I_RelPart duplicatePart();

	public abstract I_RelVersioned getRelVersioned();

	public abstract I_RelVersioned getFixedPart();

}