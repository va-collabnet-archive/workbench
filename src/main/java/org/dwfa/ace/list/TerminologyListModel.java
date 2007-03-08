package org.dwfa.ace.list;

import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractListModel;

import org.dwfa.vodb.types.ConceptBean;
import org.dwfa.vodb.types.I_GetConceptData;

import com.sleepycat.je.DatabaseException;

public class TerminologyListModel extends AbstractListModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	List<ConceptBean> elements = new ArrayList<ConceptBean>();
	
	public TerminologyListModel(List<ConceptBean> elements) {
		super();
		this.elements = elements;
	}
	
	public TerminologyListModel() {
		super();
	}


	public I_GetConceptData getElementAt(int index) {
		return elements.get(index);
	}

	public int getSize() {
		return elements.size();
	}

	public boolean addElement(ConceptBean o) {
		boolean rv = elements.add(o);
		fireIntervalAdded(this, elements.size() -1, elements.size() -1);
		try {
			System.out.println("Added: " + o.getInitialText());
		} catch (DatabaseException e) {
			throw new RuntimeException(e);
		}
		return rv;
	}
	public void addElement(int index, ConceptBean element) {
		elements.add(index, element);
		fireIntervalAdded(this, index, index);
	}
	public I_GetConceptData removeElement(int index) {
		I_GetConceptData rv = elements.remove(index);
		fireIntervalRemoved(this, index, index);
		return rv;
	}

}
