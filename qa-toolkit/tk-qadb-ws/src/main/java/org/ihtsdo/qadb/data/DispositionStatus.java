package org.ihtsdo.qadb.data;

import java.util.UUID;

public class DispositionStatus implements Comparable {
	
	private String dispositionStatusUuid;
	private String name;
	
	public DispositionStatus() {
		super();
	}
	
	public DispositionStatus(String dispositionStatusUuid, String name) {
		super();
		this.dispositionStatusUuid = dispositionStatusUuid;
		this.name = name;
	}
	
	public String getDispositionStatusUuid() {
		return dispositionStatusUuid;
	}
	public void setDispositionStatusUuid(String dispositionStatusUuid) {
		this.dispositionStatusUuid = dispositionStatusUuid;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public int compareTo(Object otherObject) {
		DispositionStatus otherStatus = (DispositionStatus) otherObject;
		return otherStatus.getName().compareTo(getName());
	}

	@Override
	public String toString() {
		return "DispositionStatus [dispositionStatusUuid=" + dispositionStatusUuid + ", name=" + name + "]";
	}
	
	
}
