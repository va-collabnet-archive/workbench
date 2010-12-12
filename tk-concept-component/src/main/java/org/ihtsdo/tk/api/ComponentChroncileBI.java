package org.ihtsdo.tk.api;

import java.util.Collection;
import org.ihtsdo.tk.api.refset.RefsetMemberChronicleBI;

public interface ComponentChroncileBI<T extends ComponentVersionBI> extends ComponentBI {

    T getVersion(Coordinate c) throws ContraditionException;

    Collection<? extends T> getVersions(Coordinate c);

    Collection<? extends T> getVersions();
    
    boolean isUncommitted();

    boolean addAnnotation(RefsetMemberChronicleBI annotation);

    Collection<? extends RefsetMemberChronicleBI> getAnnotations();
}
