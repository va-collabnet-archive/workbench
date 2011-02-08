package org.ihtsdo.tk.api.coordinate;

import org.ihtsdo.tk.api.NidSetBI;


public class EditCoordinate {
	private int authorNid;
	private int[] editPaths;
	
	public EditCoordinate(int authorNid, NidSetBI editPaths) {
		super();
		assert editPaths != null;
		assert authorNid != Integer.MIN_VALUE;
		this.authorNid = authorNid;
		this.editPaths = editPaths.getSetValues();
	}
	
	public EditCoordinate(int authorNid, int... editPathNids) {
		super();
		assert editPathNids != null;
		assert authorNid != Integer.MIN_VALUE;
		this.authorNid = authorNid;
		this.editPaths = editPathNids;
	}

	public int getAuthorNid() {
		return authorNid;
	}

	public int[] getEditPaths() {
		return editPaths;
	}
}
