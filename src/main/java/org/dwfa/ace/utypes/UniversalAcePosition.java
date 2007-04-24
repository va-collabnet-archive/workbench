package org.dwfa.ace.utypes;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class UniversalAcePosition implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Collection<UUID> pathId;

	private long time;

	public UniversalAcePosition() {
		super();
	}

	public UniversalAcePosition(Collection<UUID> pathId, long version) {
		super();
		this.pathId = pathId;
		this.time = version;
	}

	public Collection<UUID> getPathId() {
		return pathId;
	}

	public void setPathId(List<UUID> pathId) {
		this.pathId = pathId;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long version) {
		this.time = version;
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + " id: "  + pathId + " time: " + time + " (" + new Date(time) + ")";
	}

}
