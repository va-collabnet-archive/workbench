package org.dwfa.ace.api;


public interface I_DescriptionPart extends I_AmTypedPart {

	public boolean hasNewData(I_DescriptionPart another);

	public void setPathId(int pathId);

	public boolean getInitialCaseSignificant();

	public void setInitialCaseSignificant(boolean capStatus);

	public String getLang();

	public void setLang(String lang);

	public void setStatusId(int status);

	public String getText();

	public void setText(String text);

	public void setTypeId(int typeInt);

	public void setVersion(int version);

	public void convertIds(I_MapNativeToNative jarToDbNativeMap);

	public I_DescriptionPart duplicate();

}