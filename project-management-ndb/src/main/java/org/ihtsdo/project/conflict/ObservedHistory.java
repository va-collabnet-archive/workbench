package org.ihtsdo.project.conflict;

import java.util.Set;

public class ObservedHistory {
	
	private Long time;
	private Set<ChangeRecord> visibleRecords;
	
	public ObservedHistory(Long time, Set<ChangeRecord> visibleRecords) {
		super();
		this.time = time;
		this.visibleRecords = visibleRecords;
	}

	public Long getTime() {
		return time;
	}

	public void setTime(Long time) {
		this.time = time;
	}

	public Set<ChangeRecord> getVisibleRecords() {
		return visibleRecords;
	}

	public void setVisibleRecords(Set<ChangeRecord> visibleRecords) {
		this.visibleRecords = visibleRecords;
	}
}
