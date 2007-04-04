package org.dwfa.ace;


import java.io.IOException;

import org.dwfa.ace.api.I_DescriptionTuple;

public class LabelForDescriptionTuple extends LabelForTuple {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private I_DescriptionTuple desc;
	
	
	public LabelForDescriptionTuple(I_DescriptionTuple desc, boolean longForm, boolean showStatus) {
		super(longForm, showStatus);
		if (desc == null) {
			throw new NullPointerException("desc cannot be null...");
		}
		this.desc = desc;
	}

	public I_ImplementActiveLabel copy() throws IOException {
		return TermLabelMaker.newLabel(desc, isLongForm(), getShowStatus());
	}

	public I_DescriptionTuple getDesc() {
		return desc;
	}

	@Override
	protected boolean tupleEquals(Object obj) {
		if (LabelForDescriptionTuple.class.isAssignableFrom(obj.getClass())) {
			LabelForDescriptionTuple another = (LabelForDescriptionTuple) obj;
			return desc.equals(another.desc);
		}
		return false;
	}

	@Override
	protected int tupleHash() {
		return this.desc.hashCode();
	}
	@Override
	protected String getTupleString() {
		return desc.toString();
	}

}
