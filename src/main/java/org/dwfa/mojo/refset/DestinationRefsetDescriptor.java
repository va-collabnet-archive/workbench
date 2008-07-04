package org.dwfa.mojo.refset;

import java.util.UUID;

/**
 * Describes a destination refset to be modified, including the path 
 * it should be modified on (at present this is singular).  
 */
public class DestinationRefsetDescriptor {

	private UUID refsetUUID;
	
	private UUID pathUUID;

	public UUID getRefsetUUID() {
		return refsetUUID;
	}

	public void setRefsetUUID(String refsetUUID) {
		this.refsetUUID = UUID.fromString(refsetUUID);
	}

	public UUID getPathUUID() {
		return pathUUID;
	}

	public void setPathUUID(String pathUUID) {
		this.pathUUID = UUID.fromString(pathUUID);
	}
	
}
