package org.dwfa.ace.refset.spec;

public interface I_HelpMarkedParentRefset {

	public void addParentMembers(Integer... conceptIds) throws Exception;

	public void addDescriptionParentMembers(Integer... descriptionIds)
			throws Exception;

	public void removeParentMembers(Integer... conceptIds) throws Exception;

	public void removeDescriptionParentMembers(Integer... descriptionIds)
			throws Exception;

	public boolean isMarkedParent(int conceptId) throws Exception;

	public int getParentRefset() throws Exception;

	public boolean hasCurrentMarkedParentExtension(int conceptId)
			throws Exception;

}