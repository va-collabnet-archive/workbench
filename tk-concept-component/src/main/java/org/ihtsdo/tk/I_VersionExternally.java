package org.ihtsdo.tk;

import java.util.UUID;

public interface I_VersionExternally {

	public UUID getStatusUuid();
	
	public UUID getAuthorUuid();

	public UUID getPathUuid();

	public long getTime();

}