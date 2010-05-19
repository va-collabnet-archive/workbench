package org.ihtsdo.etypes;

import java.util.UUID;

public interface I_VersionExternally {

	public UUID getPathUuid();

	public UUID getStatusUuid();

	public long getTime();

}