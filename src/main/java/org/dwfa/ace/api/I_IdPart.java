package org.dwfa.ace.api;

public interface I_IdPart extends I_AmPart {

	/**
	 * @deprecated Use {@link #getStatusId()}
	 */
	@Deprecated
	public int getIdStatus();

	/**
	 * @deprecated Use {@link #setStatusId(int)}
	 */
	@Deprecated
	public void setIdStatus(int idStatus);

	public int getSource();

	public void setSource(int source);

	public Object getSourceId();

	public void setSourceId(Object sourceId);

	public boolean hasNewData(I_IdPart another);
	
	public I_IdPart duplicate();

}