package org.ihtsdo.tk.api;

import java.io.IOException;


public interface ComponentVersionBI extends ComponentBI {

    boolean isActive(NidSetBI allowedStatusNids);
    int getStatusNid();
    int getAuthorNid();
    int getPathNid();
    long getTime();
    int getSapNid();
    boolean sapIsInRange(int min, int max);
    PositionBI getPosition() throws IOException;
    ComponentChroncileBI getChronicle();
    String toUserString(TerminologySnapshotDI snapshot) 
            throws IOException, ContraditionException;
    
}
