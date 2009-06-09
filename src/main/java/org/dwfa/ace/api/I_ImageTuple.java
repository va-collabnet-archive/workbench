package org.dwfa.ace.api;

public interface I_ImageTuple extends I_AmTypedTuple {

	public byte[] getImage();

	public int getImageId();

	public String getTextDescription();

	public String getFormat();

	public int getConceptId();

	public I_ImageVersioned getVersioned();

	/**
	 * @deprecated Use {@link #duplicate()}
	 */
	@Deprecated
	public I_ImagePart duplicatePart();

	public I_ImagePart duplicate();
	
	public I_ImagePart getPart();


}