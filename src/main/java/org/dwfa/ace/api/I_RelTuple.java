package org.dwfa.ace.api;

public interface I_RelTuple extends I_AmTypedTuple {

	public abstract int getC1Id();

	public abstract int getC2Id();

	public abstract int getRelId();

	public abstract int getCharacteristicId();

	public abstract int getGroup();

	public abstract int getRefinabilityId();

	/**
	 * @deprecated Use {@link #getTypeId()}
	 */
	@Deprecated
	public abstract int getRelTypeId();

	/**
	 * @deprecated Use {@link #setTypeId(int)}
	 */
	@Deprecated
	public abstract void setRelTypeId(Integer typeId);

	public abstract void setStatusId(Integer statusId);

	public abstract void setCharacteristicId(Integer characteristicId);

	public abstract void setRefinabilityId(Integer refinabilityId);

	public abstract void setGroup(Integer group);

	/**
	 * @deprecated Use {@link #duplicate()}
	 */
	@Deprecated
	public abstract I_RelPart duplicatePart();

	public I_RelPart duplicate();
	
	public abstract I_RelPart getPart();

	public abstract I_RelVersioned getRelVersioned();

	public abstract I_RelVersioned getFixedPart();

}