package org.ihtsdo.workunit.sif;

import java.util.List;

public class SifChangeSet {
	
	private List<SifChange> changes;
	private SifSource source;
	
	public SifChangeSet() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @return the changes
	 */
	public List<SifChange> getChanges() {
		return changes;
	}

	/**
	 * @param changes the changes to set
	 */
	public void setChanges(List<SifChange> changes) {
		this.changes = changes;
	}

	/**
	 * @return the source
	 */
	public SifSource getSource() {
		return source;
	}

	/**
	 * @param source the source to set
	 */
	public void setSource(SifSource source) {
		this.source = source;
	}


}
