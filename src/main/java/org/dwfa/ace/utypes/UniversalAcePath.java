package org.dwfa.ace.utypes;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

public class UniversalAcePath implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private List<UUID> pathId;
	
	private List<UniversalAcePosition> origins;
	
	public UniversalAcePath(List<UUID> pathId, List<UniversalAcePosition> origins) {
		super();
		this.pathId = pathId;
		this.origins = origins;
	}

	public List<UniversalAcePosition> getOrigins() {
		return origins;
	}

	public List<UUID> getPathId() {
		return pathId;
	}
}
