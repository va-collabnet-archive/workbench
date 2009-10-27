package org.dwfa.ace;

import java.io.IOException;
import java.util.List;

import org.dwfa.ace.api.I_RelTuple;

public class LabelForGroupTuple extends LabelForTuple {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private List<I_RelTuple> relGroup;

	public LabelForGroupTuple(List<I_RelTuple> rel, boolean longForm,
			boolean showStatus) {
		super(longForm, showStatus);
		this.relGroup = rel;
	}

	public I_ImplementActiveLabel copy() throws IOException {
		return TermLabelMaker.newLabel(relGroup, isLongForm(), getShowStatus());
	}

	public List<I_RelTuple> getRelGroup() {
		return relGroup;
	}

	@Override
	protected boolean tupleEquals(Object obj) {
		if (LabelForGroupTuple.class.isAssignableFrom(obj.getClass())) {
			LabelForGroupTuple another = (LabelForGroupTuple) obj;
			return relGroup.equals(another.relGroup);
		}
		return false;
	}

	@Override
	protected int tupleHash() {
		return this.relGroup.hashCode();
	}

	@Override
	protected String getTupleString() {
		return relGroup.toString();
	}

}
