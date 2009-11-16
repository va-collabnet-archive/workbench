package org.dwfa.ace.api;


public interface I_DescriptionPart extends I_AmTypedPart {

	public boolean getInitialCaseSignificant();

	public void setInitialCaseSignificant(boolean capStatus);

	public String getLang();

	public void setLang(String lang);

	public String getText();

	public void setText(String text);

	public I_DescriptionPart duplicate();

}