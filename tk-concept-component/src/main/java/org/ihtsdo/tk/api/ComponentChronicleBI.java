package org.ihtsdo.tk.api;

import java.io.IOException;
import java.util.Collection;
import java.util.Set;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.coordinate.EditCoordinate;

import org.ihtsdo.tk.api.coordinate.ViewCoordinate;

public interface ComponentChronicleBI<T extends ComponentVersionBI>
        extends ComponentBI {

    T getVersion(ViewCoordinate viewCoordinate) throws ContradictionException;

    Collection<? extends T> getVersions(ViewCoordinate viewCoordinate);

    Collection<? extends T> getVersions();

    boolean isUncommitted();

    Set<Integer> getAllStampNids() throws IOException;
    
    Set<PositionBI> getPositions() throws IOException;
    
    T getPrimordialVersion();
    
    boolean makeAdjudicationAnalogs(EditCoordinate editCoordinate, ViewCoordinate viewCoordinate) throws Exception;
    
    ConceptChronicleBI getEnclosingConcept();

}
