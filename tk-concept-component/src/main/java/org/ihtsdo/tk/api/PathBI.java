package org.ihtsdo.tk.api;

import java.util.Collection;
import java.util.Set;


public interface PathBI {

    public int getConceptNid();

    public Collection<? extends PositionBI> getOrigins();

    /**
     * Get all origins and origin of origins, etc., for this path.
     */
    public Set<? extends PositionBI> getInheritedOrigins();

    /**
     * Similar to {@link #getInheritedOrigins()} however superseded origins
     * (where there is more than one origin for the same path but with an 
     * earlier version) will be excluded.
     */
    public Set<? extends PositionBI> getNormalisedOrigins();

}
