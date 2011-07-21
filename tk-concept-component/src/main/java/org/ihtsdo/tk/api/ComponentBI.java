package org.ihtsdo.tk.api;


import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.api.refex.RefexVersionBI;

public interface ComponentBI {

    UUID getPrimUuid();

    List<UUID> getUUIDs();

    int getNid();

    int getConceptNid();

    String toUserString();
    
    Collection<? extends RefexChronicleBI<?>> getRefexes() throws IOException;

    Collection<? extends RefexChronicleBI<?>> getRefexes(int refsetNid) throws IOException;

    Collection<? extends RefexVersionBI<?>> getCurrentRefexes(ViewCoordinate xyz) throws IOException;

    Collection<? extends RefexVersionBI<?>> getCurrentRefexes(ViewCoordinate xyz, int refsetNid)
            throws IOException;

    Collection<? extends RefexVersionBI<?>> getInactiveRefexes(ViewCoordinate xyz) throws IOException;

    boolean addAnnotation(RefexChronicleBI<?> annotation) throws IOException;

    Collection<? extends RefexChronicleBI<?>> getAnnotations() throws IOException;

    Collection<? extends RefexVersionBI<?>> getCurrentAnnotations(ViewCoordinate xyz) throws IOException;

}
