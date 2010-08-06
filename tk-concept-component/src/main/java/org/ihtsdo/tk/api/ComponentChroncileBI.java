package org.ihtsdo.tk.api;

import java.util.Collection;

public interface ComponentChroncileBI<T extends ComponentVersionBI> extends ComponentBI {
	
	public T getVersion(Coordinate c) throws ContraditionException;
	public Collection<T> getVersions(Coordinate c);
	public Collection<T> getVersions();

}
