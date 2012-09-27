package org.ihtsdo.concept;

import java.util.List;

import org.dwfa.ace.api.I_TrackContinuation;
import org.ihtsdo.tk.api.ProcessUnfetchedConceptDataBI;

public interface I_ProcessUnfetchedConceptData extends I_TrackContinuation,
        ProcessUnfetchedConceptDataBI {

     public void setParallelConceptIterators(List<ParallelConceptIterator> pcis);
     
}
