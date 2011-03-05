package org.ihtsdo.tk.api;

import java.io.IOException;
import java.util.Collection;
import java.util.Set;

import org.ihtsdo.tk.api.coordinate.ViewCoordinate;

public interface ComponentChroncileBI<T extends ComponentVersionBI>
        extends ComponentBI {

    T getVersion(ViewCoordinate c) throws ContraditionException;

    Collection<? extends T> getVersions(ViewCoordinate c);

    Collection<? extends T> getVersions();

    boolean isUncommitted();

    Set<Integer> getAllSapNids() throws IOException;
    
    Set<PositionBI> getPositions() throws IOException;

}
