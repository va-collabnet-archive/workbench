package org.dwfa.ace.api;

public interface I_ImageTuple {

	public abstract byte[] getImage();

	public abstract int getImageId();

	public abstract int getPathId();

	public abstract int getStatusId();

	public abstract int getVersion();

	public abstract String getTextDescription();

	public abstract int getTypeId();

	public abstract String getFormat();

	public abstract int getConceptId();

	public abstract I_ImageVersioned getVersioned();

	public abstract I_ImagePart duplicatePart();

}