package org.ihtsdo.tk.api;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;


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

    public PathBI getMatchingPath(int pathNid);

	public String toHtmlString() throws IOException;
	
	public List<UUID> getUUIDs();

}
