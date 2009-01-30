package org.dwfa.ace.utypes;

import java.util.Collection;
import java.util.UUID;

public interface I_VersionComponent {

	public Collection<UUID> getPathId();
	public void setPathId(Collection<UUID> pathId);
	
	public Collection<UUID> getStatusId();
	public void setStatusId(Collection<UUID> status);
	
	public long getTime();
	public void setTime(long version);

}
