package org.ihtsdo.qa.store.model;

import java.util.UUID;

public class DispositionStatus implements Comparable {
	
	private UUID dispositionStatusUuid;
	private String name;
	
	public DispositionStatus() {
		super();
	}
	
	public DispositionStatus(UUID dispositionStatusUuid, String name) {
		super();
		this.dispositionStatusUuid = dispositionStatusUuid;
		this.name = name;
	}
	
	public UUID getDispositionStatusUuid() {
		return dispositionStatusUuid;
	}
	public void setDispositionStatusUuid(UUID dispositionStatusUuid) {
		this.dispositionStatusUuid = dispositionStatusUuid;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public String toString() {
		return getName();
	}

	@Override
	public int compareTo(Object otherObject) {
		DispositionStatus otherStatus = (DispositionStatus) otherObject;
		return otherStatus.getName().compareTo(getName());
	}
}
