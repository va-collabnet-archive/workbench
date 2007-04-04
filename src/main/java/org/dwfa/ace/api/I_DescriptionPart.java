package org.dwfa.ace.api;


public interface I_DescriptionPart {

	public abstract boolean hasNewData(I_DescriptionPart another);

	public abstract int getPathId();

	public abstract void setPathId(int pathId);

	public abstract boolean getInitialCaseSignificant();

	public abstract void setInitialCaseSignificant(boolean capStatus);

	public abstract String getLang();

	public abstract void setLang(String lang);

	public abstract int getStatusId();

	public abstract void setStatusId(int status);

	public abstract String getText();

	public abstract void setText(String text);

	public abstract int getTypeId();

	public abstract void setTypeId(int typeInt);

	public abstract int getVersion();

	public abstract void setVersion(int version);

	public abstract void convertIds(I_MapNativeToNative jarToDbNativeMap);

	public abstract I_DescriptionPart duplicate();

}