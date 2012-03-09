package org.ihtsdo.rf2.identifier.impl;

interface I_Tuple {

	public abstract String getRelId();
	public abstract String getGroupId();
	public abstract void setGroupId(String groupId);
	public abstract String getGroupNumber();
	public abstract String writeTuple();
	public abstract String getTypeC2();
	public abstract String getStatus();
	public abstract String getEffTime();
	public abstract String getLineWOId();

}