package org.ihtsdo.arena.conceptview;

import java.util.List;

import org.dwfa.ace.api.I_RelTuple;

public class RelGroupForDragPanel {
	
	public I_RelTuple[] relGroup;

	public I_RelTuple[] getRelGroup() {
		return relGroup;
	}

	public RelGroupForDragPanel(I_RelTuple[] relGroup) {
		super();
		this.relGroup = relGroup;
	}

	public RelGroupForDragPanel(List<I_RelTuple> group) {
		relGroup = group.toArray(new I_RelTuple[]{});
	}
}
