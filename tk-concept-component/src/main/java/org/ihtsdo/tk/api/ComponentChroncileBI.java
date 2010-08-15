package org.ihtsdo.tk.api;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface ComponentChroncileBI<T extends ComponentVersionBI> extends ComponentBI {
	
	public List<UUID> getUUIDs();

	public T getVersion(Coordinate c) throws ContraditionException;
	public Collection<? extends T> getVersions(Coordinate c);
	public Collection<? extends T> getVersions();

}
