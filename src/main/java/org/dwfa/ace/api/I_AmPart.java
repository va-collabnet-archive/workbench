package org.dwfa.ace.api;

import org.apache.commons.collections.primitives.ArrayIntList;

public interface I_AmPart {
	
	/**
	 * 
	 * @return the position identifier of this part. 
	 */
	public int getPositionId();
	
	/**
	 * 
	 * @param pid the position identifier for this part.
	 */
	public void setPositionId(int pid);
	

	public int getPathId();
	public int getVersion();
	public int getStatusId();
	
	public void setPathId(int pathId);
	public void setVersion(int version);
	public void setStatusId(int statusId);
	
	public I_AmPart duplicate();
	
	public ArrayIntList getPartComponentNids();
	
}
