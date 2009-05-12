package org.dwfa.ace.api;

public interface I_ImageTuple extends I_AmTypedPart {

	public byte[] getImage();

	public int getImageId();

	public int getPathId();

	public int getStatusId();

	public int getVersion();

	public String getTextDescription();

	public int getTypeId();

	public String getFormat();

	public int getConceptId();

	public I_ImageVersioned getVersioned();

	public I_ImagePart duplicatePart();

	public I_ImagePart getPart();


}