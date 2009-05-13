package org.dwfa.ace.api;


public interface I_ImagePart extends I_AmTypedPart {

	public String getTextDescription();

	public void setTextDescription(String name);

	public boolean hasNewData(I_ImagePart another);

	public I_ImagePart duplicate();

}