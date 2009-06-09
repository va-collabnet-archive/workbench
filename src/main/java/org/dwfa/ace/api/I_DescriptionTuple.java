package org.dwfa.ace.api;

public interface I_DescriptionTuple extends I_AmTypedTuple {

	public abstract boolean getInitialCaseSignificant();

	public abstract String getLang();

	public abstract String getText();

	public abstract int getConceptId();

	public abstract int getDescId();

	public abstract void setInitialCaseSignificant(boolean capStatus);

	public abstract void setLang(String lang);

	public abstract void setText(String text);

	/**
	 * @deprecated Use {@link #duplicate()}
	 */
	@Deprecated
	public abstract I_DescriptionPart duplicatePart();

	public I_DescriptionPart duplicate();
	
	public abstract I_DescriptionPart getPart();

	public abstract I_DescriptionVersioned getDescVersioned();

}