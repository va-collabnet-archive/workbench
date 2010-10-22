package org.ihtsdo.concept;

import java.io.IOException;
import java.util.List;

import org.dwfa.ace.api.I_TrackContinuation;
import org.ihtsdo.tk.api.NidBitSetBI;

public interface I_ProcessUnfetchedConceptData extends I_TrackContinuation {

    public void processUnfetchedConceptData(int cNid, I_FetchConceptFromCursor fcfc) throws Exception;

    public void setParallelConceptIterators(List<ParallelConceptIterator> pcis);
    
    public NidBitSetBI getNidSet() throws IOException;

}
