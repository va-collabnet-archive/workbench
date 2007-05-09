package org.dwfa.ace.api;

import javax.swing.ListModel;

public interface I_ModelTerminologyList extends ListModel {
	public I_GetConceptData getElementAt(int index);

	public int getSize();

	public boolean addElement(I_GetConceptData concept);
	public void addElement(int index, I_GetConceptData element);
	public I_GetConceptData removeElement(int index);
	public void clear();

}
