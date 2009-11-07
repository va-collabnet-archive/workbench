package org.dwfa.ace.api;


public interface I_RepresentIdSet {

	public boolean isMember(int nid);

	public void setMember(int nid);

	public void setNotMember(int nid);

	public void and(I_RepresentIdSet other);

	public void or(I_RepresentIdSet other);
	
	public I_IterateIds iterator();
	
	public int size();

}