package org.dwfa.ace.api;

public interface I_HelpMarkedParentRefsets extends I_HelpRefsets {

	public void addParentMembers(Integer... conceptIds) throws Exception;

	public void removeParentMembers(Integer... conceptIds) throws Exception;

	public boolean isMarkedParent(int conceptId) throws Exception;

	public int getParentRefset() throws Exception;

	public boolean hasCurrentMarkedParentExtension(int conceptId)
			throws Exception;

}