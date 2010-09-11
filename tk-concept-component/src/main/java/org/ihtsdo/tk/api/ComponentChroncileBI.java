package org.ihtsdo.tk.api;

import java.util.Collection;

public interface ComponentChroncileBI<T extends ComponentVersionBI> extends ComponentBI {

	public T getVersion(Coordinate c) throws ContraditionException;
	public Collection<? extends T> getVersions(Coordinate c);
	public Collection<? extends T> getVersions();

	public boolean isUncommitted();

}
