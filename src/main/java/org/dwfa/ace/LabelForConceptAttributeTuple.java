package org.dwfa.ace;

import org.dwfa.ace.api.I_ConceptAttributeTuple;

import com.sleepycat.je.DatabaseException;

public class LabelForConceptAttributeTuple extends LabelForTuple {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private I_ConceptAttributeTuple conAttr;
	
	
	public LabelForConceptAttributeTuple(I_ConceptAttributeTuple conAttr, boolean longForm, boolean showStatus) {
		super(longForm, showStatus);
		this.conAttr = conAttr;
	}

	public I_ImplementActiveLabel copy() throws DatabaseException {
		return TermLabelMaker.newLabel(conAttr, isLongForm(), getShowStatus());
	}

	public I_ConceptAttributeTuple getConAttr() {
		return conAttr;
	}

	@Override
	protected boolean tupleEquals(Object obj) {
		if (LabelForConceptAttributeTuple.class.isAssignableFrom(obj.getClass())) {
			LabelForConceptAttributeTuple another = (LabelForConceptAttributeTuple) obj;
			return conAttr.equals(another.conAttr);
		}
		return false;
	}

	@Override
	protected int tupleHash() {
		return this.conAttr.hashCode();
	}
	@Override
	protected String getTupleString() {
		return conAttr.toString();
	}

}
