package org.ihtsdo.tk.api;

import java.io.IOException;


public interface ComponentVersionBI extends ComponentBI {

    boolean isActive(NidSetBI allowedStatusNids);
    /**
     * 
     * @return  <code>true</code> if this version is stored in the read-only
     * database, rather than in the mutable database. <code>false</code> otherwise.
     */
    boolean isBaselineGeneration();
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
