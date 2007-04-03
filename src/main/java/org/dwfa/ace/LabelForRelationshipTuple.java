package org.dwfa.ace;


import org.dwfa.ace.api.I_RelTuple;

import com.sleepycat.je.DatabaseException;


public class LabelForRelationshipTuple extends LabelForTuple {
	

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private I_RelTuple rel;
	
	
	public LabelForRelationshipTuple(I_RelTuple rel, boolean longForm, boolean showStatus) {
		super(longForm, showStatus);
		this.rel = rel;
	}
	

	public I_ImplementActiveLabel copy() throws DatabaseException {
		return TermLabelMaker.newLabel(rel, isLongForm(), getShowStatus());
	}

	public I_RelTuple getRel() {
		return rel;
	}

	@Override
	protected boolean tupleEquals(Object obj) {
		if (LabelForRelationshipTuple.class.isAssignableFrom(obj.getClass())) {
			LabelForRelationshipTuple another = (LabelForRelationshipTuple) obj;
			return rel.equals(another.rel);
		}
		return false;
	}

	@Override
	protected int tupleHash() {
		return this.rel.hashCode();
	}

	@Override
	protected String getTupleString() {
		return rel.toString();
	}
	
}
