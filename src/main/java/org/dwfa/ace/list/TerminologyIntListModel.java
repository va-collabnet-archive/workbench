package org.dwfa.ace.list;

import java.io.IOException;

import javax.swing.event.ListDataListener;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_ModelTerminologyList;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.log.AceLog;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.vodb.types.IntList;

public class TerminologyIntListModel implements I_ModelTerminologyList {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private IntList elements ;
	
	public TerminologyIntListModel(IntList elements) {
		super();
		this.elements = elements;
	}
	
	public I_GetConceptData getElementAt(int index) {
		try {
			return LocalVersionedTerminology.get().getConcept(elements.get(index));
		} catch (TerminologyException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (IOException e) {
			AceLog.getAppLog().alertAndLogException(e);
		}
		return null;
	}

	public int getSize() {
		return elements.size();
	}

	public boolean addElement(I_GetConceptData o) {
		boolean rv = elements.add(o.getConceptId());
		return rv;
	}
	public void addElement(int index, I_GetConceptData element) {
		elements.add(index, element.getConceptId());
	}
	public I_GetConceptData removeElement(int index) {
		int id = elements.remove(index);
		try {
			return LocalVersionedTerminology.get().getConcept(id);
		} catch (TerminologyException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (IOException e) {
			AceLog.getAppLog().alertAndLogException(e);
		}
		return null;
	}
	public void clear() {
		elements.clear();
	}

	public void addListDataListener(ListDataListener l) {
		elements.addListDataListener(l);
		
	}

	public void removeListDataListener(ListDataListener l) {
		elements.removeListDataListener(l);
		
	}

	public IntList getElements() {
		return elements;
	}
}
