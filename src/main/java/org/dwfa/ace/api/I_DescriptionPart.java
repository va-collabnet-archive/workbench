package org.dwfa.ace.api;


public interface I_DescriptionPart {

	public boolean hasNewData(I_DescriptionPart another);

	public int getPathId();

	public void setPathId(int pathId);

	public boolean getInitialCaseSignificant();

	public void setInitialCaseSignificant(boolean capStatus);

	public String getLang();

	public void setLang(String lang);

	public int getStatusId();

	public void setStatusId(int status);

	public String getText();

	public void setText(String text);

	public int getTypeId();

	public void setTypeId(int typeInt);

	public int getVersion();

	public void setVersion(int version);

	public void convertIds(I_MapNativeToNative jarToDbNativeMap);

	public I_DescriptionPart duplicate();

}