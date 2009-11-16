package org.dwfa.ace.api;


public interface I_RelPart extends I_AmTypedPart {

	public int getCharacteristicId();

	public void setCharacteristicId(int characteristicId);

	public int getGroup();

	public void setGroup(int group);

	public int getRefinabilityId();

	public void setRefinabilityId(int refinabilityId);

	/**
	 * @deprecated Use {@link #getTypeId()}
	 */
	@Deprecated
	public int getRelTypeId();

	/**
	 * @deprecated Use {@link #setTypeId(int)}
	 */
	@Deprecated
	public void setRelTypeId(int relTypeId);

	public I_RelPart duplicate();

}