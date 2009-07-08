package org.dwfa.ace.api;

import org.apache.commons.collections.primitives.ArrayIntList;

public interface I_AmPart {
	
	public int getPathId();
	public int getVersion();
	public int getStatusId();
	
	public void setPathId(int pathId);
	public void setVersion(int version);
	public void setStatusId(int statusId);
	
	public I_AmPart duplicate();
	
	public ArrayIntList getPartComponentNids();
	
}
