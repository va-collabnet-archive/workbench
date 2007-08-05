package org.dwfa.ace.api;

public interface I_DescriptionTuple {

	public abstract int getPathId();

	public abstract boolean getInitialCaseSignificant();

	public abstract String getLang();

	public abstract int getStatusId();

	public abstract String getText();

	public abstract int getTypeId();

	public abstract int getVersion();

	public abstract int getConceptId();

	public abstract int getDescId();

	public abstract void setInitialCaseSignificant(boolean capStatus);

	public abstract void setLang(String lang);

	public abstract void setPathId(int pathId);

	public abstract void setStatusId(int status);

	public abstract void setText(String text);

	public abstract void setTypeId(int typeInt);

	public abstract void setVersion(int version);

   public abstract I_DescriptionPart duplicatePart();

   public abstract I_DescriptionPart getPart();

	public abstract I_DescriptionVersioned getDescVersioned();

}